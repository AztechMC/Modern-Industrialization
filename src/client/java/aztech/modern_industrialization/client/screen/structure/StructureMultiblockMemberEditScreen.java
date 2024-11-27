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
package aztech.modern_industrialization.client.screen.structure;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.structure.member.StructureMemberMode;
import aztech.modern_industrialization.blocks.structure.member.StructureMultiblockMemberBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import aztech.modern_industrialization.network.structure.StructureUpdateMemberPacket;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMultiblockMemberEditScreen extends Screen {
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xE07272;

    private static final ImmutableList<StructureMemberMode> ALL_MODES = ImmutableList.copyOf(StructureMemberMode.values());

    private final StructureMultiblockMemberBlockEntity member;

    private Button doneButton;
    private Button cancelButton;
    private CycleButton<StructureMemberMode> modeButton;

    private EditBox nameBox;

    private EditBox previewBox;
    private EditBox membersBox;

    private EditBox casingBox;
    private EditBox flagsBox;

    public StructureMultiblockMemberEditScreen(StructureMultiblockMemberBlockEntity member) {
        super(Component.translatable(MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().getDescriptionId()));
        this.member = member;
    }

    private Optional<BlockState> getPreview() {
        return Optional.ofNullable(StructureMultiblockInputFormatters.preview(previewBox.getValue()));
    }

    private Optional<List<StructureMemberTest>> getMembers() {
        return Optional.ofNullable(StructureMultiblockInputFormatters.members(membersBox.getValue()));
    }

    private Optional<MachineCasing> getCasing() {
        return Optional.ofNullable(StructureMultiblockInputFormatters.casing(casingBox.getValue()));
    }

    private Optional<HatchFlags> getHatchFlags() {
        return Optional.ofNullable(StructureMultiblockInputFormatters.hatchFlags(flagsBox.getValue()));
    }

    private void updateMode(StructureMemberMode mode) {
        this.sendToServer();

        nameBox.visible = false;
        previewBox.visible = false;
        membersBox.visible = false;
        casingBox.visible = false;
        flagsBox.visible = false;

        switch (mode) {
        case HATCH -> {
            previewBox.visible = true;
            membersBox.visible = true;
            casingBox.visible = true;
            flagsBox.visible = true;
        }
        case SIMPLE -> {
            previewBox.visible = true;
            membersBox.visible = true;
        }
        case VARIABLE -> {
            nameBox.visible = true;
        }
        }
    }

    private void updateName() {
        boolean validName = !nameBox.getValue().isEmpty();
        nameBox.setTextColor(validName ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
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
        this.updateMode(member.getMode());
        this.updateName();
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
        new StructureUpdateMemberPacket(
                member.getBlockPos(),
                modeButton.getValue(),
                nameBox.getValue(),
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
        this.addRenderableWidget(modeButton = CycleButton.builder(StructureMemberMode::text)
                .withValues(ALL_MODES, ALL_MODES)
                .displayOnlyValue()
                .withInitialValue(member.getMode())
                .create(width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (button, mode) -> this.updateMode(mode)));

        nameBox = new EditBox(font, width / 2 - 152, 20, 304, 20, MIText.StructureMultiblockMemberName.text());
        nameBox.setMaxLength(Short.MAX_VALUE);
        nameBox.setValue(member.getInputName());
        nameBox.setResponder(text -> this.updateName());
        this.addRenderableWidget(nameBox);

        previewBox = new EditBox(font, width / 2 - 152, 20, 304, 20, MIText.StructureMultiblockMemberPreview.text());
        previewBox.setMaxLength(Short.MAX_VALUE);
        previewBox.setValue(member.getInputPreview());
        previewBox.setResponder(text -> this.updatePreview());
        this.addRenderableWidget(previewBox);

        membersBox = new EditBox(font, width / 2 - 152, 60, 304, 20, MIText.StructureMultiblockMemberMembers.text());
        membersBox.setMaxLength(Short.MAX_VALUE);
        membersBox.setValue(member.getInputMembers());
        membersBox.setResponder(text -> this.updateMembers());
        this.addRenderableWidget(membersBox);

        casingBox = new EditBox(font, width / 2 - 152, 100, 304, 20, MIText.StructureMultiblockCasing.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ResourceLocation.isAllowedInResourceLocation(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        casingBox.setMaxLength(Short.MAX_VALUE);
        casingBox.setValue(member.getInputCasing());
        casingBox.setResponder(text -> this.updateCasing());
        this.addRenderableWidget(casingBox);

        flagsBox = new EditBox(font, width / 2 - 152, 140, 304, 20, MIText.StructureMultiblockMemberHatchFlags.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return (Character.isDigit(codePoint) || Character.isAlphabetic(codePoint) || codePoint == ';' || codePoint == '_')
                        && super.charTyped(codePoint, modifiers);
            }
        };
        flagsBox.setMaxLength(Short.MAX_VALUE);
        flagsBox.setValue(member.getInputHatchFlags());
        flagsBox.setResponder(text -> this.updateFlags());
        this.addRenderableWidget(flagsBox);

        this.updateAll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        if (nameBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockMemberName.text(), width / 2 - 152, 10, 0xA0A0A0);

        if (previewBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockMemberPreview.text(), width / 2 - 152, 10, 0xA0A0A0);

        if (membersBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockMemberMembers.text(), width / 2 - 152, 50, 0xA0A0A0);

        if (casingBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockCasing.text(), width / 2 - 152, 90, 0xA0A0A0);

        if (flagsBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockMemberHatchFlags.text(), width / 2 - 152, 130, 0xA0A0A0);

        graphics.drawString(font, modeButton.getValue().textInfo(), width / 2 - 4 - 150, 175, 0xA0A0A0);
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
        StructureMemberMode modeButtonValue = modeButton.getValue();
        String nameBoxValue = nameBox.getValue();
        String previewBoxValue = previewBox.getValue();
        String membersBoxValue = membersBox.getValue();
        String casingBoxValue = casingBox.getValue();
        String flagsBoxValue = flagsBox.getValue();

        this.init(minecraft, width, height);

        modeButton.setValue(modeButtonValue);
        nameBox.setValue(nameBoxValue);
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
        if (member.isRemoved()) {
            this.onClose();
        }
    }
}
