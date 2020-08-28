package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import net.minecraft.nbt.CompoundTag;

// There is no data for item networks, two pipes of the same type can always connect.
public class ItemNetworkData extends PipeNetworkData {
    @Override
    public PipeNetworkData clone() {
        return new ItemNetworkData();
    }

    @Override
    public void fromTag(CompoundTag tag) {

    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemNetworkData;
    }
}
