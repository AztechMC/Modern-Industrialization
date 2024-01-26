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
package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.MIIdentifier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public final class MachineProcessConditions {
    private static final BiMap<ResourceLocation, Codec<? extends MachineProcessCondition>> MAP = HashBiMap.create();

    public static void register(ResourceLocation id, Codec<? extends MachineProcessCondition> serializer) {
        if (MAP.get(id) != null || MAP.inverse().get(serializer) != null) {
            throw new IllegalArgumentException("Duplicate registration for process condition " + id);
        }

        MAP.put(id, serializer);
    }

    @Nullable
    public static Codec<? extends MachineProcessCondition> get(ResourceLocation id) {
        return MAP.get(id);
    }

    public static ResourceLocation getId(Codec<? extends MachineProcessCondition> serializer) {
        return MAP.inverse().get(serializer);
    }

    static {
        register(new MIIdentifier("dimension"), DimensionProcessCondition.CODEC);
        register(new MIIdentifier("adjacent_block"), AdjacentBlockProcessCondition.CODEC);
        register(new MIIdentifier("biome"), BiomeProcessCondition.CODEC);
        register(new MIIdentifier("custom"), CustomProcessCondition.CODEC);
    }
}
