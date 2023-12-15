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

import aztech.modern_industrialization.inventory.WhitelistedItemStorage;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.util.StorageUtil2;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
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
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;

public class ItemNetwork extends PipeNetwork {
    public static final int TICK_RATE = 60;
    private static final ReferenceOpenHashSet<Item> WHITELIST_CACHED_SET = new ReferenceOpenHashSet<>();

    int inactiveTicks = 0;
    long lastMovedItems = 0;

    public ItemNetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new ItemNetworkData() : data);
    }

    @Override
    public void tick(ServerLevel world) {
        // Only tick once
        if (inactiveTicks == 0) {
            doNetworkTransfer(world);
            inactiveTicks = TICK_RATE;
        }
        --inactiveTicks;
    }

    private void doNetworkTransfer(ServerLevel world) {
        List<ExtractionTarget> extractionTargets = new ArrayList<>();
        for (var entry : iterateTickingNodes()) {
            BlockPos pos = entry.getPos();
            ItemNetworkNode itemNode = (ItemNetworkNode) entry.getNode();
            for (ItemNetworkNode.ItemConnection connection : itemNode.connections) {
                if (connection.canExtract()) {
                    var queryPos = pos.relative(connection.direction);
                    var querySide = connection.direction.getOpposite();

                    Storage<ItemVariant> source = ItemStorage.SIDED.find(world, queryPos, querySide);

                    if (source != null) {
                        extractionTargets.add(new ExtractionTarget(connection, source, queryPos, querySide));
                    }
                }
            }
        }
        // Lower priority extracts first.
        extractionTargets.sort(Comparator.comparing(et -> et.connection.extractPriority));

        // Do the actual transfer.
        var insertTargets = getAggregatedInsertTargets(world);
        var insertStorage = new CombinedStorage<>(insertTargets);
        lastMovedItems = 0;
        try (Transaction tx = Transaction.openOuter()) {
            for (ExtractionTarget target : extractionTargets) {
                // Lower priority extracts first, and pipes can only move items to things that have >= priorities.
                // So we can just pop insert targets at the end of the list if they have a priority smaller than the current extraction target.
                while (insertTargets.size() > 0 && target.connection.extractPriority > insertTargets.get(insertTargets.size() - 1).getPriority()) {
                    insertTargets.remove(insertTargets.size() - 1);
                }

                try {
                    lastMovedItems += StorageUtil.move(target.storage, insertStorage, target.connection::canStackMoveThrough,
                            target.connection.getMoves(), tx);
                } catch (Exception exception) {
                    var crashReport = CrashReport.forThrowable(exception, "Moving items in a pipe network");
                    crashReport.addCategory("Block being extracted from:")
                            .setDetail("Dimension", world.dimension())
                            .setDetail("Position", target.queryPos)
                            .setDetail("Accessed from side", target.querySide);
                    throw new ReportedException(crashReport);
                }
            }
            tx.commit();
        }
    }

    private static class ExtractionTarget {
        private final ItemNetworkNode.ItemConnection connection;
        private final Storage<ItemVariant> storage;
        private final BlockPos queryPos;
        private final Direction querySide;

        private ExtractionTarget(ItemNetworkNode.ItemConnection connection, Storage<ItemVariant> storage, BlockPos queryPos, Direction querySide) {
            this.connection = connection;
            this.storage = storage;
            this.queryPos = queryPos;
            this.querySide = querySide;
        }
    }

    /**
     * Find all connections in which to insert that are loaded.
     */
    private List<Aggregate> getAggregatedInsertTargets(ServerLevel world) {
        Int2ObjectMap<PriorityBucket> priorityBuckets = new Int2ObjectOpenHashMap<>();

        for (var entry : iterateTickingNodes()) {
            ItemNetworkNode node = (ItemNetworkNode) entry.getNode();
            for (ItemNetworkNode.ItemConnection connection : node.connections) {
                if (connection.canInsert()) {
                    if (connection.cache == null) {
                        connection.cache = BlockApiCache.create(ItemStorage.SIDED, world, entry.getPos().relative(connection.direction));
                    }
                    Storage<ItemVariant> target = connection.cache.find(connection.direction.getOpposite());
                    if (target != null && target.supportsInsertion()) {
                        PriorityBucket bucket = priorityBuckets.computeIfAbsent(connection.insertPriority, PriorityBucket::new);
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

        List<Aggregate> targets = new ArrayList<>();
        Random random = ThreadLocalRandom.current();

        for (PriorityBucket pb : sortedBuckets) {
            int whitelistSize = pb.whitelist.size();
            int blacklistSize = pb.blacklist.size();
            if (whitelistSize > 0) {
                Collections.shuffle(pb.whitelist);
                targets.add(new WhitelistAggregate(pb.priority, pb.whitelist));
            }
            if (blacklistSize > 0) {
                Collections.shuffle(pb.blacklist);
                targets.add(new BlacklistAggregate(pb.priority, pb.blacklist));
            }

            // Ensure equal chance to receive items on average.
            if (whitelistSize > 0 && blacklistSize > 0) {
                if (random.nextDouble() >= (double) whitelistSize / (whitelistSize + blacklistSize)) {
                    Collections.swap(targets, targets.size() - 2, targets.size() - 1);
                }
            }
        }

        return targets;
    }

    private static class PriorityBucket {
        private final int priority;
        private final List<InsertTarget> whitelist = new ArrayList<>();
        private final List<InsertTarget> blacklist = new ArrayList<>();

        private PriorityBucket(int priority) {
            this.priority = priority;
        }
    }

    private interface Aggregate extends InsertionOnlyStorage<ItemVariant> {
        int getPriority();

        @Override
        default Iterator<StorageView<ItemVariant>> iterator() {
            return Collections.emptyIterator();
        }
    }

    private static class WhitelistAggregate implements Aggregate {
        private final int priority;
        // Used when the inserted item doesn't have NBT
        private final Map<Item, List<Storage<ItemVariant>>> map = new IdentityHashMap<>();
        // Used when the inserted item has NBT.
        private final List<InsertTarget> targets;

        WhitelistAggregate(int priority, List<InsertTarget> targets) {
            this.priority = priority;
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
        public int getPriority() {
            return priority;
        }
    }

    private static class BlacklistAggregate implements Aggregate {
        private final int priority;
        private final List<InsertTarget> targets;

        private BlacklistAggregate(int priority, List<InsertTarget> targets) {
            this.priority = priority;
            this.targets = targets;
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            return insertTargets(targets, resource, maxAmount, transaction);
        }

        @Override
        public int getPriority() {
            return priority;
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
