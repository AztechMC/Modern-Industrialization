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
import aztech.modern_industrialization.blocks.structure.controller.StructureControllerBounds;
import aztech.modern_industrialization.blocks.structure.controller.StructureControllerMode;
import aztech.modern_industrialization.blocks.structure.controller.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.structure.MIStructureTemplateManager;
import aztech.modern_industrialization.machines.multiblocks.structure.StructureMultiblockInputFormatters;
import aztech.modern_industrialization.network.structure.StructureLoadControllerPacket;
import aztech.modern_industrialization.network.structure.StructureSaveControllerPacket;
import aztech.modern_industrialization.network.structure.StructureUpdateControllerPacket;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class StructureMultiblockControllerEditScreen extends Screen {
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xE07272;

    private static final ImmutableList<StructureControllerMode> ALL_MODES = ImmutableList.copyOf(StructureControllerMode.values());

    private final StructureMultiblockControllerBlockEntity controller;

    private Button doneButton;
    private Button cancelButton;
    private CycleButton<StructureControllerMode> modeButton;
    private Button saveButton;
    private Button loadButton;

    private EditBox idBox;
    private EditBox casingBox;

    private EditBox posXBox;
    private EditBox posYBox;
    private EditBox posZBox;
    private EditBox sizeXBox;
    private EditBox sizeYBox;
    private EditBox sizeZBox;

    private CycleButton<Boolean> showBoundsBox;

    public StructureMultiblockControllerEditScreen(StructureMultiblockControllerBlockEntity controller) {
        super(Component.translatable(MIBlock.STRUCTURE_MULTIBLOCK_CONTROLLER.asBlock().getDescriptionId()));
        this.controller = controller;
    }

    private Optional<ResourceLocation> getId() {
        ResourceLocation id = StructureMultiblockInputFormatters.id(idBox.getValue());
        return switch (modeButton.getValue()) {
        case SAVE -> Optional.ofNullable(id);
        case LOAD -> id != null && MIStructureTemplateManager.exists(id) ? Optional.of(id) : Optional.empty();
        };
    }

    private Optional<MachineCasing> getCasing() {
        return Optional.ofNullable(StructureMultiblockInputFormatters.casing(casingBox.getValue()));
    }

    private Optional<StructureControllerBounds> getBounds() {
        try {
            int x = Integer.parseInt(posXBox.getValue());
            int y = Integer.parseInt(posYBox.getValue());
            int z = Integer.parseInt(posZBox.getValue());
            int sizeX = Integer.parseInt(sizeXBox.getValue());
            int sizeY = Integer.parseInt(sizeYBox.getValue());
            int sizeZ = Integer.parseInt(sizeZBox.getValue());
            return Optional.of(new StructureControllerBounds(x, y, z, sizeX, sizeY, sizeZ));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private void updateMode(StructureControllerMode mode) {
        this.sendToServer();

        saveButton.visible = false;
        loadButton.visible = false;
        casingBox.visible = false;
        posXBox.visible = false;
        posYBox.visible = false;
        posZBox.visible = false;
        sizeXBox.visible = false;
        sizeYBox.visible = false;
        sizeZBox.visible = false;
        showBoundsBox.visible = false;

        switch (mode) {
        case SAVE -> {
            saveButton.visible = true;
            casingBox.visible = true;
            posXBox.visible = true;
            posYBox.visible = true;
            posZBox.visible = true;
            sizeXBox.visible = true;
            sizeYBox.visible = true;
            sizeZBox.visible = true;
            showBoundsBox.visible = true;
        }
        case LOAD -> {
            loadButton.visible = true;
        }
        }

        idBox.setValue(idBox.getValue());
    }

    private void updateId() {
        boolean validId = idBox.getValue().isEmpty() || this.getId().isPresent();
        idBox.setTextColor(validId ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
        loadButton.active = validId;
    }

    private void updateCasing() {
        boolean validCasing = casingBox.getValue().isEmpty() || this.getCasing().isPresent();
        casingBox.setTextColor(validCasing ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateBounds() {
        boolean validBounds = this.getBounds().filter((b) -> !b.isEmpty()).isPresent();
        saveButton.active = validBounds;
    }

    private void updateAll() {
        this.updateMode(controller.getMode());
        this.updateId();
        this.updateCasing();
        this.updateBounds();
    }

    private void done() {
        this.sendToServer();
        minecraft.setScreen(null);
    }

    private void sendToServer() {
        new StructureUpdateControllerPacket(
                controller.getBlockPos(),
                modeButton.getValue(),
                idBox.getValue(),
                casingBox.getValue(),
                this.getBounds().orElse(new StructureControllerBounds(0, 0, 0, 1, 1, 1)),
                showBoundsBox.getValue()).sendToServer();
    }

    private void cancel() {
        minecraft.setScreen(null);
    }

    private void save() {
        this.sendToServer();
        new StructureSaveControllerPacket(controller.getBlockPos()).sendToServer();
        minecraft.setScreen(null);
    }

    private void load() {
        this.sendToServer();
        new StructureLoadControllerPacket(controller.getBlockPos(), !hasShiftDown()).sendToServer();
        minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(
                doneButton = Button.builder(CommonComponents.GUI_DONE, button -> this.done()).bounds(width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(
                cancelButton = Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).bounds(width / 2 + 4, 210, 150, 20).build());
        this.addRenderableWidget(
                saveButton = Button.builder(Component.translatable("structure_block.button.save"), button -> this.save())
                        .bounds(width / 2 + 4 + 100, 185, 50, 20).build());
        this.addRenderableWidget(
                loadButton = Button.builder(Component.translatable("structure_block.button.load"), button -> this.load())
                        .bounds(width / 2 + 4 + 100, 185, 50, 20)
                        .tooltip(Tooltip.create(MIText.StructureMultiblockLoadTooltip.text()))
                        .build());
        this.addRenderableWidget(modeButton = CycleButton.builder(StructureControllerMode::text)
                .withValues(ALL_MODES, ALL_MODES)
                .displayOnlyValue()
                .withInitialValue(controller.getMode())
                .create(width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (button, mode) -> this.updateMode(mode)));

        idBox = new EditBox(font, width / 2 - 152, 50, 304, 20, MIText.StructureMultiblockStructureName.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return StructureMultiblockInputFormatters.isValidIdCharacter(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        idBox.setMaxLength(Short.MAX_VALUE);
        idBox.setValue(controller.getInputId());
        idBox.setResponder(text -> this.updateId());
        this.addRenderableWidget(idBox);

        casingBox = new EditBox(font, width / 2 - 152, 90, 304, 20, MIText.StructureMultiblockCasing.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ResourceLocation.isAllowedInResourceLocation(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        casingBox.setMaxLength(Short.MAX_VALUE);
        casingBox.setValue(controller.getInputCasing());
        casingBox.setResponder(text -> this.updateCasing());
        this.addRenderableWidget(casingBox);

        StructureControllerBounds bounds = controller.getBounds();

        posXBox = new EditBox(font, width / 2 - 152, 130, 35, 20, Component.translatable("structure_block.position.x"));
        posXBox.setMaxLength(15);
        posXBox.setValue(Integer.toString(bounds.x()));
        posXBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(posXBox);
        posYBox = new EditBox(font, width / 2 - 117, 130, 35, 20, Component.translatable("structure_block.position.y"));
        posYBox.setMaxLength(15);
        posYBox.setValue(Integer.toString(bounds.y()));
        posYBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(posYBox);
        posZBox = new EditBox(font, width / 2 - 82, 130, 35, 20, Component.translatable("structure_block.position.z"));
        posZBox.setMaxLength(15);
        posZBox.setValue(Integer.toString(bounds.z()));
        posZBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(posZBox);

        sizeXBox = new EditBox(font, width / 2 - 47 + 8, 130, 35, 20, Component.translatable("structure_block.size.x"));
        sizeXBox.setMaxLength(15);
        sizeXBox.setValue(Integer.toString(bounds.sizeX()));
        sizeXBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(sizeXBox);
        sizeYBox = new EditBox(font, width / 2 - 4, 130, 35, 20, Component.translatable("structure_block.size.y"));
        sizeYBox.setMaxLength(15);
        sizeYBox.setValue(Integer.toString(bounds.sizeY()));
        sizeYBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(sizeYBox);
        sizeZBox = new EditBox(font, width / 2 + 31, 130, 35, 20, Component.translatable("structure_block.size.z"));
        sizeZBox.setMaxLength(15);
        sizeZBox.setValue(Integer.toString(bounds.sizeZ()));
        sizeZBox.setResponder(text -> this.updateBounds());
        this.addRenderableWidget(sizeZBox);

        this.addRenderableWidget(showBoundsBox = CycleButton.onOffBuilder(controller.shouldShowBounds())
                .displayOnlyValue()
                .create(width / 2 + 4 + 100, 130, 50, 20, Component.translatable("structure_block.show_boundingbox")));

        this.updateAll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        graphics.drawString(font, MIText.StructureMultiblockStructureName.text(), width / 2 - 152, 40, 0xA0A0A0);

        if (casingBox.visible)
            graphics.drawString(font, MIText.StructureMultiblockCasing.text(), width / 2 - 152, 80, 0xA0A0A0);

        if (posXBox.visible || posYBox.visible || posZBox.visible)
            graphics.drawString(font, Component.translatable("structure_block.position"), width / 2 - 152, 120, 0xA0A0A0);

        if (sizeXBox.visible || sizeYBox.visible || sizeZBox.visible)
            graphics.drawString(font, Component.translatable("structure_block.size"), width / 2 - 47 + 8, 120, 0xA0A0A0);

        if (showBoundsBox.visible)
            graphics.drawString(font, Component.translatable("structure_block.show_boundingbox"),
                    width / 2 + 154 - font.width(Component.translatable("structure_block.show_boundingbox")), 120, 0xA0A0A0);

        graphics.drawString(font, modeButton.getValue().textInfo(), width / 2 - 4 - 150, 175, 0xA0A0A0);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(idBox);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        StructureControllerMode modeButtonValue = modeButton.getValue();
        String idBoxValue = idBox.getValue();
        String casingBoxValue = casingBox.getValue();
        String posXBoxValue = posXBox.getValue();
        String posYBoxValue = posYBox.getValue();
        String posZBoxValue = posZBox.getValue();
        String sizeXBoxValue = sizeXBox.getValue();
        String sizeYBoxValue = sizeYBox.getValue();
        String sizeZBoxValue = sizeZBox.getValue();
        boolean showBoundsBoxValue = showBoundsBox.getValue();

        this.init(minecraft, width, height);

        modeButton.setValue(modeButtonValue);
        idBox.setValue(idBoxValue);
        casingBox.setValue(casingBoxValue);
        posXBox.setValue(posXBoxValue);
        posYBox.setValue(posYBoxValue);
        posZBox.setValue(posZBoxValue);
        sizeXBox.setValue(sizeXBoxValue);
        sizeYBox.setValue(sizeYBoxValue);
        sizeZBox.setValue(sizeZBoxValue);
        showBoundsBox.setValue(showBoundsBoxValue);
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
        if (controller.isRemoved()) {
            this.onClose();
        }
    }
}
