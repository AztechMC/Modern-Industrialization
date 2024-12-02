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
package aztech.modern_industrialization.blocks.structure.controller;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class StructureMultiblockControllerBlockItem extends BlockItem {
    public StructureMultiblockControllerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public static StructureControllerMode getMode(ItemStack stack) {
        if (!stack.is(MIBlock.STRUCTURE_MULTIBLOCK_CONTROLLER.asItem())) {
            return null;
        }

        String modeId = stack.has(DataComponents.BLOCK_ENTITY_DATA)
                ? stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag().getString("mode")
                : "";
        if (!modeId.isEmpty()) {
            try {
                return StructureControllerMode.valueOf(modeId.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return StructureControllerMode.SAVE;
            }
        }
        return StructureControllerMode.SAVE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(DataComponents.BLOCK_ENTITY_DATA)) {
            CompoundTag tag = stack.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();

            var mode = getMode(stack);

            tooltipComponents.add(MIText.StructureMultiblockControllerTooltipMode.text(mode.text().withStyle(MITooltips.HIGHLIGHT_STYLE))
                    .withStyle(MITooltips.DEFAULT_STYLE));

            if (tag.contains("structure_id", Tag.TAG_STRING)) {
                var name = tag.getString("structure_id");
                tooltipComponents.add(MIText.StructureMultiblockControllerTooltipName
                        .text(Component.literal(name).withStyle(MITooltips.HIGHLIGHT_STYLE)).withStyle(MITooltips.DEFAULT_STYLE));
            }

            if (mode == StructureControllerMode.SAVE) {
                if (tag.contains("bounds", Tag.TAG_COMPOUND)) {
                    CompoundTag boundsTag = tag.getCompound("bounds");
                    BlockPos origin = NbtUtils.readBlockPos(boundsTag, "origin").orElseThrow();
                    BlockPos size = NbtUtils.readBlockPos(boundsTag, "size").orElseThrow();
                    var bounds = new StructureControllerBounds(
                            origin.getX(), origin.getY(), origin.getZ(),
                            size.getX(), size.getY(), size.getZ());
                    tooltipComponents.add(MIText.StructureMultiblockControllerTooltipBoundsRelative.text(
                            Component.literal("(%d, %d, %d)".formatted(bounds.x(), bounds.y(), bounds.z())).withStyle(MITooltips.HIGHLIGHT_STYLE))
                            .withStyle(MITooltips.DEFAULT_STYLE));
                    tooltipComponents.add(MIText.StructureMultiblockControllerTooltipBoundsSize.text(Component
                            .literal("(%d, %d, %d)".formatted(bounds.sizeX(), bounds.sizeY(), bounds.sizeZ())).withStyle(MITooltips.HIGHLIGHT_STYLE))
                            .withStyle(MITooltips.DEFAULT_STYLE));
                }

                if (tag.contains("show_bounds")) {
                    var showBounds = tag.getBoolean("show_bounds");
                    tooltipComponents.add(MIText.StructureMultiblockControllerTooltipShowBounds
                            .text(Component.literal(Boolean.toString(showBounds)).withStyle(MITooltips.HIGHLIGHT_STYLE))
                            .withStyle(MITooltips.DEFAULT_STYLE));
                }

                if (tag.contains("include_block_entities")) {
                    var includeBlockEntities = tag.getBoolean("include_block_entities");
                    tooltipComponents.add(MIText.StructureMultiblockControllerTooltipIncludeBlockEntities
                            .text(Component.literal(Boolean.toString(includeBlockEntities)).withStyle(MITooltips.HIGHLIGHT_STYLE))
                            .withStyle(MITooltips.DEFAULT_STYLE));
                }

                if (tag.contains("ignore_nbt_positions", Tag.TAG_LIST)) {
                    List<BlockPos> ignoreNBTPositions = new ArrayList<>();
                    NbtHelper.getBlockPosList(tag, "ignore_nbt_positions", ignoreNBTPositions);
                    if (!ignoreNBTPositions.isEmpty()) {
                        tooltipComponents.add(MIText.StructureMultiblockControllerTooltipIgnoreNBT
                                .text(Component.literal(Integer.toString(ignoreNBTPositions.size())).withStyle(MITooltips.HIGHLIGHT_STYLE))
                                .withStyle(MITooltips.DEFAULT_STYLE));
                    }
                }

                if (tag.contains("weak_nbt_positions", Tag.TAG_LIST)) {
                    List<BlockPos> weakNBTPositions = new ArrayList<>();
                    NbtHelper.getBlockPosList(tag, "weak_nbt_positions", weakNBTPositions);
                    if (!weakNBTPositions.isEmpty()) {
                        tooltipComponents.add(MIText.StructureMultiblockControllerTooltipWeakNBT
                                .text(Component.literal(Integer.toString(weakNBTPositions.size())).withStyle(MITooltips.HIGHLIGHT_STYLE))
                                .withStyle(MITooltips.DEFAULT_STYLE));
                    }
                }
            }
        }
    }
}
