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
package aztech.modern_industrialization.items;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

public interface DynamicToolItem {

    ItemStack SHEAR_STACK = new ItemStack(Items.SHEARS, 1);

    @ApiStatus.NonExtendable
    default boolean isSupportedBlock(ItemStack stack, BlockState state) {
        return stack.is(ItemTags.AXES) && state.is(BlockTags.MINEABLE_WITH_AXE)
                || stack.is(ItemTags.PICKAXES) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
                || stack.is(ItemTags.SHOVELS) && state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        // TODO NEO
//                || stack.is(Tags.Items.SHEARS) &&
//                        (state.is(FabricMineableTags.SHEARS_MINEABLE) || Items.SHEARS.getDestroySpeed(
//                                SHEAR_STACK, state) > 1.0f)
//                || stack.is(ItemTags.SWORDS) && state.is(FabricMineableTags.SWORD_MINEABLE);
    }

}
