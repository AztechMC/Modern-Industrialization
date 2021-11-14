/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.api.WhitelistedItemStorage;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.item.ItemNetwork.InsertTarget;
import aztech.modern_industrialization.util.MIBlockApiCache;
import aztech.modern_industrialization.util.StorageUtil2;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class ItemNetwork extends PipeNetwork {
    private static final ReferenceOpenHashSet<Item> WHITELIST_CACHED_SET = new ReferenceOpenHashSet<>();

    private int inactiveTicks = 0;

    public ItemNetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new ItemNetworkData() : data);
    }

    @Override
    public void tick(ServerWorld world) {
        // Only tick once
        if (inactiveTicks == 0) {
            doNetworkTransfer(world);
            inactiveTicks = 60;
        }
        --inactiveTicks;
    }

    private void doNetworkTransfer(ServerWorld world) {
        List<ExtractionTarget> extractionTargets = new ArrayList<>();
        for (var entry : iterateTickingNodes()) {
            BlockPos pos = entry.getPos();
            ItemNetworkNode itemNode = (ItemNetworkNode) entry.getNode();
            for (ItemNetworkNode.ItemConnection connection : itemNode.connections) {
                if (connection.canExtract()) {
                    Storage<ItemVariant> source = ItemStorage.SIDED.find(world, pos.offset(connection.direction), connection.direction.getOpposite());

                    if (source != null) {
                        extractionTargets.add(new ExtractionTarget(connection, source));
                    }
                }
            }
        }
        // Lower priority extracts first.
        extractionTargets.sort(Comparator.comparing(et -> et.connection.priority));

        // Do the actual transfer.
        Storage<ItemVariant> insertTargets = getAggregateInsertTarget(world);
        try (Transaction tx = Transaction.openOuter()) {
            for (ExtractionTarget target : extractionTargets) {
                StorageUtil.move(target.storage, insertTargets, target.connection::canStackMoveThrough, target.connection.getMoves(), tx);
            }
            tx.commit();
        }
    }

    private static class ExtractionTarget {
        private final ItemNetworkNode.ItemConnection connection;
        private final Storage<ItemVariant> storage;

        private ExtractionTarget(ItemNetworkNode.ItemConnection connection, Storage<ItemVariant> storage) {
            this.connection = connection;
            this.storage = storage;
        }
    }

    /**
     * Find all connections in which to insert that are loaded.
     */
    private Storage<ItemVariant> getAggregateInsertTarget(ServerWorld world) {
        Int2ObjectMap<PriorityBucket> priorityBuckets = new Int2ObjectOpenHashMap<>();

        for (var entry : iterateTickingNodes()) {
            ItemNetworkNode node = (ItemNetworkNode) entry.getNode();
            for (ItemNetworkNode.ItemConnection connection : node.connections) {
                if (connection.canInsert()) {
                    if (connection.cache == null) {
                        connection.cache = MIBlockApiCache.create(ItemStorage.SIDED, world, entry.getPos().offset(connection.direction));
                    }
                    Storage<ItemVariant> target = connection.cache.find(connection.direction.getOpposite());
                    if (target != null && target.supportsInsertion()) {
                        PriorityBucket bucket = priorityBuckets.computeIfAbsent(connection.priority, PriorityBucket::new);
                        InsertTarget it = new InsertTarget(connection, StorageUtil2.wrapInventory(target));

                        if (connection.whitelist || (target instanceof WhitelistedItemStorage wis && wis.currentlyWhitelisted())) {
                            bucket.whitelist.add(it);
                        } else {
                            bucket.blacklist.add(it);
                        }
                    }
                }
            }
        }

        PriorityBucket[] sortedBuckets = priorityBuckets.values().toArray(new PriorityBucket[0]);
        // Now we sort by priority, high to low
        Arrays.sort(sortedBuckets, Comparator.comparingInt(pb -> -pb.priority));

        List<Storage<ItemVariant>> targets = new ArrayList<>();
        Random random = ThreadLocalRandom.current();

        for (PriorityBucket pb : sortedBuckets) {
            int whitelistSize = pb.whitelist.size();
            int blacklistSize = pb.blacklist.size();
            if (whitelistSize > 0) {
                Collections.shuffle(pb.whitelist);
                targets.add(new WhitelistAggregate(pb.whitelist));
            }
            if (blacklistSize > 0) {
                Collections.shuffle(pb.blacklist);
                targets.add(new BlacklistAggregate(pb.blacklist));
            }

            // Ensure equal chance to receive items on average.
            if (whitelistSize > 0 && blacklistSize > 0) {
                int tot = whitelistSize + blacklistSize;

                if (random.nextDouble() >= (double) whitelistSize / (whitelistSize + blacklistSize)) {
                    Collections.swap(targets, targets.size() - 2, targets.size() - 1);
                }
            }
        }

        return new CombinedStorage<>(targets);
    }

    private static class PriorityBucket {
        private final int priority;
        private final List<InsertTarget> whitelist = new ArrayList<>();
        private final List<InsertTarget> blacklist = new ArrayList<>();

        private PriorityBucket(int priority) {
            this.priority = priority;
        }
    }

    private static class WhitelistAggregate implements InsertionOnlyStorage<ItemVariant> {
        // Used when the inserted item doesn't have NBT
        private final Map<Item, List<Storage<ItemVariant>>> map = new IdentityHashMap<>();
        // Used when the inserted item has NBT.
        private final List<InsertTarget> targets;

        WhitelistAggregate(List<InsertTarget> targets) {
            this.targets = targets;
            for (InsertTarget target : targets) {
                if (target.connection.whitelist) {
                    ItemNetworkNode.ItemConnection conn = target.connection;
                    for (ItemVariant variant : conn.stacksCache) {
                        if (!variant.hasNbt()) {
                            map.computeIfAbsent(variant.getItem(), v -> new ArrayList<>()).add(target.target);
                        }
                    }
                } else if (target.target instanceof WhitelistedItemStorage wis) {
                    WHITELIST_CACHED_SET.clear();
                    wis.getWhitelistedItems(WHITELIST_CACHED_SET);
                    for (Item item : WHITELIST_CACHED_SET) {
                        map.computeIfAbsent(item, v -> new ArrayList<>()).add(target.target);
                    }
                } else {
                    throw new IllegalStateException("Internal item pipe error! Should never happen!");
                }
            }
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.hasNbt()) {
                return insertTargets(targets, resource, maxAmount, transaction);
            }

            StoragePreconditions.notBlankNotNegative(resource, maxAmount);
            long totalInserted = 0;

            List<Storage<ItemVariant>> targets = map.get(resource.getItem());
            if (targets != null) {
                for (Storage<ItemVariant> target : targets) {
                    long inserted = target.insert(resource, maxAmount, transaction);
                    maxAmount -= inserted;
                    totalInserted += inserted;
                    if (maxAmount == 0) {
                        break;
                    }
                }
            }

            return totalInserted;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }
    }

    private static class BlacklistAggregate implements InsertionOnlyStorage<ItemVariant> {
        private final List<InsertTarget> targets;

        private BlacklistAggregate(List<InsertTarget> targets) {
            this.targets = targets;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return insertTargets(targets, resource, maxAmount, transaction);
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator(TransactionContext transaction) {
            return Collections.emptyIterator();
        }
    }

    private static long insertTargets(List<InsertTarget> targets, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);
        long totalInserted = 0;

        for (InsertTarget target : targets) {
            if (target.connection.canStackMoveThrough(resource)) {
                long inserted = target.target.insert(resource, maxAmount, transaction);
                maxAmount -= inserted;
                totalInserted += inserted;
                if (maxAmount == 0) {
                    break;
                }
            }
        }

        return totalInserted;
    }

    private record InsertTarget(ItemNetworkNode.ItemConnection connection, Storage<ItemVariant> target) {
    }
}
