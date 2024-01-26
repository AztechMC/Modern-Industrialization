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

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageUtil;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class AbstractStorageBlock<T extends TransferVariant<?>> extends Block implements EntityBlock {

    public final EntityBlock factory;
    public final StorageBehaviour<T> behavior;

    public AbstractStorageBlock(Properties settings, EntityBlock factory, StorageBehaviour<T> behaviour) {
        super(settings);
        this.factory = factory;
        this.behavior = behaviour;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.newBlockEntity(pos, state);
    }

    protected ItemStack getStack(BlockEntity entity) {
        var storageBlockEntity = (AbstractStorageBlockEntity<?>) entity;
        ItemStack stack = new ItemStack(asItem());
        if (!storageBlockEntity.isEmpty() || storageBlockEntity.isLocked()) {
            CompoundTag tag = new CompoundTag();
            tag.put("BlockEntityTag", storageBlockEntity.saveWithoutMetadata());
            stack.setTag(tag);
        }
        return stack;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(getStack(builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return getStack(world.getBlockEntity(pos));
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return !behavior.isCreative();
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof AbstractStorageBlockEntity<?>storageBlockEntity) {
            return StorageUtil.calculateComparatorOutput(storageBlockEntity);
        }
        return 0;
    }
}
