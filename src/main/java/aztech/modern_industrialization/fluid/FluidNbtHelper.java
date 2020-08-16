package aztech.modern_industrialization.fluid;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.registry.Registry;

public class FluidNbtHelper {
    public static Fluid getFluid(CompoundTag tag, String key) {
        return Registry.FLUID.get(tag.getInt(key));
    }
    public static void putFluid(CompoundTag tag, String key, Fluid fluid) {
        tag.putInt("fluid_id", Registry.FLUID.getRawId(fluid));
    }
}
