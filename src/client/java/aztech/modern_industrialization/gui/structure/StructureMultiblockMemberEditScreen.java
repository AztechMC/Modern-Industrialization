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
import aztech.modern_industrialization.blocks.structure.StructureMultiblockMemberBlockEntity;
import aztech.modern_industrialization.network.structure.StructureUpdateMemberPacket;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMultiblockMemberEditScreen extends Screen {
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xE07272;

    private final StructureMultiblockMemberBlockEntity member;

    private Button doneButton;
    private Button cancelButton;

    private EditBox previewBox;
    private EditBox membersBox;

    public StructureMultiblockMemberEditScreen(StructureMultiblockMemberBlockEntity member) {
        super(Component.translatable(MIBlock.STRUCTURE_MULTIBLOCK_MEMBER.asBlock().getDescriptionId()));
        this.member = member;
    }

    private Optional<BlockState> getPreview() {
        return Optional.ofNullable(StructureMultiblockMemberBlockEntity.formatPreview(previewBox.getValue()));
    }

    private Optional<List<Predicate<BlockState>>> getMembers() {
        return Optional.ofNullable(StructureMultiblockMemberBlockEntity.formatMembers(membersBox.getValue()));
    }

    private void updatePreview() {
        boolean validPreview = previewBox.getValue().isEmpty() || this.getPreview().isPresent();
        previewBox.setTextColor(validPreview ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateMembers() {
        boolean validMembers = membersBox.getValue().isEmpty() || this.getMembers().isPresent();
        membersBox.setTextColor(validMembers ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateAll() {
        this.updatePreview();
        this.updateMembers();
    }

    private void done() {
        this.sendToServer();
        minecraft.setScreen(null);
    }

    private void sendToServer() {
        minecraft.getConnection().send(new StructureUpdateMemberPacket(
                member.getBlockPos(),
                previewBox.getValue(),
                membersBox.getValue()));
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
        previewBox.setValue(member.getInputPreview());
        previewBox.setResponder(text -> this.updatePreview());
        this.addRenderableWidget(previewBox);

        membersBox = new EditBox(font, width / 2 - 152, 90, 304, 20, MIText.StructureMultiblockMemberMembers.text());
        membersBox.setMaxLength(Short.MAX_VALUE);
        membersBox.setValue(member.getInputMembers());
        membersBox.setResponder(text -> this.updateMembers());
        this.addRenderableWidget(membersBox);

        this.updateAll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        graphics.drawString(font, MIText.StructureMultiblockMemberPreview.text(), width / 2 - 153, 40, 0xA0A0A0);

        graphics.drawString(font, MIText.StructureMultiblockMemberMembers.text(), width / 2 - 153, 80, 0xA0A0A0);
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

        this.init(minecraft, width, height);

        previewBox.setValue(previewBoxValue);
        membersBox.setValue(membersBoxValue);
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
