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

package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class HatchTypes {
    private static final Map<ResourceLocation, HatchType> registeredHatches = new HashMap<>();

    public static final HatchType ITEM_INPUT = register(MI.id("item_input"), MIText.ItemInputHatch);
    public static final HatchType ITEM_OUTPUT = register(MI.id("item_output"), MIText.ItemOutputHatch);
    public static final HatchType FLUID_INPUT = register(MI.id("fluid_input"), MIText.FluidInputHatch);
    public static final HatchType FLUID_OUTPUT = register(MI.id("fluid_output"), MIText.FluidOutputHatch);
    public static final HatchType ENERGY_INPUT = register(MI.id("energy_input"), MIText.EnergyInputHatch);
    public static final HatchType ENERGY_OUTPUT = register(MI.id("energy_output"), MIText.EnergyOutputHatch);
    public static final HatchType NUCLEAR_ITEM = register(MI.id("nuclear_item"), MI.id("nuclear_item_hatch"));
    public static final HatchType NUCLEAR_FLUID = register(MI.id("nuclear_fluid"), MI.id("nuclear_fluid_hatch"));
    public static final HatchType LARGE_TANK = register(MI.id("large_tank"), MI.id("large_tank_hatch"));

    public static HatchType register(ResourceLocation id, MutableComponent description) {
        if (registeredHatches.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate hatch type definition: " + id);
        }
        var type = new HatchType(id, description);
        registeredHatches.put(id, type);
        return type;
    }

    public static HatchType register(ResourceLocation id, MIText description) {
        return register(id, description.text());
    }

    /**
     * The block id corresponds to the user-facing description of the hatch type.
     */
    public static HatchType register(ResourceLocation id, ResourceLocation blockId) {
        if (registeredHatches.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate hatch type definition: " + id);
        }
        var type = new HatchType(id, blockId);
        registeredHatches.put(id, type);
        return type;
    }

    public static HatchType get(ResourceLocation id) {
        var type = registeredHatches.get(id);
        if (type != null) {
            return type;
        } else {
            throw new IllegalArgumentException("Hatch type \"" + id + "\" does not exist.");
        }
    }

    public static HatchType get(String name) {
        return get(ResourceLocation.isValidPath(name) ? MI.id(name) : ResourceLocation.parse(name));
    }

    public static Collection<HatchType> values() {
        return registeredHatches.values();
    }
}
