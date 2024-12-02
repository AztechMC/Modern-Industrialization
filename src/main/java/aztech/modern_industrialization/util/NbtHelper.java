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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class NbtHelper {
    public static void putFluid(CompoundTag tag, String key, FluidVariant fluid, HolderLookup.Provider registries) {
        CompoundTag savedTag = new CompoundTag();
        savedTag.put("fk", fluid.toNbt(registries));
        tag.put(key, savedTag);
    }

    public static Item getItem(CompoundTag tag, String key) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString(key)));
    }

    public static void putItem(CompoundTag tag, String key, Item item) {
        tag.putString(key, BuiltInRegistries.ITEM.getKey(item).toString());
    }

    public static byte encodeDirections(Iterable<Direction> directions) {
        byte mask = 0;
        for (Direction direction : directions) {
            mask |= 1 << direction.get3DDataValue();
        }
        return mask;
    }

    public static Direction[] decodeDirections(byte mask) {
        Direction[] directions = new Direction[Long.bitCount(mask)];
        int j = 0;
        for (int i = 0; i < 6; ++i) {
            if ((mask & (1 << i)) != 0) {
                directions[j++] = Direction.from3DDataValue(i);
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

    public static <T> void putList(CompoundTag tag, String key, List<T> list, Function<T, CompoundTag> encoder) {
        ListTag listTag = new ListTag();
        for (T t : list) {
            listTag.add(encoder.apply(t));
        }
        tag.put(key, listTag);
    }

    public static <T> void getList(CompoundTag tag, String key, List<T> list, Function<CompoundTag, T> decoder) {
        list.clear();
        ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag elementTag = listTag.getCompound(i);
            list.add(decoder.apply(elementTag));
        }
    }

    public static int[] encodeBlockPos(BlockPos pos) {
        return new int[] { pos.getX(), pos.getY(), pos.getZ() };
    }

    public static BlockPos decodeBlockPos(int[] pos) {
        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    public static void putBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.putIntArray(key, encodeBlockPos(pos));
        }
    }

    public static BlockPos getBlockPos(CompoundTag tag, String key) {
        return tag.contains(key) ? decodeBlockPos(tag.getIntArray(key)) : null;
    }

    public static void putBlockPosList(CompoundTag tag, String key, List<BlockPos> list) {
        ListTag listTag = new ListTag();
        for (BlockPos pos : list) {
            listTag.add(new IntArrayTag(encodeBlockPos(pos)));
        }
        tag.put(key, listTag);
    }

    public static void getBlockPosList(CompoundTag tag, String key, List<BlockPos> list) {
        list.clear();
        ListTag listTag = tag.getList(key, Tag.TAG_INT_ARRAY);
        for (Tag entry : listTag) {
            list.add(decodeBlockPos(((IntArrayTag) entry).getAsIntArray()));
        }
    }

    public static FluidVariant getFluidCompatible(CompoundTag tag, String key, HolderLookup.Provider registries) {
        if (tag == null || !tag.contains(key))
            return FluidVariant.blank();

        if (tag.get(key) instanceof StringTag) {
            return FluidVariant.of(BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString(key))));
        } else {
            CompoundTag compound = tag.getCompound(key);
            if (compound.contains("fk")) {
                return FluidVariant.fromNbt(compound.getCompound("fk"), registries);
            } else {
                return FluidVariant.of(readLbaTag(tag.getCompound(key)));
            }
        }
    }

    private static Fluid readLbaTag(CompoundTag tag) {
        if (tag.contains("ObjName") && tag.getString("Registry").equals("f")) {
            return BuiltInRegistries.FLUID.get(ResourceLocation.parse(tag.getString("ObjName")));
        } else {
            return Fluids.EMPTY;
        }
    }

    public static void putNonzeroInt(CompoundTag tag, String key, int i) {
        if (i == 0) {
            tag.remove(key);
        } else {
            tag.putInt(key, i);
        }
    }

    /**
     * A more reasonable version of NbtUtils#compareNbt that compares numeric tags without requiring they be of the same type.
     */
    public static boolean equals(@Nullable Tag a, @Nullable Tag b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return switch (a) {
        case CompoundTag tagA -> {
            if (b instanceof CompoundTag tagB) {
                if (tagA.size() != tagB.size()) {
                    yield false;
                }
                Set<String> allKeys = new HashSet<>();
                allKeys.addAll(tagA.getAllKeys());
                allKeys.addAll(tagB.getAllKeys());
                for (String key : allKeys) {
                    Tag valueA = tagA.get(key);
                    Tag valueB = tagB.get(key);
                    if (!equals(valueA, valueB)) {
                        yield false;
                    }
                }
                yield true;
            }
            yield false;
        }
        case ListTag tagA -> {
            if (b instanceof ListTag tagB) {
                if (tagA.size() != tagB.size()) {
                    yield false;
                }
                for (int i = 0; i < tagA.size(); i++) {
                    Tag valueA = tagA.get(i);
                    Tag valueB = tagB.get(i);
                    if (!equals(valueA, valueB)) {
                        yield false;
                    }
                }
                yield true;
            }
            yield false;
        }
        case NumericTag tagA -> {
            if (b instanceof NumericTag tagB) {
                if (a instanceof DoubleTag || a instanceof FloatTag || b instanceof DoubleTag || b instanceof FloatTag) {
                    yield tagA.getAsDouble() == tagB.getAsDouble();
                } else if (a instanceof LongTag || b instanceof LongTag) {
                    yield tagA.getAsLong() == tagB.getAsLong();
                } else {
                    yield tagA.getAsInt() == tagB.getAsInt();
                }
            }
            yield false;
        }
        default -> a.equals(b);
        };
    }
}
