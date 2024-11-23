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
package aztech.modern_industrialization.gui.structure;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.structure.StructureMultiblockHatchBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMemberTest;
import aztech.modern_industrialization.network.structure.StructureUpdateHatchPacket;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMultiblockHatchEditScreen extends Screen {
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xE07272;

    private final StructureMultiblockHatchBlockEntity hatch;

    private Button doneButton;
    private Button cancelButton;

    private EditBox previewBox;
    private EditBox membersBox;

    private EditBox casingBox;
    private EditBox flagsBox;

    public StructureMultiblockHatchEditScreen(StructureMultiblockHatchBlockEntity hatch) {
        super(Component.translatable(MIBlock.STRUCTURE_MULTIBLOCK_HATCH.asBlock().getDescriptionId()));
        this.hatch = hatch;
    }

    private Optional<BlockState> getPreview() {
        return Optional.ofNullable(StructureMultiblockFormatters.preview(previewBox.getValue()));
    }

    private Optional<List<StructureMemberTest>> getMembers() {
        return Optional.ofNullable(StructureMultiblockFormatters.members(membersBox.getValue()));
    }

    private Optional<MachineCasing> getCasing() {
        return Optional.ofNullable(StructureMultiblockFormatters.casing(casingBox.getValue()));
    }

    private Optional<HatchFlags> getHatchFlags() {
        return Optional.ofNullable(StructureMultiblockFormatters.hatchFlags(flagsBox.getValue()));
    }

    private void updatePreview() {
        boolean validPreview = previewBox.getValue().isEmpty() || this.getPreview().isPresent();
        previewBox.setTextColor(validPreview ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateMembers() {
        boolean validMembers = membersBox.getValue().isEmpty() || this.getMembers().isPresent();
        membersBox.setTextColor(validMembers ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateCasing() {
        boolean validCasing = casingBox.getValue().isEmpty() || this.getCasing().isPresent();
        casingBox.setTextColor(validCasing ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateFlags() {
        boolean validFlags = flagsBox.getValue().isEmpty() || this.getHatchFlags().isPresent();
        flagsBox.setTextColor(validFlags ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateAll() {
        this.updatePreview();
        this.updateMembers();
        this.updateCasing();
        this.updateFlags();
    }

    private void done() {
        this.sendToServer();
        minecraft.setScreen(null);
    }

    private void sendToServer() {
        new StructureUpdateHatchPacket(
                hatch.getBlockPos(),
                previewBox.getValue(),
                membersBox.getValue(),
                casingBox.getValue(),
                flagsBox.getValue()).sendToServer();
    }

    private void cancel() {
        minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
                doneButton = Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(
                cancelButton = Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(width / 2 + 4, 210, 150, 20).build());

        previewBox = new EditBox(font, width / 2 - 152, 50, 304, 20, MIText.StructureMultiblockMemberPreview.text());
        previewBox.setMaxLength(Short.MAX_VALUE);
        previewBox.setValue(hatch.getInputPreview());
        previewBox.setResponder(text -> this.updatePreview());
        this.addRenderableWidget(previewBox);

        membersBox = new EditBox(font, width / 2 - 152, 90, 304, 20, MIText.StructureMultiblockMemberMembers.text());
        membersBox.setMaxLength(Short.MAX_VALUE);
        membersBox.setValue(hatch.getInputMembers());
        membersBox.setResponder(text -> this.updateMembers());
        this.addRenderableWidget(membersBox);

        casingBox = new EditBox(font, width / 2 - 152, 130, 304, 20, MIText.StructureMultiblockHatchCasing.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ResourceLocation.isAllowedInResourceLocation(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        casingBox.setMaxLength(Short.MAX_VALUE);
        casingBox.setValue(hatch.getInputCasing());
        casingBox.setResponder(text -> this.updateCasing());
        this.addRenderableWidget(casingBox);

        flagsBox = new EditBox(font, width / 2 - 152, 170, 304, 20, MIText.StructureMultiblockHatchFlags.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return (Character.isDigit(codePoint) || Character.isAlphabetic(codePoint) || codePoint == ';' || codePoint == '_')
                        && super.charTyped(codePoint, modifiers);
            }
        };
        flagsBox.setMaxLength(Short.MAX_VALUE);
        flagsBox.setValue(hatch.getInputFlags());
        flagsBox.setResponder(text -> this.updateFlags());
        this.addRenderableWidget(flagsBox);

        this.updateAll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        graphics.drawString(font, MIText.StructureMultiblockMemberPreview.text(), width / 2 - 152, 40, 0xA0A0A0);

        graphics.drawString(font, MIText.StructureMultiblockMemberMembers.text(), width / 2 - 152, 80, 0xA0A0A0);

        graphics.drawString(font, MIText.StructureMultiblockHatchCasing.text(), width / 2 - 152, 120, 0xA0A0A0);

        graphics.drawString(font, MIText.StructureMultiblockHatchFlags.text(), width / 2 - 152, 160, 0xA0A0A0);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(previewBox);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String previewBoxValue = previewBox.getValue();
        String membersBoxValue = membersBox.getValue();
        String casingBoxValue = casingBox.getValue();
        String flagsBoxValue = flagsBox.getValue();

        this.init(minecraft, width, height);

        previewBox.setValue(previewBoxValue);
        membersBox.setValue(membersBoxValue);
        casingBox.setValue(casingBoxValue);
        flagsBox.setValue(flagsBoxValue);
    }

    @Override
    public void onClose() {
        this.cancel();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER) {
            this.done();
            return true;
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (hatch.isRemoved()) {
            this.onClose();
        }
    }
}
