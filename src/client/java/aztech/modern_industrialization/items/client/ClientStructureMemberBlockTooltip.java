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
package aztech.modern_industrialization.items.client;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.blocks.structure.member.StructureMemberMode;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlockItem;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.TagStructureMemberTest;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class ClientStructureMemberBlockTooltip implements ClientTooltipComponent {
    private static final ResourceLocation HATCH_ICON_ATLAS = MI.id("textures/gui/tooltip/hatch_icons.png");

    private final StructureMultiblockMemberBlockItem.TooltipData data;

    private final List<Line> lines = new ArrayList<>();

    public ClientStructureMemberBlockTooltip(StructureMultiblockMemberBlockItem.TooltipData data) {
        this.data = data;

        StructureMemberMode mode = data.mode();

        lines.add(new ComponentLine(MIText.StructureMultiblockMemberTooltipMode.text(mode.text().withStyle(MITooltips.HIGHLIGHT_STYLE))
                .withStyle(MITooltips.DEFAULT_STYLE)));

        if (mode == StructureMemberMode.VARIABLE) {
            String name = data.name();
            if (name != null && !name.isEmpty()) {
                lines.add(new ComponentLine(MIText.StructureMultiblockMemberTooltipVariableName
                        .text(Component.literal(name).withStyle(MITooltips.HIGHLIGHT_STYLE)).withStyle(MITooltips.DEFAULT_STYLE)));
            }
        }

        if (mode == StructureMemberMode.SIMPLE || mode == StructureMemberMode.HATCH) {
            lines.add(new ComponentLine(MIText.StructureMultiblockNBTMode.text(data.nbtMode().textTooltip().withStyle(MITooltips.HIGHLIGHT_STYLE))
                    .withStyle(MITooltips.DEFAULT_STYLE)));

            var preview = data.preview();
            if (preview != null) {
                var previewState = preview.state().get();
                if (!previewState.isAir()) {
                    ItemStack previewStack = previewState.getBlock().asItem().getDefaultInstance();
                    lines.add(new IconsLine(MIText.StructureMultiblockMemberTooltipPreview.text().append(" ").withStyle(MITooltips.DEFAULT_STYLE),
                            List.of(new IconsLine.StackEntry(previewStack)), 6, previewStack.getHoverName().copy()));
                }
            }

            List<IconsLine.Entry> members = new ArrayList<>();
            if (data.members() != null) {
                for (StructureMemberTest member : data.members()) {
                    if (member instanceof StateStructureMemberTest stateTest) {
                        Block block = stateTest.blockState().getBlock();
                        ItemStack stack;
                        if (block instanceof LiquidBlock liquidBlock) {
                            stack = liquidBlock.fluid.getBucket().getDefaultInstance();
                        } else {
                            stack = block.asItem().getDefaultInstance();
                            if (stack.isEmpty()) {
                                stack = Items.BUCKET.getDefaultInstance();
                            }
                        }
                        members.add(new IconsLine.StackEntry(stack));
                    } else if (member instanceof TagStructureMemberTest tagTest) {
                        members.add(new IconsLine.TagEntry(tagTest.blockTag()));
                    }
                }
            }
            if (!members.isEmpty()) {
                lines.add(new IconsLine(MIText.StructureMultiblockMemberTooltipMembers.text().append(" ").withStyle(MITooltips.DEFAULT_STYLE),
                        members, 6, null));
            }
        }

        if (mode == StructureMemberMode.HATCH) {
            MachineCasing casing = data.casing();
            if (casing != null) {
                lines.add(new MachineCasingLine(MIText.StructureMultiblockMemberTooltipCasing.text().append(" ").withStyle(MITooltips.DEFAULT_STYLE),
                        casing));
            }

            HatchFlags hatchFlags = data.hatchFlags();
            if (hatchFlags != null && hatchFlags.flags != 0) {
                List<IconsLine.Entry> hatches = new ArrayList<>();
                for (HatchType hatchType : HatchType.values()) {
                    if (hatchFlags.allows(hatchType)) {
                        hatches.add(new IconsLine.HatchEntry(hatchType));
                    }
                }
                lines.add(new IconsLine(MIText.StructureMultiblockMemberTooltipHatches.text().append(" ").withStyle(MITooltips.DEFAULT_STYLE),
                        hatches, 9, null));
            }
        }
    }

    @Override
    public int getHeight() {
        Font font = Minecraft.getInstance().font;
        int height = 0;
        for (Line line : lines) {
            height += line.height(font);
        }
        return height;
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        for (Line line : lines) {
            int lineWidth = line.width(font);
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        return width;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int lineY = y;
        for (Line line : lines) {
            line.renderImage(font, x, lineY, graphics);
            lineY += line.height(font);
        }
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        int lineY = y;
        for (Line line : lines) {
            line.renderText(font, x, lineY, matrix, buffer);
            lineY += line.height(font);
        }
    }

    private interface Line {
        int height(Font font);

        int width(Font font);

        default void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        }

        default void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        }
    }

    private record ComponentLine(@Nullable MutableComponent label, MutableComponent text) implements Line {
        public ComponentLine(MutableComponent text) {
            this(null, text);
        }

        // 200 is the max width used by most tooltips
        private static final int MAX_WIDTH = 200;

        private Component fullText() {
            return label == null ? text : label.append(text);
        }

        private int labelWidth(Font font) {
            return label == null ? 0 : font.width(label);
        }

        @Override
        public int height(Font font) {
            return 9 * font.split(text, MAX_WIDTH - labelWidth(font)).size();
        }

        @Override
        public int width(Font font) {
            return Math.min(font.width(fullText().getVisualOrderText()), MAX_WIDTH);
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
            if (label != null) {
                font.drawInBatch(label, x, y, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            }
            int labelWidth = labelWidth(font);
            int lineY = y;
            for (FormattedCharSequence line : font.split(text, MAX_WIDTH - labelWidth)) {
                int lineX = x + labelWidth;
                font.drawInBatch(line, lineX, lineY, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                lineY += 9;
            }
        }
    }

    private record IconsLine(MutableComponent label, List<Entry> entries, int maxToDisplay, @Nullable MutableComponent suffix) implements Line {

        @Override
        public int height(Font font) {
            return 18;
        }

        @Override
        public int width(Font font) {
            return font.width(label) + (18 * Math.min(maxToDisplay, entries.size()))
                    + (entries.size() > maxToDisplay ? font.width(Component.literal("+ ...")) : 0)
                    + (suffix != null ? font.width(suffix) : 0);
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            int i = 0;
            for (var entry : entries) {
                entry.renderImage(font, x + (i * 18) + font.width(label), y + 1, graphics);
                if (++i >= maxToDisplay) {
                    break;
                }
            }
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
            font.drawInBatch(label, x, y + 5, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
            x += font.width(label);

            if (entries.size() > maxToDisplay) {
                x += (18 * maxToDisplay) + 2;
                Component moreText = Component.literal("+ ...").withStyle(MITooltips.HIGHLIGHT_STYLE);
                font.drawInBatch(moreText, x, y + 5, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
                x += font.width(moreText);
            } else {
                x += (18 * entries.size()) + 2;
            }

            if (suffix != null) {
                font.drawInBatch(suffix.withStyle(MITooltips.HIGHLIGHT_STYLE), x, y + 5, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0,
                        0xF000F0);
            }
        }

        private interface Entry {
            void renderImage(Font font, int x, int y, GuiGraphics graphics);
        }

        private record StackEntry(ItemStack stack) implements Entry {
            @Override
            public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
                RenderHelper.renderAndDecorateItem(graphics, font, stack, x, y);
            }
        }

        private record TagEntry(TagKey<Block> tag) implements Entry {
            @Override
            public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
                var maybeTag = BuiltInRegistries.BLOCK.getTag(tag);
                ItemStack stack;
                if (maybeTag.isPresent()) {
                    var blocks = maybeTag.get();
                    stack = blocks.get(0).value().asItem().getDefaultInstance();
                } else {
                    stack = Items.BARRIER.getDefaultInstance();
                }
                RenderHelper.renderAndDecorateItem(graphics, font, stack, x, y);

                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200);
                graphics.drawString(font, Component.literal("#").withStyle(MITooltips.HIGHLIGHT_STYLE), x, y, 0xFFFFFF, true);
                graphics.pose().popPose();
            }
        }

        private record HatchEntry(HatchType type) implements Entry {
            @Override
            public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
                int u = type.getId() * 16;
                graphics.blit(HATCH_ICON_ATLAS, x, y, u, 0, 16, 16);
            }
        }
    }

    private record MachineCasingLine(Component label, MachineCasing casing) implements Line {
        @Override
        public int height(Font font) {
            return 18;
        }

        @Override
        public int width(Font font) {
            return font.width(label) + 20 + font.width(casing.getDisplayName());
        }

        @Override
        public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
            RenderHelper.renderMachineCasingItem(graphics, casing, x + font.width(label), y + 1);
        }

        @Override
        public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
            font.drawInBatch(label, x, y + 5, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);

            font.drawInBatch(casing.getDisplayName().withStyle(MITooltips.HIGHLIGHT_STYLE), x + font.width(label) + 20, y + 5, -1, true, matrix,
                    buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }
}
