package aztech.modern_industrialization.machinesv2.components;

import net.minecraft.nbt.CompoundTag;

public class IsActiveComponent implements IComponent {

    public boolean isActive = false;

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putBoolean("isActive", isActive);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        isActive = tag.getBoolean("isActive");
    }

    @Override
    public boolean isClientSynced(){
        return true;
    }
}
