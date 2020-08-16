package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.fluid.FluidNbtHelper;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;

public class FluidNetworkData extends PipeNetworkData {
    Fluid fluid;
    int nodeCapacity;

    public FluidNetworkData(Fluid fluid, int nodeCapacity) {
        this.fluid = fluid;
        this.nodeCapacity = nodeCapacity;
    }

    @Override
    public FluidNetworkData clone() {
        return new FluidNetworkData(fluid, nodeCapacity);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        fluid = FluidNbtHelper.getFluid(tag, "fluid");
        nodeCapacity = tag.getInt("nodeCapacity");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        FluidNbtHelper.putFluid(tag, "fluid", fluid);
        tag.putInt("nodeCapacity", nodeCapacity);
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FluidNetworkData) {
            FluidNetworkData otherData = (FluidNetworkData)obj;
            return otherData.fluid.equals(fluid) && otherData.nodeCapacity == nodeCapacity;
        } else {
            return false;
        }
    }
}
