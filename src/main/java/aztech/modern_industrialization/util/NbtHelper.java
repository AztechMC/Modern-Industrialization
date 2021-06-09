/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.util;

import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class NbtHelper {
    public static void putFluid(NbtCompound tag, String key, FluidKey fluid) {
        NbtCompound savedTag = new NbtCompound();
        savedTag.put("fk", fluid.toNbt());
        tag.put(key, savedTag);
    }

    public static Item getItem(NbtCompound tag, String key) {
        return Registry.ITEM.get(new Identifier(tag.getString(key)));
    }

    public static void putItem(NbtCompound tag, String key, Item item) {
        tag.putString(key, Registry.ITEM.getId(item).toString());
    }

    public static byte encodeDirections(Iterable<Direction> directions) {
        byte mask = 0;
        for (Direction direction : directions) {
            mask |= 1 << direction.getId();
        }
        return mask;
    }

    public static Direction[] decodeDirections(byte mask) {
        Direction[] directions = new Direction[Long.bitCount(mask)];
        int j = 0;
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) != 0) {
                directions[j++] = Direction.byId(i);
            }
        }
        return directions;
    }

    public static byte[] encodeConnections(PipeEndpointType[] connections) {
        byte[] encoded = new byte[6];
        for (int i = 0; i < 6; ++i) {
            PipeEndpointType type = connections[i];
            encoded[i] = type == null ? 127 : (byte) type.getId();
        }
        return encoded;
    }

    public static PipeEndpointType[] decodeConnections(byte[] encoded) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (int i = 0; i < 6; ++i) {
            connections[i] = PipeEndpointType.byId(encoded[i]);
        }
        return connections;
    }

    public static <T> void putList(NbtCompound tag, String key, List<T> list, Function<T, NbtCompound> encoder) {
        NbtList listTag = new NbtList();
        for (T t : list) {
            listTag.add(encoder.apply(t));
        }
        tag.put(key, listTag);
    }

    public static <T> void getList(NbtCompound tag, String key, List<T> list, Function<NbtCompound, T> decoder) {
        list.clear();
        NbtList listTag = tag.getList(key, NbtType.COMPOUND);
        for (int i = 0; i < listTag.size(); ++i) {
            NbtCompound elementTag = listTag.getCompound(i);
            list.add(decoder.apply(elementTag));
        }
    }

    public static void putBlockPos(NbtCompound tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.putIntArray(key, new int[] { pos.getX(), pos.getY(), pos.getZ() });
        }
    }

    public static BlockPos getBlockPos(NbtCompound tag, String key) {
        if (tag.contains(key)) {
            int[] pos = tag.getIntArray(key);
            return new BlockPos(pos[0], pos[1], pos[2]);
        } else {
            return null;
        }
    }

    public static FluidKey getFluidCompatible(NbtCompound tag, String key) {
        if (tag == null || !tag.contains(key))
            return FluidKey.empty();

        if (tag.get(key) instanceof NbtString) {
            return FluidKey.of(Registry.FLUID.get(new Identifier(tag.getString(key))));
        } else {
            NbtCompound compound = tag.getCompound(key);
            if (compound.contains("fk")) {
                return FluidKey.fromNbt(compound.getCompound("fk"));
            } else {
                return FluidKey.of(readLbaTag(tag.getCompound(key)));
            }
        }
    }

    private static Fluid readLbaTag(NbtCompound tag) {
        if (tag.contains("ObjName") && tag.getString("Registry").equals("f")) {
            return Registry.FLUID.get(new Identifier(tag.getString("ObjName")));
        } else {
            return Fluids.EMPTY;
        }
    }
}
