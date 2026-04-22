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

package aztech.modern_industrialization.client.machines.models;

import java.util.List;
import java.util.function.Supplier;

import aztech.modern_industrialization.MI;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.common.util.Lazy;
import org.jspecify.annotations.Nullable;

public record UseBlockModelBakedModel(BlockState targetState,
                                      Supplier<BlockStateModel> stateModel) implements DynamicBlockStateModel {

    // The rest just delegates to stateModel.get()

    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return stateModel.get().createGeometryKey(level, pos, state, random);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        stateModel.get().collectParts(level, pos, state, random, parts);
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return stateModel.get().particleMaterial(level, pos, state);
    }

    @Override
    public Material.Baked particleMaterial() {
        return stateModel.get().particleMaterial();
    }

    @Deprecated
    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return stateModel.get().materialFlags();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return stateModel.get().materialFlags(level, pos, state);
    }

    public record Unbaked(Block block) implements CustomUnbakedBlockStateModel {
        public static final Identifier TYPE_ID = MI.id("use_block_model");

        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(i ->
                i.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Unbaked::block)
                ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public BlockStateModel bake(ModelBaker modelBakery) {
            var targetState = block.defaultBlockState();
            return new UseBlockModelBakedModel(
                    targetState,
                    Lazy.of(() -> Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(targetState)));
        }

        @Override
        public void resolveDependencies(Resolver resolver) {}
    }
}
