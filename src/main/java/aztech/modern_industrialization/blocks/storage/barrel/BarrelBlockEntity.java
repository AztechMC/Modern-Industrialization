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
package aztech.modern_industrialization.blocks.storage.barrel;

import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends AbstractStorageBlockEntity<ItemVariant> {

    private final long stackCapacity;

    public BarrelBlockEntity(BlockEntityType type, BlockPos pos, BlockState state, long stackCapacity) {
        super(type, pos, state);
        this.stackCapacity = stackCapacity;
    }

    @Override
    public ItemVariant getBlankResource() {
        return ItemVariant.blank();
    }

    @Override
    public long getCapacityForResource(ItemVariant resource) {
        return stackCapacity * resource.getItem().getMaxStackSize();
    }

    @Override
    public ItemVariant loadResource(CompoundTag tag) {
        return ItemVariant.fromNbt(tag.getCompound("item"));
    }

    @Override
    public void saveResource(ItemVariant resource, CompoundTag tag) {
        tag.put("item", resource.toNbt());
    }
}
