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
package aztech.modern_industrialization.blocks.storage;

import aztech.modern_industrialization.MIBlock;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class AbstractStorageBlock extends MIBlock {
    public AbstractStorageBlock(String id, Settings settings, Function<MIBlock, BlockItem> blockItemCtor, int registrationFlag) {
        super(id, settings, blockItemCtor, registrationFlag);
    }

    public AbstractStorageBlock(String id, Settings settings, Function<MIBlock, BlockItem> blockItemCtor) {
        super(id, settings, blockItemCtor);
    }

    protected ItemStack getStack(BlockEntity entity) {
        var storageBlockEntity = (AbstractStorageBlockEntity<?>) entity;
        ItemStack stack = new ItemStack(asItem());
        if (!storageBlockEntity.isEmpty()) {
            NbtCompound tag = new NbtCompound();
            tag.put("BlockEntityTag", storageBlockEntity.toClientTag(new NbtCompound()));
            stack.setNbt(tag);
        }
        return stack;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        return Arrays.asList(getStack(lootContext.get(LootContextParameters.BLOCK_ENTITY)));
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return getStack(world.getBlockEntity(pos));
    }
}
