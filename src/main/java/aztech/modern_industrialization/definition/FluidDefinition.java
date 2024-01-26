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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.fluid.MIBucketItem;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.fluid.MIFluidBlock;
import aztech.modern_industrialization.fluid.MIFluidType;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

public class FluidDefinition extends Definition implements FluidLike {

    public static final int LOW_OPACITY = 180;
    public static final int FULL_OPACITY = 255;
    public static final int NEAR_OPACITY = 240;
    public static final int MEDIUM_OPACITY = 230;

    private final DeferredHolder<Fluid, MIFluid> fluid;
    private DeferredBlock<MIFluidBlock> fluidBlock;
    private ItemDefinition<MIBucketItem> bucketItemDefinition;
    private DeferredHolder<FluidType, MIFluidType> fluidType;

    public final int color;
    public final int opacity;

    public final boolean isGas;

    public final FluidTexture fluidTexture;

    public FluidDefinition(String englishName, String id, int color, int opacity, FluidTexture texture, boolean isGas) {
        super(englishName, id);
        this.color = color;
        this.isGas = isGas;

        fluid = MIFluids.FLUIDS.register(id, () -> new MIFluid(fluidBlock, bucketItemDefinition, fluidType, color));
        fluidBlock = MIBlock.BLOCKS.register(id, () -> new MIFluidBlock(color));
        bucketItemDefinition = MIItem.item(englishName + " Bucket",
                id + "_bucket", s -> new MIBucketItem(fluid.get(), color, s), SortOrder.BUCKETS);
        fluidType = MIFluids.FLUID_TYPES.register(id,
                () -> {
                    var props = FluidType.Properties.create()
                            .descriptionId(fluidBlock.get().getDescriptionId());
                    if (isGas) {
                        props.density(-1000); // Make it lighter than air!
                    }
                    return new MIFluidType(fluidBlock, props);
                });

        this.fluidTexture = texture;
        this.opacity = opacity;
    }

    @Override
    public String getTranslationKey() {
        return "block.modern_industrialization." + this.getId().getPath();
    }

    @Override
    public Fluid asFluid() {
        return fluid.get();
    }

    public MIFluidBlock asFluidBlock() {
        return fluidBlock.get();
    }

    public BucketItem getBucket() {
        return bucketItemDefinition.asItem();
    }

    public FluidVariant variant() {
        return FluidVariant.of(asFluid());
    }

}
