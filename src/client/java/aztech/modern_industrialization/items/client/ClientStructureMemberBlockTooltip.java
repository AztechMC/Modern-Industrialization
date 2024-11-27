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

import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.blocks.structure.member.StructureMemberMode;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlockItem;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class ClientStructureMemberBlockTooltip implements ClientTooltipComponent {
    private final StructureMultiblockMemberBlockItem.TooltipData data;

    private final List<Object> lines = new ArrayList<>();

    public ClientStructureMemberBlockTooltip(StructureMultiblockMemberBlockItem.TooltipData data) {
        this.data = data;

        StructureMemberMode mode = data.mode();

        // TODO SWEDZ: use translations
        lines.add(Component.literal("Mode: ").append(mode.text()));

        if (mode == StructureMemberMode.VARIABLE) {
            String name = data.name();
            if (name != null && !name.isEmpty()) {
                lines.add(Component.literal("Name: %s".formatted(name)));
            }
        }

        if (mode == StructureMemberMode.SIMPLE || mode == StructureMemberMode.HATCH) {
            BlockState preview = data.preview();
            if (preview != null && !preview.isAir()) {
                ItemStack previewStack = preview.getBlock().asItem().getDefaultInstance();
                lines.add(Component.literal("Preview:").withStyle(ChatFormatting.UNDERLINE));
                lines.add(List.of(previewStack));
            }

            List<ItemStack> members = new ArrayList<>();
            if (data.members() != null) {
                for (StructureMemberTest member : data.members()) {
                    if (member instanceof StateStructureMemberTest stateTest) {
                        members.add(stateTest.blockState().getBlock().asItem().getDefaultInstance());
                    }
                }
            }
            if (!members.isEmpty()) {
                lines.add(Component.literal("Members:").withStyle(ChatFormatting.UNDERLINE));
                lines.add(members);
            }
        }

        if (mode == StructureMemberMode.HATCH) {
            MachineCasing casing = data.casing();
            if (casing != null) {
                lines.add(Component.literal("Casing:").withStyle(ChatFormatting.UNDERLINE));
                lines.add(casing);
            }

            HatchFlags hatchFlags = data.hatchFlags();
            if (hatchFlags != null && hatchFlags.flags != 0) {
                lines.add(Component.literal("Hatches:").withStyle(ChatFormatting.UNDERLINE));
                for (HatchType hatchType : HatchType.values()) {
                    if (hatchFlags.allows(hatchType)) {
                        lines.add(Component.literal("- %s".formatted(hatchType.name().toLowerCase(Locale.ROOT))));
                    }
                }
            }
        }
    }

    private int iterateLines(int startY, @Nullable BiConsumer<Integer, Component> actionText,
            @Nullable BiConsumer<Integer, List<ItemStack>> actionStacks,
            @Nullable BiConsumer<Integer, MachineCasing> actionCasing) {
        int y = startY;
        for (int i = 0; i < lines.size(); i++) {
            Object line = lines.get(i);
            if (i > 0) {
                Object last = lines.get(i - 1);
                if (line instanceof List || line instanceof MachineCasing) {
                    y += 4;
                }
            }
            if (line instanceof Component text) {
                if (actionText != null) {
                    actionText.accept(y, text);
                }
                y += 9;
            } else if (line instanceof List list) {
                if (actionStacks != null) {
                    actionStacks.accept(y, list);
                }
                y += 20;
            } else if (line instanceof MachineCasing casing) {
                if (actionCasing != null) {
                    actionCasing.accept(y, casing);
                }
                y += 20;
            }
        }
        return y;
    }

    @Override
    public int getHeight() {
        return iterateLines(0, null, null, null);
    }

    @Override
    public int getWidth(Font font) {
        int width = 0;
        for (Object line : lines) {
            int lineWidth = 0;
            if (line instanceof Component text) {
                lineWidth = font.width(text.getVisualOrderText());
            } else if (line instanceof List) {
                lineWidth = 18 * 6;
            } else if (line instanceof MachineCasing) {
                lineWidth = 20;
            }
            if (lineWidth > width) {
                width = lineWidth;
            }
        }
        return width;
    }

    private void renderRowImage(List<ItemStack> stacks, Font font, int x, int y, GuiGraphics graphics) {
        int i = 0;
        for (var stack : stacks) {
            RenderHelper.renderAndDecorateItem(graphics, font, stack, x + i * 18, y);
            if (++i >= 5) {
                break;
            }
        }
    }

    private void renderRowImage(MachineCasing casing, Font font, int x, int y, GuiGraphics graphics) {
        RenderHelper.renderMachineCasingItem(graphics, casing, x, y);
    }

    private void renderRowImageText(int count, Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        if (count >= 6) {
            font.drawInBatch(Component.literal("+ ...").withStyle(MITooltips.DEFAULT_STYLE), x + 18 * 5, y + 5, -1, true, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }

    private void renderRowText(Component text, Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(text, x, y, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        iterateLines(y, null, (lineY, stacks) -> renderRowImage(stacks, font, x, lineY, graphics),
                (lineY, casing) -> renderRowImage(casing, font, x, lineY, graphics));
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        iterateLines(y, (lineY, text) -> renderRowText(text, font, x, lineY, matrix, buffer),
                (lineY, stacks) -> renderRowImageText(stacks.size(), font, x, lineY, matrix, buffer), null);
    }
}
