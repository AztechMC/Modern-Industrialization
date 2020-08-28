package aztech.modern_industrialization.blocks.tank;

import aztech.modern_industrialization.fluid.FluidInventory;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.Direction;

public class TankBlockEntity extends BlockEntity implements FluidInventory {
    private Fluid fluid;
    private int amount;
    private int capacity;

    public TankBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public int insert(Direction direction, Fluid fluid, int maxAmount, boolean simulate) {
        if(this.fluid == Fluids.EMPTY) {
            int ins = Math.min(capacity, maxAmount);
            if(ins > 0 && !simulate) {
                this.fluid = fluid;
                this.amount += ins;
                markDirty();
            }
            return ins;
        } else if(this.fluid == fluid) {
            int ins = Math.min(capacity - amount, maxAmount);
            if(!simulate) {
                amount += ins;
                markDirty();
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
                markDirty();
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
}
