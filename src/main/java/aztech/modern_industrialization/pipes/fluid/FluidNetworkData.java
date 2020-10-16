package aztech.modern_industrialization.pipes.fluid;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.util.NbtHelper;
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
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.put("fluid", fluid.toTag());
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluidNetworkData) {
            FluidNetworkData otherData = (FluidNetworkData) obj;
            return otherData.fluid == fluid;
        } else {
            return false;
        }
    }
}
