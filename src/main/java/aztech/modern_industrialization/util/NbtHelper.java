package aztech.modern_industrialization.util;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class NbtHelper {
    public static Fluid getFluid(CompoundTag tag, String key) {
        return tag.contains(key) ? Registry.FLUID.get(new Identifier(tag.getString(key))) : Fluids.EMPTY;
    }
    public static void putFluid(CompoundTag tag, String key, Fluid fluid) {
        tag.putString(key, Registry.FLUID.getId(fluid).toString());
    }
    public static Item getItem(CompoundTag tag, String key) {
        return Registry.ITEM.get(new Identifier(tag.getString(key)));
    }
    public static void putItem(CompoundTag tag, String key, Item item) {
        tag.putString(key, Registry.ITEM.getId(item).toString());
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
    public static byte[] encodeConnections(PipeEndpointType[] connections) {
        byte[] encoded = new byte[6];
        for(int i = 0; i < 6; ++i) {
            PipeEndpointType type = connections[i];
            encoded[i] = type == null ? 127 : (byte)type.getId();
        }
        return encoded;
    }
    public static PipeEndpointType[] decodeConnections(byte[] encoded) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for(int i = 0; i < 6; ++i) {
            connections[i] = PipeEndpointType.byId(encoded[i]);
        }
        return connections;
    }
    public static <T> void putList(CompoundTag tag, String key, List<T> list, BiFunction<T, CompoundTag, CompoundTag> encoder) {
        ListTag listTag = new ListTag();
        for(int i = 0; i < list.size(); ++i) {
            CompoundTag elementTag = new CompoundTag();
            elementTag.putByte("Slot", (byte)i);
            listTag.add(encoder.apply(list.get(i), elementTag));
        }
        tag.put(key, listTag);
    }
    public static <T> void getList(CompoundTag tag, String key, List<T> list, BiConsumer<T, CompoundTag> decoder) {
        ListTag listTag = tag.getList(key, (new CompoundTag()).getType());
        for(int i = 0; i < list.size(); ++i) {
            CompoundTag elementTag = listTag.getCompound(i);
            decoder.accept(list.get(elementTag.getByte("Slot")), elementTag);
        }
    }
    public static void putBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if(pos != null) {
            tag.putIntArray(key, new int[] {pos.getX(), pos.getY(), pos.getZ()});
        }
    }
    public static BlockPos getBlockPos(CompoundTag tag, String key) {
        if(tag.contains(key)) {
            int[] pos = tag.getIntArray(key);
            return new BlockPos(pos[0], pos[1], pos[2]);
        } else {
            return null;
        }
    }
    public static FluidKey getFluidCompatible(CompoundTag tag, String key) {
        if(!tag.contains(key)) return FluidKeys.EMPTY;
        if(tag.get(key) instanceof StringTag) {
            return FluidKeys.get(Registry.FLUID.get(new Identifier(tag.getString(key))));
        } else {
            return FluidKey.fromTag(tag.getCompound(key));
        }
    }
}
