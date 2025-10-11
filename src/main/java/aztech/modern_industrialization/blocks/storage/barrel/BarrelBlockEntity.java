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

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockEntity;
import aztech.modern_industrialization.blocks.storage.ResourceStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BarrelBlockEntity extends AbstractStorageBlockEntity<ItemVariant> {

    public BarrelBlockEntity(BlockEntityType type,
            BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    @Override
    public DataComponentType<ResourceStorage<ItemVariant>> componentType() {
        return MIComponents.ITEM_STORAGE.get();
    }

    @Override
    public ItemVariant getBlankResource() {
        return ItemVariant.blank();
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        tag.remove("item");
    }

    @Override
    public ItemVariant loadResource(CompoundTag tag, HolderLookup.Provider registries) {
        return ItemVariant.fromNbt(tag.getCompound("item"), registries);
    }

    @Override
    public void saveResource(ItemVariant resource, CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("item", resource.toNbt(registries));
    }
}
