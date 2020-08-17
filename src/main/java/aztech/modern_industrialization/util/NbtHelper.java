package aztech.modern_industrialization.util;

import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;

import java.util.Collection;

public class NbtHelper {
    public static Fluid getFluid(CompoundTag tag, String key) {
        return Registry.FLUID.get(tag.getInt(key));
    }
    public static void putFluid(CompoundTag tag, String key, Fluid fluid) {
        tag.putInt("fluid_id", Registry.FLUID.getRawId(fluid));
    }
    public static byte encodeDirections(Collection<Direction> directions) {
        byte mask = 0;
        for(Direction direction : directions) {
            mask |= 1 << direction.getId();
        }
        return mask;
    }
    public static Direction[] decodeDirections(byte mask) {
        Direction[] directions = new Direction[Long.bitCount(mask)];
        int j = 0;
        for(int i = 0; i < 6; ++i) {
            if((mask & (1 << i)) != 0) {
                directions[j++] = Direction.byId(i);
            }
        }
        return directions;
    }
}
