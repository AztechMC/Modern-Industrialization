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

import aztech.modern_industrialization.MI;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

interface IItemSink {
    static int listMoveAll(List<? extends IItemSink> sinks, ServerLevel world, ExtractionSource target, int sourceSlot, int maxAmount) {
        int moved = 0;

        for (var sink : sinks) {
            moved += sink.moveAll(world, target, sourceSlot, maxAmount - moved);
            if (moved >= maxAmount) {
                break;
            }
        }

        return moved;
    }

    /**
     * Moves as many items as possible from an extraction target to this sink.
     *
     * @param world      current level, for logging in case of errors
     * @param source     item source
     * @param sourceSlot slot in the source to move from
     * @param maxAmount  maximum amount of items to move
     * @return the amount of items moved
     */
    int moveAll(ServerLevel world, ExtractionSource source, int sourceSlot, int maxAmount);

    record HandlerWrapper(IItemHandler handler) implements IItemSink {
        @Override
        public int moveAll(ServerLevel world, ExtractionSource source, int sourceSlot, int maxToMove) {
            IItemHandler sourceHandler = source.storage();
            int moved = 0;

            // Repeated extraction because extractItem limits to max stack size
            while (true) {
                // Simulate first
                var available = sourceHandler.extractItem(sourceSlot, maxToMove - moved, true);
                if (available.isEmpty()) {
                    break;
                }

                int availableCount = available.getCount();
                var simulateLeftover = ItemHandlerHelper.insertItemStacked(handler, available, true);
                int canFit = availableCount - simulateLeftover.getCount();
                if (canFit <= 0) {
                    break;
                }

                // Do!
                var taken = sourceHandler.extractItem(sourceSlot, canFit, false);
                int takenCount = taken.getCount();
                var leftover = ItemHandlerHelper.insertItemStacked(handler, taken, false);

                int movedThisTime = takenCount - leftover.getCount();
                moved += movedThisTime;

                if (!leftover.isEmpty()) {
                    // Be nice and try to give the overflow back
                    leftover = sourceHandler.insertItem(sourceSlot, leftover, false);

                    // rest in pieces, little stacks
                    if (!leftover.isEmpty()) {
                        MI.LOGGER.error("Discarding overflowing item {}, extracted from block at position {} in {}, accessed from {} side",
                                leftover, source.queryPos(), world.dimension(), source.querySide());
                    }

                    break;
                }

                if (movedThisTime == 0) {
                    break;
                }
            }

            return moved;
        }
    }
}
