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
package aztech.modern_industrialization.definition;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.fluid.MIBucketItem;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.fluid.MIFluidBlock;
import aztech.modern_industrialization.items.SortOrder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;

public class FluidDefinition extends Definition implements FluidLike {

    public static final int LOW_OPACITY = 180;
    public static final int FULL_OPACITY = 255;
    public static final int NEAR_OPACITY = 240;
    public static final int MEDIUM_OPACITY = 230;

    public final MIFluidBlock fluidBlock;
    public final MIFluid fluid;
    private final FluidVariant variant;

    public final int color;
    public final int opacity;

    public final boolean isGas;

    public final FluidTexture fluidTexture;

    public final ItemDefinition<MIBucketItem> bucketItemDefinition;

    public FluidDefinition(String englishName, String id, int color, int opacity, FluidTexture texture, boolean isGas) {
        super(englishName, id);
        this.color = color;
        this.isGas = isGas;

        fluidBlock = new MIFluidBlock(color);
        fluid = new MIFluid(fluidBlock, color);
        bucketItemDefinition = MIItem.item(englishName + " Bucket",
                id + "_bucket", s -> new MIBucketItem(fluid, s), SortOrder.BUCKETS);

        fluid.setBucketItem(bucketItemDefinition.asItem());
        this.variant = FluidVariant.of(fluid);
        this.fluidTexture = texture;
        this.opacity = opacity;

        if (isGas) {
            FluidVariantAttributes.register(fluid, new FluidVariantAttributeHandler() {
                @Override
                public boolean isLighterThanAir(FluidVariant variant) {
                    return true;
                }
            });
        }

    }

    @Override
    public String getTranslationKey() {
        return "block.modern_industrialization." + this.getId().getPath();
    }

    @Override
    public Fluid asFluid() {
        return fluid;
    }

    public BucketItem getBucket() {
        return bucketItemDefinition.asItem();
    }

    public FluidVariant variant() {
        return variant;
    }

}
