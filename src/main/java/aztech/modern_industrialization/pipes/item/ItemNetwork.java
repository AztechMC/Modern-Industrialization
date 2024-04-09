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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

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
        List<ExtractionSource> extractionSources = new ArrayList<>();
        for (var entry : iterateTickingNodes()) {
            BlockPos pos = entry.getPos();
            ItemNetworkNode itemNode = (ItemNetworkNode) entry.getNode();
            for (ItemNetworkNode.ItemConnection connection : itemNode.connections) {
                if (connection.canExtract()) {
                    var queryPos = pos.relative(connection.direction);
                    var querySide = connection.direction.getOpposite();

                    var source = world.getCapability(Capabilities.ItemHandler.BLOCK, queryPos, querySide);

                    if (source != null) {
                        extractionSources.add(new ExtractionSource(connection, source, queryPos, querySide));
                    }
                }
            }
        }
        // Lower priority extracts first.
        extractionSources.sort(Comparator.comparing(et -> et.connection().extractPriority));

        // Do the actual transfer.
        var insertTargets = getAggregatedInsertTargets(world);
        lastMovedItems = 0;
        for (ExtractionSource target : extractionSources) {
            // Lower priority extracts first, and pipes can only move items to things that have >= priorities.
            // So we can just pop insert targets at the end of the list if they have a priority smaller than the current extraction target.
            while (insertTargets.size() > 0 && target.connection().extractPriority > insertTargets.get(insertTargets.size() - 1).getPriority()) {
                insertTargets.remove(insertTargets.size() - 1);
            }

            try {
                lastMovedItems += moveAll(world, target, insertTargets, target.connection()::canStackMoveThrough,
                        target.connection().getMoves());
            } catch (Exception exception) {
                var crashReport = CrashReport.forThrowable(exception, "Moving items in a pipe network");
                crashReport.addCategory("Block being extracted from:")
                        .setDetail("Dimension", world.dimension())
                        .setDetail("Position", target.queryPos())
                        .setDetail("Accessed from side", target.querySide());
                throw new ReportedException(crashReport);
            }
        }
    }

    private static int moveAll(ServerLevel world, ExtractionSource target, List<? extends IItemSink> sinks, Predicate<ItemStack> filter,
            int maxToMove) {
        IItemHandler source = target.storage();
        int moved = 0;

        int sourceSlots = source.getSlots();
        for (int i = 0; i < sourceSlots; ++i) {
            // Filter check
            var stack = source.getStackInSlot(i);
            if (stack.isEmpty() || !filter.test(stack)) {
                continue;
            }

            moved += IItemSink.listMoveAll(sinks, world, target, i, maxToMove - moved);
            if (moved >= maxToMove) {
                break;
            }
        }

        return moved;
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
                        connection.cache = BlockCapabilityCache.create(Capabilities.ItemHandler.BLOCK, world,
                                entry.getPos().relative(connection.direction), connection.direction.getOpposite());
                    }
                    var target = connection.cache.getCapability();
                    if (target != null && target.getSlots() > 0) {
                        PriorityBucket bucket = priorityBuckets.computeIfAbsent(connection.insertPriority, PriorityBucket::new);
                        InsertTarget it = new InsertTarget(connection, new IItemSink.HandlerWrapper(target));

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

    private interface Aggregate extends IItemSink {
        int getPriority();
    }

    private static class WhitelistAggregate implements Aggregate {
        private final int priority;
        // Used when the inserted item doesn't have NBT
        private final Map<Item, List<IItemSink>> map = new IdentityHashMap<>();
        // Used when the inserted item has NBT.
        private final List<InsertTarget> targets;

        WhitelistAggregate(int priority, List<InsertTarget> targets) {
            this.priority = priority;
            this.targets = targets;
            for (InsertTarget target : targets) {
                if (target.connection.whitelist) {
                    ItemNetworkNode.ItemConnection conn = target.connection;
                    for (ItemStack stack : conn.stacks) {
                        if (!stack.hasTag()) {
                            map.computeIfAbsent(stack.getItem(), v -> new ArrayList<>()).add(target.target);
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
        public int moveAll(ServerLevel world, ExtractionSource source, int sourceSlot, int maxAmount) {
            var stack = source.storage().getStackInSlot(sourceSlot);

            if (stack.hasTag()) {
                return insertTargets(targets, world, source, sourceSlot, maxAmount);
            }

            List<IItemSink> targets = map.get(stack.getItem());
            if (targets != null) {
                return IItemSink.listMoveAll(targets, world, source, sourceSlot, maxAmount);
            }
            return 0;
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
        public int moveAll(ServerLevel world, ExtractionSource source, int sourceSlot, int maxAmount) {
            return insertTargets(targets, world, source, sourceSlot, maxAmount);
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }

    private static int insertTargets(List<InsertTarget> targets, ServerLevel world, ExtractionSource source, int sourceSlot, int maxAmount) {
        int moved = 0;

        for (InsertTarget target : targets) {
            var stack = source.storage().getStackInSlot(sourceSlot);
            if (stack.isEmpty()) {
                break;
            }

            if (target.connection.canStackMoveThrough(stack)) {
                moved += target.target.moveAll(world, source, sourceSlot, maxAmount - moved);
                if (moved >= maxAmount) {
                    break;
                }
            }
        }

        return moved;
    }

    private record InsertTarget(ItemNetworkNode.ItemConnection connection, IItemSink target) {
    }
}
