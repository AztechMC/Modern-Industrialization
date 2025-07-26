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
package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.machines.recipe.condition.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface ProcessConditionHelper {
    ProcessConditionHelper processCondition(MachineProcessCondition condition);

    default ProcessConditionHelper dimension(ResourceLocation dimension) {
        return processCondition(new DimensionProcessCondition(ResourceKey.create(Registries.DIMENSION, dimension)));
    }

    default ProcessConditionHelper adjacentBlock(Block block, String relativePosition) {
        return processCondition(new AdjacentBlockProcessCondition(block, relativePosition));
    }

    default ProcessConditionHelper biome(ResourceLocation biome) {
        return processCondition(new BiomeProcessCondition(Either.left(ResourceKey.create(Registries.BIOME, biome))));
    }

    default ProcessConditionHelper biomeTag(ResourceLocation tag) {
        return processCondition(new BiomeProcessCondition(Either.right(TagKey.create(Registries.BIOME, tag))));
    }

    default ProcessConditionHelper customCondition(String id) {
        return processCondition(new CustomProcessCondition(id));
    }

    default ProcessConditionHelper registeredCondition(JsonElement condition) {
        if (!condition.isJsonObject()) {
            throw new IllegalArgumentException("Parameter must be a JsonObject");
        }

        JsonObject obj = condition.getAsJsonObject();
        if (obj.size() != 1) {
            throw new IllegalArgumentException("Expected only condition ID");
        }

        Map.Entry<String, JsonElement> entry = obj.entrySet().iterator().next();
        String idString = entry.getKey();

        ResourceLocation id = ResourceLocation.tryParse(idString);
        if (id == null) {
            throw new IllegalArgumentException(
                    String.format("'%s' is not registered at MachineProcessConditions. Perhaps you meant to use a customCondition?", idString));
        }

        MapCodec<? extends MachineProcessCondition> codec = MachineProcessConditions.getCodec(id);
        if (codec == null) {
            throw new IllegalArgumentException("'%s' doesn't have a registered codec!".formatted(idString));
        }

        DataResult<MachineProcessCondition> result = codec.codec().decode(JsonOps.INSTANCE, entry.getValue())
                .map(Pair::getFirst);

        return processCondition(result.getOrThrow(p -> {
            throw new IllegalArgumentException("Couldn't parse '%s': %s".formatted(idString, p));
        }));
    }
}
