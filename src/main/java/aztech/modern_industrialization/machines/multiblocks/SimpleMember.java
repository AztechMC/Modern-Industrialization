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

package aztech.modern_industrialization.machines.multiblocks;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The representation of a simple logic-less member that is part of a shape,
 * e.g. a casing.
 */
public interface SimpleMember {
    boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity);

    @Nullable
    BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state);

    BlockState getPreviewState();

    default ItemStack getItemPreviewState(RegistryAccess registries) {
        return this.getPreviewState().getBlock().asItem().getDefaultInstance();
    }

    static SimpleMember forBlock(Supplier<? extends Block> block) {
        Objects.requireNonNull(block);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return state.is(block.get());
            }

            @Override
            public @Nullable BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state) {
                return null;
            }

            @Override
            public BlockState getPreviewState() {
                return block.get().defaultBlockState();
            }
        };
    }

    static SimpleMember forBlockId(ResourceLocation id) {
        return forBlock(() -> BuiltInRegistries.BLOCK.get(id));
    }

    static SimpleMember forBlockTag(ResourceLocation id, ResourceLocation tagId) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tagId);

        var tag = TagKey.create(Registries.BLOCK, tagId);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return state.is(tag);
            }

            @Override
            public @Nullable BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state) {
                return null;
            }

            @Override
            public BlockState getPreviewState() {
                return BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            }
        };
    }

    static SimpleMember forBlockState(BlockState state) {
        Objects.requireNonNull(state);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state2, @Nullable BlockEntity blockEntity) {
                return state == state2;
            }

            @Override
            public @Nullable BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state) {
                return null;
            }

            @Override
            public BlockState getPreviewState() {
                return state;
            }
        };
    }

    static SimpleMember verticalChain() {
        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return state.is(Blocks.CHAIN) && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y;
            }

            @Override
            public @Nullable BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state) {
                return null;
            }

            @Override
            public BlockState getPreviewState() {
                return Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            }
        };
    }
}
