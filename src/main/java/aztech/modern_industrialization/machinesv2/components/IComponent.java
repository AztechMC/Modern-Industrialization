package aztech.modern_industrialization.machinesv2.components;

import net.minecraft.nbt.CompoundTag;

public interface IComponent {

    void writeNbt(CompoundTag tag);

    void readNbt(CompoundTag tag);

    default boolean isClientSynced(){
        return false;
    }

    default boolean forceRemesh(){
        return isClientSynced();
    }
}
