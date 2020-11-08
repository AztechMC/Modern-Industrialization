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
package aztech.modern_industrialization.blocks.creativetank;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.*;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.ConstantFluidFilter;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class CreativeTankBlockEntity extends BlockEntity implements FluidExtractable, BlockEntityClientSerializable {
    FluidKey fluid = FluidKeys.EMPTY;
    static final FluidAmount MAX_OUTPUT = FluidAmount.of(Integer.MAX_VALUE, 1000);

    public CreativeTankBlockEntity() {
        super(MITanks.CREATIVE_BLOCK_ENTITY_TYPE);
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.put("fluid", fluid.toTag());
        return tag;
    }

    public void onChanged() {
        markDirty();
        if (!world.isClient)
            sync();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        toClientTag(tag);
        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        fromClientTag(tag);
        super.fromTag(state, tag);
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        if (!this.fluid.isEmpty() && filter.matches(this.fluid)) {
            return this.fluid.withAmount(maxAmount.min(MAX_OUTPUT));
        }
        return FluidVolumeUtil.EMPTY;
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Reference<ItemStack> heldStackRef = new Reference<ItemStack>() {
            @Override
            public ItemStack get() {
                return player.inventory.getMainHandStack();
            }

            @Override
            public boolean set(ItemStack value) {
                if (PlayerInventory.isValidHotbarIndex(player.inventory.selectedSlot)) {
                    player.inventory.main.set(player.inventory.selectedSlot, value);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean isValid(ItemStack value) {
                return true;
            }
        };
        LimitedConsumer<ItemStack> excessConsumer = (itemStack, simulation) -> {
            if (simulation.isAction()) {
                player.inventory.offerOrDrop(player.world, itemStack);
            }
            return true;
        };
        // Try to set fluid
        if (fluid.isEmpty()) {
            fluid = FluidAttributes.EXTRACTABLE.get(heldStackRef, excessConsumer)
                    .attemptExtraction(ConstantFluidFilter.ANYTHING, FluidAmount.ABSOLUTE_MAXIMUM, Simulation.SIMULATE).getFluidKey();
            onChanged();
            if (!fluid.isEmpty())
                return true;
        }
        // Try to insert into held item
        if (!fluid.isEmpty()) {
            FluidInsertable insertable = FluidAttributes.INSERTABLE.get(heldStackRef, excessConsumer);
            FluidAmount leftover = insertable.insert(this.fluid.withAmount(FluidAmount.ABSOLUTE_MAXIMUM)).amount();
            return !leftover.equals(FluidAmount.ABSOLUTE_MAXIMUM);
        }
        return false;
    }
}
