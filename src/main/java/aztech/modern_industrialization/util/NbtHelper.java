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

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class NbtHelper {
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

    public static byte[] encodeConnections(@Nullable PipeEndpointType[] connections) {
        byte[] encoded = new byte[6];
        for (int i = 0; i < 6; ++i) {
            PipeEndpointType type = connections[i];
            encoded[i] = type == null ? 127 : (byte) type.getId();
        }
        return encoded;
    }

    public static @Nullable PipeEndpointType[] decodeConnections(byte[] encoded) {
        @Nullable
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (int i = 0; i < 6; ++i) {
            connections[i] = PipeEndpointType.byId(encoded[i]);
        }
        return connections;
    }

    public static <T> void putList(ValueOutput output, String key, List<T> list, Codec<T> elementCodec) {
        var outputList = output.list(key, elementCodec);
        for (T t : list) {
            outputList.add(t);
        }
    }

    public static <T> void getList(ValueInput input, String key, List<T> list, Codec<T> elementCodec) {
        list.clear();
        for (T t : input.listOrEmpty(key, elementCodec)) {
            list.add(t);
        }
    }
}
