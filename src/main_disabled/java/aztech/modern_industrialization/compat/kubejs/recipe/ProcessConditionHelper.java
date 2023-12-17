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

import aztech.modern_industrialization.machines.recipe.condition.AdjacentBlockProcessCondition;
import aztech.modern_industrialization.machines.recipe.condition.BiomeProcessCondition;
import aztech.modern_industrialization.machines.recipe.condition.CustomProcessCondition;
import aztech.modern_industrialization.machines.recipe.condition.DimensionProcessCondition;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public interface ProcessConditionHelper {
    ProcessConditionHelper processCondition(MachineProcessCondition condition);

    default ProcessConditionHelper dimension(ResourceLocation dimension) {
        return processCondition(new DimensionProcessCondition(dimension));
    }

    default ProcessConditionHelper adjacentBlock(Block block, String relativePosition) {
        return processCondition(new AdjacentBlockProcessCondition(block, relativePosition));
    }

    default ProcessConditionHelper biome(ResourceLocation biome) {
        return processCondition(new BiomeProcessCondition(biome));
    }

    default ProcessConditionHelper customCondition(String id) {
        return processCondition(new CustomProcessCondition(id));
    }
}
