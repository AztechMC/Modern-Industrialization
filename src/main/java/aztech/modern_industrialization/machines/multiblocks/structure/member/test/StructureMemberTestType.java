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
package aztech.modern_industrialization.machines.multiblocks.structure.member.test;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;

public final class StructureMemberTestType<T extends StructureMemberTest> {
    private static final Map<String, StructureMemberTestType<?>> TYPES = Maps.newHashMap();

    public static final StructureMemberTestType<StateStructureMemberTest> STATE = register("state", StateStructureMemberTest.CODEC);
    public static final StructureMemberTestType<TagStructureMemberTest> TAG = register("tag", TagStructureMemberTest.CODEC);

    private static <M extends StructureMemberTest> StructureMemberTestType<M> register(String name, MapCodec<M> codec) {
        var type = new StructureMemberTestType<>(name, codec);
        TYPES.put(name, type);
        return type;
    }

    static StructureMemberTestType<?> getType(String name) {
        var type = TYPES.get(name);
        if (type == null) {
            throw new IllegalArgumentException("No type could be found for the name \"" + name + "\"");
        }
        return type;
    }

    private final String name;
    private final MapCodec<T> codec;

    public StructureMemberTestType(String name, MapCodec<T> codec) {
        this.name = name;
        this.codec = codec;
    }

    public String name() {
        return name;
    }

    public MapCodec<T> codec() {
        return codec;
    }
}
