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
import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class CreativeTankBlockEntity extends BlockEntity implements FluidExtractable, BlockEntityClientSerializable {
    FluidKey fluid = FluidKeys.EMPTY;

    public CreativeTankBlockEntity() {
        super(MITanks.CREATIVE_BLOCK_ENTITY_TYPE);
    }

    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");

        if (world != null && world.isClient) {
            ClientWorld clientWorld = (ClientWorld) world;
            WorldRendererGetter wrg = (WorldRendererGetter) clientWorld;
            wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
        }
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
            return this.fluid.withAmount(maxAmount);
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
