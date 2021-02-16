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
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import java.io.IOException;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;

/**
 * A fluid that can only be used for crafting, i.e. not be placed in the world.
 */
public class CraftingFluid extends Fluid {
    public final Item bucketItem;
    public final String name;
    public final int color;
    public final boolean isGas;
    private final CraftingFluidBlock block;

    public CraftingFluid(String name, int color, boolean isGas) {
        bucketItem = new BucketItem(this, new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1).group(ModernIndustrialization.ITEM_GROUP));
        this.name = name;
        this.block = new CraftingFluidBlock(name, color);
        this.color = color;
        this.isGas = isGas;
        MIFluids.FLUIDS.add(this);
    }

    public CraftingFluid(String name, int color) {
        this(name, color, false);
    }

    @Override
    public Item getBucketItem() {
        return bucketItem;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return true;
    }

    @Override
    protected Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        return Vec3d.ZERO;
    }

    @Override
    public int getTickRate(WorldView world) {
        return 0;
    }

    @Override
    protected float getBlastResistance() {
        return 0;
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public float getHeight(FluidState state) {
        return 0;
    }

    @Override
    protected BlockState toBlockState(FluidState state) {
        return block.getDefaultState();
    }

    @Override
    public boolean isStill(FluidState state) {
        return true;
    }

    @Override
    public int getLevel(FluidState state) {
        return 0;
    }

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        return null;
    }

    public void registerTextures(TextureManager tm) {
        String path = "modern_industrialization:textures/fluid/";

        for (String alt : new String[] { "" }) {

            String bucket = path + String.format("bucket%s.png", alt);
            String bucket_content = path + String.format("bucket_content%s.png", alt);

            try {
                NativeImage bucket_image = tm.getAssetAsTexture(bucket);
                NativeImage bucket_content_image = tm.getAssetAsTexture(bucket_content);
                TextureHelper.colorize(bucket_content_image, color);
                TextureHelper.blend(bucket_image, bucket_content_image);
                if (isGas) {
                    TextureHelper.flip(bucket_image);
                }
                tm.addTexture(String.format("modern_industrialization:textures/items/bucket/%s.png", name), bucket_image);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
