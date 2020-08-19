package aztech.modern_industrialization.util;

import aztech.modern_industrialization.pipes.api.PipeConnectionType;
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
    public static byte encodeDirections(Iterable<Direction> directions) {
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
    public static byte[] encodeConnections(PipeConnectionType[] connections) {
        byte[] encoded = new byte[6];
        for(int i = 0; i < 6; ++i) {
            PipeConnectionType type = connections[i];
            encoded[i] = type == null ? 127 : (byte)type.getId();
        }
        return encoded;
    }
    public static PipeConnectionType[] decodeConnections(byte[] encoded) {
        PipeConnectionType[] connections = new PipeConnectionType[6];
        for(int i = 0; i < 6; ++i) {
            connections[i] = PipeConnectionType.byId(encoded[i]);
        }
        return connections;
    }
}
