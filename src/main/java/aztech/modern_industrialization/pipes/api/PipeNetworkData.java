package aztech.modern_industrialization.pipes.api;

import net.minecraft.nbt.CompoundTag;

public abstract class PipeNetworkData {
    public abstract PipeNetworkData clone();

    public abstract void fromTag(CompoundTag tag);
    public abstract CompoundTag toTag(CompoundTag tag);
}
