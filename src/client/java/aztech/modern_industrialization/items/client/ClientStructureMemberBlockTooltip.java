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
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StateStructureMemberTest;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

// TODO SWEDZ: there has to be a better way to align all the text and stuff. maybe some kind of builder?
public final class ClientStructureMemberBlockTooltip implements ClientTooltipComponent {
    private static final int TEXT_ROW_HEIGHT = 9;
    private static final int TEXT_BEFORE_IMAGE_ROW_HEIGHT = TEXT_ROW_HEIGHT + 4;
    private static final int IMAGE_ROW_HEIGHT = 20;

    private final StructureMultiblockMemberBlockItem.TooltipData data;

    private final ItemStack preview;
    private final List<ItemStack> members;

    public ClientStructureMemberBlockTooltip(StructureMultiblockMemberBlockItem.TooltipData data) {
        this.data = data;

        preview = data.preview().getBlock().asItem().getDefaultInstance();

        members = new ArrayList<>();
        for (StructureMemberTest member : data.members()) {
            if (member instanceof StateStructureMemberTest stateTest) {
                members.add(stateTest.blockState().getBlock().asItem().getDefaultInstance());
            }
        }
    }

    @Override
    public int getHeight() {
        int height = 0;

        height += TEXT_ROW_HEIGHT;

        if (renderName()) {
            height += TEXT_ROW_HEIGHT;
        }

        if (renderCasing()) {
            height += TEXT_ROW_HEIGHT;
        }

        if (renderPreview()) {
            height += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            height += IMAGE_ROW_HEIGHT;
        }

        if (renderMembers()) {
            height += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            height += IMAGE_ROW_HEIGHT;
        }

        if (renderHatch()) {
            height += TEXT_ROW_HEIGHT;
            for (HatchType hatchType : HatchType.values()) {
                if (data.hatchFlags().allows(hatchType)) {
                    height += TEXT_ROW_HEIGHT;
                }
            }
        }

        return height;
    }

    @Override
    public int getWidth(Font font) {
        // TODO SWEDZ: get actual width
        return 18 * 6;
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

    private void renderRowImageText(int count, Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        if (count >= 6) {
            font.drawInBatch(Component.literal("+ ...").withStyle(MITooltips.DEFAULT_STYLE), x + 18 * 5, y + 5, -1, true, matrix, buffer,
                    Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
    }

    private void renderRowText(Component text, Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(text, x, y, -1, true, matrix, buffer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
    }

    private boolean renderName() {
        return data.mode() == StructureMemberMode.VARIABLE;
    }

    private boolean renderPreview() {
        return data.mode() == StructureMemberMode.SIMPLE || data.mode() == StructureMemberMode.HATCH;
    }

    private boolean renderMembers() {
        return data.mode() == StructureMemberMode.SIMPLE || data.mode() == StructureMemberMode.HATCH;
    }

    private boolean renderCasing() {
        return data.mode() == StructureMemberMode.HATCH;
    }

    private boolean renderHatch() {
        return data.mode() == StructureMemberMode.HATCH && data.hatchFlags().flags != 0;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int lineY = y;

        lineY += TEXT_ROW_HEIGHT;

        if (renderName()) {
            lineY += TEXT_ROW_HEIGHT;
        }

        if (renderCasing()) {
            lineY += TEXT_ROW_HEIGHT;
        }

        if (renderPreview()) {
            lineY += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            renderRowImage(List.of(preview), font, x, lineY, graphics);
            lineY += IMAGE_ROW_HEIGHT;
        }

        if (renderMembers()) {
            lineY += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            renderRowImage(members, font, x, lineY, graphics);
            lineY += IMAGE_ROW_HEIGHT;
        }

        if (renderHatch()) {
            lineY += TEXT_ROW_HEIGHT;
            for (HatchType hatchType : HatchType.values()) {
                if (data.hatchFlags().allows(hatchType)) {
                    lineY += TEXT_ROW_HEIGHT;
                }
            }
        }
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource buffer) {
        int lineY = y;

        // TODO SWEDZ: use translations
        renderRowText(Component.literal("Mode: ").append(data.mode().text()), font, x, lineY, matrix, buffer);
        lineY += TEXT_ROW_HEIGHT;

        if (renderName()) {
            renderRowText(Component.literal("Name: %s".formatted(data.name())), font, x, lineY, matrix, buffer);
            lineY += TEXT_ROW_HEIGHT;
        }

        if (renderCasing()) {
            renderRowText(Component.literal("Casing: %s".formatted(data.casing().key.getPath())), font, x, lineY, matrix, buffer);
            lineY += TEXT_ROW_HEIGHT;
        }

        if (renderPreview()) {
            renderRowText(Component.literal("Preview").withStyle(ChatFormatting.UNDERLINE), font, x, lineY, matrix, buffer);
            lineY += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            lineY += IMAGE_ROW_HEIGHT;
        }

        if (renderMembers()) {
            renderRowText(Component.literal("Members").withStyle(ChatFormatting.UNDERLINE), font, x, lineY, matrix, buffer);
            lineY += TEXT_BEFORE_IMAGE_ROW_HEIGHT;
            renderRowImageText(members.size(), font, x, lineY, matrix, buffer);
            lineY += IMAGE_ROW_HEIGHT;
        }

        if (renderHatch()) {
            renderRowText(Component.literal("Hatches").withStyle(ChatFormatting.UNDERLINE), font, x, lineY, matrix, buffer);
            lineY += TEXT_ROW_HEIGHT;
            for (HatchType hatchType : HatchType.values()) {
                if (data.hatchFlags().allows(hatchType)) {
                    renderRowText(Component.literal("- %s".formatted(hatchType.name().toLowerCase(Locale.ROOT))), font, x, lineY, matrix, buffer);
                    lineY += TEXT_ROW_HEIGHT;
                }
            }
        }
    }
}
