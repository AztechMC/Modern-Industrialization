package aztech.modern_industrialization.blocks.tank;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.fluid.*;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.math.RoundingMode;

public class TankBlockEntity extends BlockEntity implements FluidTransferable, BlockEntityClientSerializable, RenderAttachmentBlockEntity {
    private FluidKey fluid = FluidKeys.EMPTY;
    private int amount;
    private int capacity;

    public TankBlockEntity() {
        super(MITanks.BLOCK_ENTITY_TYPE);
    }

    public boolean isEmpty() {
        return amount == 0;
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
        amount = tag.getInt("amount");
        capacity = tag.getInt("capacity");

        if(world != null && world.isClient) {
            ClientWorld clientWorld = (ClientWorld) world;
            WorldRendererGetter wrg = (WorldRendererGetter) clientWorld;
            wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.put("fluid", fluid.toTag());
        tag.putInt("amount", amount);
        tag.putInt("capacity", capacity);
        return tag;
    }

    public void onChanged() {
        markDirty();
        if(!world.isClient) sync();
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
    public FluidVolume attemptInsertion(FluidVolume fluid, Simulation simulation) {
        int ins = 0;
        if(this.fluid.isEmpty()) {
            ins = Math.min(capacity, fluid.amount().asInt(1000, RoundingMode.FLOOR));
            if(ins > 0 && simulation.isAction()) {
                this.fluid = fluid.getFluidKey();
                this.amount += ins;
                onChanged();
            }
        } else if(this.fluid == fluid.getFluidKey()) {
            ins = Math.min(capacity - amount, fluid.amount().asInt(1000, RoundingMode.FLOOR));
            if(ins > 0 && simulation.isAction()) {
                this.amount += ins;
                onChanged();
            }
        }
        return fluid.getFluidKey().withAmount(fluid.amount().sub(FluidAmount.of(ins, 1000)));
    }

    @Override
    public FluidVolume attemptExtraction(FluidFilter filter, FluidAmount maxAmount, Simulation simulation) {
        if(!this.fluid.isEmpty() && filter.matches(this.fluid)) {
            int ext = Math.min(amount, maxAmount.asInt(1000, RoundingMode.FLOOR));
            FluidKey key = this.fluid;
            if(simulation.isAction()) {
                amount -= ext;
                if(amount == 0) this.fluid = FluidKeys.EMPTY;
                onChanged();
            }
            return key.withAmount(FluidAmount.of(ext, 1000));
        }
        return FluidVolumeUtil.EMPTY;
    }

    @Override
    public Object getRenderAttachmentData() {
        return new RenderAttachment(fluid.getRawFluid(), (float) amount / capacity);
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public static class RenderAttachment {
        public final Fluid fluid;
        public final float fillFraction;

        public RenderAttachment(Fluid fluid, float fillFraction) {
            this.fluid = fluid;
            this.fillFraction = fillFraction;
        }
    }

    public boolean onPlayerUse(PlayerEntity player) {
        Reference<ItemStack> heldStackRef = new Reference<ItemStack>() {
            @Override
            public ItemStack get() {
                return player.inventory.getMainHandStack();
            }

            @Override
            public boolean set(ItemStack value) {
                if(PlayerInventory.isValidHotbarIndex(player.inventory.selectedSlot)) {
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
            if(simulation.isAction()) {
                player.inventory.offerOrDrop(player.world, itemStack);
            }
            return true;
        };
        // Try to extract from held item first
        FluidExtractable extractable = FluidAttributes.EXTRACTABLE.get(heldStackRef, excessConsumer);
        FluidVolume extracted = extractable.extract(fk -> fk == this.fluid || this.fluid.isEmpty(), FluidAmount.of(capacity - amount, 1000));
        int ext = extracted.amount().asInt(1000, RoundingMode.FLOOR);
        if (ext > 0) {
            amount += ext;
            this.fluid = extracted.getFluidKey();
            onChanged();
            return true;
        } else {
            // Otherwise insert into held item
            FluidInsertable insertable = FluidAttributes.INSERTABLE.get(heldStackRef, excessConsumer);
            int leftover = insertable.insert(this.fluid.withAmount(FluidAmount.of(amount, 1000))).amount().asInt(1000, RoundingMode.FLOOR);
            if(leftover != amount) {
                amount = leftover;
                if(amount == 0) {
                    this.fluid = FluidKeys.EMPTY;
                }
                onChanged();
                return true;
            }
        }
        return false;
    }
}
