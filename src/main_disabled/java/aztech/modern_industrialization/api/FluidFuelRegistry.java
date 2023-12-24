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
package aztech.modern_industrialization.api;

import aztech.modern_industrialization.definition.FluidLike;
import java.util.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class FluidFuelRegistry {
    private static final Map<ResourceLocation, Integer> fluidEus = new HashMap<>();

    public static void register(FluidLike fluid, int eu) {
        register(fluid.asFluid(), eu);
    }

    public static void register(Fluid fluid, int eu) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new RuntimeException("May not register a null or empty fluid!");
        }
        register(BuiltInRegistries.FLUID.getKey(fluid), eu);
    }

    public static void register(ResourceLocation fluidId, int eu) {
        Objects.requireNonNull(fluidId);
        if (eu <= 0) {
            throw new RuntimeException("Fluids must have a positive eu amount!");
        }
        if (fluidEus.containsKey(fluidId)) {
            throw new RuntimeException("May not re-register a fluid fuel!");
        }
        fluidEus.put(fluidId, eu);
    }

    /**
     * Get the burn time of a fluid, or 0 if the fluid is not a registered fuel.
     */
    public static int getEu(Fluid fluid) {
        return fluidEus.getOrDefault(BuiltInRegistries.FLUID.getKey(fluid), 0);
    }

    public static List<Fluid> getRegisteredFluids() {
        List<ResourceLocation> fluidIds = new ArrayList<>(fluidEus.keySet());
        fluidIds.sort(Comparator.comparing(fluidEus::get));
        return fluidIds.stream().map(BuiltInRegistries.FLUID::get).filter(fluid -> fluid != Fluids.EMPTY).toList();
    }
}