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

package aztech.modern_industrialization.test;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.pipes.api.PipeConfigType;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.SavedPipeConfig;
import aztech.modern_industrialization.pipes.item.ItemPipeInterface;
import aztech.modern_industrialization.test.framework.MIGameTest;
import aztech.modern_industrialization.test.framework.MIGameTestHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;

public class ItemPipeTests {
    private final PipeNetworkType itemPipe = PipeNetworkType.get(MI.id("item_pipe"));

    /**
     * An "Insert or Extract" connection with a whitelist must not move its own extractions back into itself.
     * The source's insert priority is higher than the target's, so before the fix the source itself was always
     * the first whitelisted candidate, and the target never received anything.
     */
    @MIGameTest
    public void testWhitelistedInOutConnectionDoesNotInsertIntoItself(MIGameTestHelper helper) {
        var source = new BlockPos(0, 1, 0);
        var target = new BlockPos(2, 1, 0);
        barrel(helper, source, new ItemStack(Items.IRON_INGOT, 32));
        barrel(helper, target);
        helper.pipe(new BlockPos(1, 1, 0), itemPipe, pipe -> {
            pipe.addInOutConnection(Direction.WEST);
            pipe.applyConfig(Direction.WEST, config(PipeEndpointType.BLOCK_IN_OUT, true, 1, 0, new ItemStack(Items.IRON_INGOT)));
            pipe.addInConnection(Direction.EAST);
            pipe.applyConfig(Direction.EAST, config(PipeEndpointType.BLOCK_IN, false, 0, 0));
        });
        helper.startSequence()
                .thenWaitUntil(() -> assertItems(helper, target, Items.IRON_INGOT, 16))
                .thenExecute(() -> assertItems(helper, source, Items.IRON_INGOT, 16))
                .thenSucceed();
    }

    /**
     * Whitelisted insert targets still receive items.
     */
    @MIGameTest
    public void testWhitelistedTargetReceivesItems(MIGameTestHelper helper) {
        var source = new BlockPos(0, 1, 0);
        var target = new BlockPos(2, 1, 0);
        barrel(helper, source, new ItemStack(Items.IRON_INGOT, 32));
        barrel(helper, target);
        helper.pipe(new BlockPos(1, 1, 0), itemPipe, pipe -> {
            pipe.addOutConnection(Direction.WEST);
            pipe.applyConfig(Direction.WEST, config(PipeEndpointType.BLOCK_OUT, false, 0, 0));
            pipe.addInConnection(Direction.EAST);
            pipe.applyConfig(Direction.EAST, config(PipeEndpointType.BLOCK_IN, true, 0, 0, new ItemStack(Items.IRON_INGOT)));
        });
        helper.startSequence()
                .thenWaitUntil(() -> assertItems(helper, target, Items.IRON_INGOT, 16))
                .thenExecute(() -> assertItems(helper, source, Items.IRON_INGOT, 16))
                .thenSucceed();
    }

    private static SavedPipeConfig config(PipeEndpointType type, boolean whitelist, int insertPriority, int extractPriority,
            ItemStack... filter) {
        var stacks = new ArrayList<>(Collections.nCopies(ItemPipeInterface.SLOTS, ItemStack.EMPTY));
        for (int i = 0; i < filter.length; i++) {
            stacks.set(i, filter[i]);
        }
        return new SavedPipeConfig(PipeConfigType.ITEM, type, whitelist, insertPriority, extractPriority, stacks, ItemStack.EMPTY,
                FluidVariant.blank());
    }

    private static void barrel(MIGameTestHelper helper, BlockPos pos, ItemStack... contents) {
        helper.setBlock(pos, Blocks.BARREL);
        var barrel = (BarrelBlockEntity) helper.getBlockEntity(pos);
        for (int i = 0; i < contents.length; i++) {
            barrel.setItem(i, contents[i]);
        }
    }

    private static void assertItems(MIGameTestHelper helper, BlockPos pos, Item item, int count) {
        var barrel = (BarrelBlockEntity) helper.getBlockEntity(pos);
        int actual = barrel.countItem(item);
        if (actual != count) {
            helper.fail("Expected %d %s, got %d".formatted(count, item, actual), pos);
        }
    }
}
