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
package aztech.modern_industrialization.fluid;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.ModernIndustrialization;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributeHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A fluid that can only be used for crafting, i.e. not be placed in the world.
 */
public class CraftingFluid extends Fluid {
    public final Item bucketItem;
    public final String name;
    public final String id;
    public final int color;
    public final boolean isGas;
    public final CraftingFluidBlock block;

    public CraftingFluid(String name, int color, boolean isGas) {
        this.color = color;

        this.name = name;
        this.id = "modern_industrialization:" + name;
        this.isGas = isGas;
        bucketItem = new MIBucketItem(this, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1).tab(ModernIndustrialization.ITEM_GROUP));
        this.block = new CraftingFluidBlock(name, color);

        MIFluids.FLUIDS.add(this);

        if (isGas) {
            FluidVariantAttributes.register(this, new FluidVariantAttributeHandler() {
                @Override
                public boolean isLighterThanAir(FluidVariant variant) {
                    return true;
                }
            });
        }
    }

    public CraftingFluid(String name, int color) {
        this(name, color, false);
    }

    @Override
    public Item getBucket() {
        return bucketItem;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter world, BlockPos pos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    protected Vec3 getFlow(BlockGetter world, BlockPos pos, FluidState state) {
        return Vec3.ZERO;
    }

    @Override
    public int getTickDelay(LevelReader world) {
        return 0;
    }

    @Override
    protected float getExplosionResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState state, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Override
    public float getOwnHeight(FluidState state) {
        return 0;
    }

    @Override
    protected BlockState createLegacyBlock(FluidState state) {
        return block.defaultBlockState();
    }

    @Override
    public boolean isSource(FluidState state) {
        return true;
    }

    @Override
    public int getAmount(FluidState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockGetter world, BlockPos pos) {
        return null;
    }
}
