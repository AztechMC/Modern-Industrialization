package aztech.modern_industrialization.pipes.fluid;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.SimpleFluidKey;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;

public class FluidNetworkData extends PipeNetworkData {
    FluidKey fluid;

    public FluidNetworkData(FluidKey fluid) {
        this.fluid = fluid;
    }

    @Override
    public FluidNetworkData clone() {
        return new FluidNetworkData(fluid);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if(tag.contains("fluid")) { // backwards-compatibility
            fluid = new SimpleFluidKey(new FluidKey.FluidKeyBuilder(NbtHelper.getFluid(tag, "fluid")));
        } else {
            fluid = FluidKey.fromTag(tag.getCompound("fluidkey"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.put("fluidkey", fluid.toTag());
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FluidNetworkData) {
            FluidNetworkData otherData = (FluidNetworkData)obj;
            return otherData.fluid == fluid;
        } else {
            return false;
        }
    }
}
