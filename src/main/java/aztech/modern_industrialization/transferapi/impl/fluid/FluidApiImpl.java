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
package aztech.modern_industrialization.transferapi.impl.fluid;

import aztech.modern_industrialization.mixin.BucketItemAccessor;
import aztech.modern_industrialization.transferapi.api.fluid.ItemFluidApi;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.registry.Registry;

public class FluidApiImpl {
    public static void init() {
        // load static, called by the mod initializer
    }

    private static void onFluidRegistered(Fluid fluid) {
        if (fluid == null)
            return;
        Item item = fluid.getBucketItem();

        if (item instanceof BucketItem) {
            BucketItem bucketItem = (BucketItem) item;
            Fluid bucketFluid = ((BucketItemAccessor) bucketItem).getFluid();

            if (fluid == bucketFluid) {
                ItemFluidApi.registerEmptyAndFullItems(Items.BUCKET, FluidKey.of(fluid), FluidConstants.BUCKET, bucketItem);
            }
        }
    }

    static {
        // register bucket compat
        Registry.FLUID.forEach(FluidApiImpl::onFluidRegistered);
        RegistryEntryAddedCallback.event(Registry.FLUID).register((rawId, id, fluid) -> onFluidRegistered(fluid));
    }
}
