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
package aztech.modern_industrialization.compat.kubejs.machine;

import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import net.minecraft.resources.ResourceLocation;

public interface ShapeTemplateHelper {
    default ShapeTemplate.LayeredBuilder layeredShape(String hatchCasing, String[][] layers) {
        return new ShapeTemplate.LayeredBuilder(MachineCasings.get(hatchCasing), layers);
    }

    default ShapeTemplate.Builder startShape(String hatchCasing) {
        return new ShapeTemplate.Builder(MachineCasings.get(hatchCasing));
    }

    default SimpleMember memberOfBlock(String blockId) {
        return SimpleMember.forBlockId(new ResourceLocation(blockId));
    }

    default HatchFlags noHatch() {
        return HatchFlags.NO_HATCH;
    }

    default HatchFlags hatchOf(String... hatches) {
        var builder = new HatchFlags.Builder();
        for (String hatch : hatches) {
            switch (hatch) {
            case "item_input" -> builder.with(HatchType.ITEM_INPUT);
            case "item_output" -> builder.with(HatchType.ITEM_OUTPUT);
            case "fluid_input" -> builder.with(HatchType.FLUID_INPUT);
            case "fluid_output" -> builder.with(HatchType.FLUID_OUTPUT);
            case "energy_input" -> builder.with(HatchType.ENERGY_INPUT);
            case "energy_output" -> builder.with(HatchType.ENERGY_OUTPUT);
            default -> throw new IllegalArgumentException("Unsupported hatch type: " + hatch);
            }
        }
        return builder.build();
    }
}
