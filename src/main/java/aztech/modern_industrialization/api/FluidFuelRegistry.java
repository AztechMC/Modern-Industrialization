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

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import java.util.*;

public class FluidFuelRegistry {
    private static final Map<FluidKey, Integer> fluidBurnTicks = new HashMap<>();

    public static void register(FluidKey fluid, int burnTicks) {
        if (burnTicks <= 0) {
            throw new RuntimeException("Fluids must have a positive burn time!");
        }
        if (fluid == null || fluid.isEmpty()) {
            throw new RuntimeException("May not register a null or empty fluid!");
        }
        if (fluidBurnTicks.containsKey(fluid)) {
            throw new RuntimeException("May not re-register a fluid fuel!");
        }
        fluidBurnTicks.put(fluid, burnTicks);
    }

    /**
     * Get the burn time of a fluid, or 0 if the fluid is not a registered fuel.
     */
    public static int getBurnTicks(FluidKey fluid) {
        return fluidBurnTicks.getOrDefault(fluid, 0);
    }

    public static List<FluidKey> getRegisteredFluids() {
        List<FluidKey> fluids = new ArrayList<>(fluidBurnTicks.keySet());
        fluids.sort(Comparator.comparing(fluidBurnTicks::get));
        return fluids;
    }
}
