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

package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public interface NuclearComponent<T extends TransferVariant> {
    double getHeatConduction();

    NeutronBehaviour getNeutronBehaviour();

    T getVariant();

    @Nullable
    default T getNeutronProduct() {
        return null;
    }

    default long getNeutronProductAmount() {
        return 0;
    }

    default double getNeutronProductProbability() {
        return 1;
    }

    default int getMaxTemperature() {
        return Integer.MAX_VALUE;
    }

    static ResourceLocation getEmiRecipeId(NuclearComponent<?> component, String category, String type) {
        return switch (component.getVariant()) {
            case ItemVariant itemVariant -> BuiltInRegistries.ITEM.getKey(itemVariant.getItem()).withPrefix("/" + category + "/item/").withSuffix("/" + type);
            case FluidVariant fluidVariant -> BuiltInRegistries.FLUID.getKey(fluidVariant.getFluid()).withPrefix("/" + category + "/fluid/").withSuffix("/" + type);
            case Object object -> throw new IllegalArgumentException("Unknown component variant " + object);
        };
    }
}
