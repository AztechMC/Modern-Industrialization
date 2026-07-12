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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * The representation of a simple logic-less member that is part of a shape,
 * e.g. a casing.
 */
public interface SimpleMember {
    boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity);

    @Nullable
    default BlockEntity newBlockEntity(RegistryAccess registries, @Nullable Level level, BlockPos pos, BlockState state) {
        return null;
    }

    BlockState getPreviewState();

    default Ingredient getItemPreviewState(RegistryAccess registries) {
        return Ingredient.of(this.getPreviewState().getBlock().asItem());
    }

    default List<Component> getTooltipPreview(RegistryAccess registries) {
        return List.of(this.getPreviewState().getBlock().getName());
    }

    static SimpleMember forBlock(Supplier<? extends Block> block) {
        Objects.requireNonNull(block);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return state.is(block.get());
            }

            @Override
            public BlockState getPreviewState() {
                return block.get().defaultBlockState();
            }
        };
    }

    static SimpleMember forBlockTag(Supplier<? extends Block> block, TagKey<Block> tag) {
        Objects.requireNonNull(block);
        Objects.requireNonNull(tag);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity) {
                return state.is(tag);
            }

            @Override
            public BlockState getPreviewState() {
                return block.get().defaultBlockState();
            }

            @Override
            public Ingredient getItemPreviewState(RegistryAccess registries) {
                var blocks = BuiltInRegistries.BLOCK.getTagOrEmpty(tag);
                return Ingredient.of(StreamSupport.stream(blocks.spliterator(), false)
                        .map(Holder::value)
                        .map(Block::asItem)
                        .filter((item) -> item != Items.AIR)
                        .map(ItemStack::new));
            }

            @Override
            public List<Component> getTooltipPreview(RegistryAccess registries) {
                List<Component> lines = new ArrayList<>();
                var translation = Tags.getTagTranslationKey(tag);
                var tagText = Component.literal("#" + tag.location());
                if (I18n.exists(translation)) {
                    lines.add(Component.translatable(translation));
                    lines.add(tagText.withStyle(ChatFormatting.DARK_GRAY));
                } else {
                    lines.add(tagText);
                }
                return lines;
            }
        };
    }

    static SimpleMember forBlockId(ResourceLocation id) {
        return forBlock(() -> BuiltInRegistries.BLOCK.get(id));
    }

    static SimpleMember forBlockTagId(ResourceLocation id, ResourceLocation tagId) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tagId);

        var tag = TagKey.create(Registries.BLOCK, tagId);
        return forBlockTag(() -> BuiltInRegistries.BLOCK.get(id), tag);
    }

    static SimpleMember forBlockState(BlockState state) {
        Objects.requireNonNull(state);

        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state2, @Nullable BlockEntity blockEntity) {
                return state == state2;
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
            public BlockState getPreviewState() {
                return Blocks.CHAIN.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
            }
        };
    }
}
