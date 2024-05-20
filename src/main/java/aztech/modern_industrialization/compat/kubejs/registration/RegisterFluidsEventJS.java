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
package aztech.modern_industrialization.compat.kubejs.registration;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.definition.FluidTexture;
import dev.latvian.mods.kubejs.event.EventJS;

public class RegisterFluidsEventJS extends EventJS {
    public void register(String englishName, String internalName, int color, String texture, boolean isGas, String opacity) {
        color |= 0xff000000; // Color must always be fully opaque

        var fluidTexture = switch (texture) {
        case "lava" -> FluidTexture.LAVA_LIKE;
        case "plasma" -> FluidTexture.PLASMA_LIKE;
        case "steam" -> FluidTexture.STEAM_LIKE;
        case "water" -> FluidTexture.WATER_LIKE;
        default -> throw new IllegalArgumentException("Invalid texture type: " + texture);
        };
        var opacityValue = switch (opacity) {
        case "low" -> FluidDefinition.LOW_OPACITY;
        case "medium" -> FluidDefinition.MEDIUM_OPACITY;
        case "high" -> FluidDefinition.NEAR_OPACITY;
        case "full" -> FluidDefinition.FULL_OPACITY;
        default -> throw new IllegalArgumentException("Invalid opacity type: " + opacity);
        };

        MIFluids.fluid(englishName, internalName, color, opacityValue, fluidTexture, isGas);
    }
}
