package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidContainerItem;
import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;

public class TankBlockEntity extends BlockEntity implements FluidInventory, BlockEntityClientSerializable, RenderAttachmentBlockEntity {
    private Fluid fluid = Fluids.EMPTY;
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
        fluid = NbtHelper.getFluid(tag, "fluid");
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
        NbtHelper.putFluid(tag, "fluid", fluid);
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
    public int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        if(this.fluid == Fluids.EMPTY) {
            int ins = Math.min(capacity, maxAmount);
            if(ins > 0 && !simulate) {
                this.fluid = fluid;
                this.amount += ins;
                onChanged();
            }
            return ins;
        } else if(this.fluid == fluid) {
            int ins = Math.min(capacity - amount, maxAmount);
            if(!simulate) {
                amount += ins;
                onChanged();
            }
            return ins;
        }
        return 0;
    }

    @Override
    public int extract(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        if(this.fluid == fluid) {
            int ext = Math.min(amount, maxAmount);
            if(!simulate) {
                amount -= ext;
                if(amount == 0) this.fluid = Fluids.EMPTY;
                onChanged();
            }
            return ext;
        }
        return 0;
    }

    @Override
    public Fluid[] getExtractableFluids(Direction direction) {
        return fluid == Fluids.EMPTY ? new Fluid[0] : new Fluid[] { fluid };
    }

    @Override
    public boolean canFluidContainerConnect(Direction direction) {
        return true;
    }

    @Override
    public Object getRenderAttachmentData() {
        return new RenderAttachment(fluid, (float) amount / capacity);
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
        ItemStack heldStack = player.inventory.getMainHandStack();
        if (heldStack.getItem() instanceof FluidContainerItem) {
            FluidContainerItem fluidContainer = (FluidContainerItem) heldStack.getItem();
            // Try to extract from held item, then try to insert into held item
            Fluid extractedFluid = fluid;
            if (extractedFluid == Fluids.EMPTY) {
                extractedFluid = fluidContainer.getExtractableFluid(heldStack);
            }
            int ext = fluidContainer.extractFluid(heldStack, extractedFluid, capacity - amount, FluidContainerItem.handPlayerConsumer(player));
            if (ext > 0) {
                amount += ext;
                fluid = extractedFluid;
                onChanged();
                return true;
            } else {
                int ins = fluidContainer.insertFluid(heldStack, fluid, amount, FluidContainerItem.handPlayerConsumer(player));
                if(ins > 0) {
                    amount -= ins;
                    if(amount == 0) this.fluid = Fluids.EMPTY;
                    onChanged();
                    return true;
                }
            }
        }
        return false;
    }
}
