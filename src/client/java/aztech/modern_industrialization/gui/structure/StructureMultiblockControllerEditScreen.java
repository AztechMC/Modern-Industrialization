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
import aztech.modern_industrialization.blocks.structure.StructureMultiblockControllerBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.network.structure.StructureSaveControllerPacket;
import aztech.modern_industrialization.network.structure.StructureUpdateControllerPacket;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public class StructureMultiblockControllerEditScreen extends Screen {
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xE07272;

    private final StructureMultiblockControllerBlockEntity controller;

    private Button doneButton;
    private Button cancelButton;
    private Button saveButton;

    private EditBox idBox;
    private EditBox casingBox;

    private EditBox posX;
    private EditBox posY;
    private EditBox posZ;
    private EditBox sizeX;
    private EditBox sizeY;
    private EditBox sizeZ;

    public StructureMultiblockControllerEditScreen(StructureMultiblockControllerBlockEntity controller) {
        super(Component.translatable(MIBlock.STRUCTURE_MULTIBLOCK_CONTROLLER.asBlock().getDescriptionId()));
        this.controller = controller;
    }

    private Optional<ResourceLocation> getId() {
        return Optional.ofNullable(ResourceLocation.tryParse(idBox.getValue()));
    }

    private Optional<MachineCasing> getCasing() {
        return Optional.ofNullable(StructureMultiblockControllerBlockEntity.formatCasing(casingBox.getValue()));
    }

    // TODO this needs to be reworked to allow for negative sizes and whatnot
    private Optional<BoundingBox> getBoundingBox() {
        try {
            int minX = Integer.parseInt(posX.getValue());
            int minY = Integer.parseInt(posY.getValue());
            int minZ = Integer.parseInt(posZ.getValue());
            BlockPos min = new BlockPos(minX, minY, minZ);
            int maxX = Integer.parseInt(sizeX.getValue()) + minX - 1;
            int maxY = Integer.parseInt(sizeY.getValue()) + minY - 1;
            int maxZ = Integer.parseInt(sizeZ.getValue()) + minZ - 1;
            BlockPos max = new BlockPos(maxX, maxY, maxZ);
            return Optional.of(BoundingBox.fromCorners(min, max));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private void updateId() {
        boolean validId = idBox.getValue().isEmpty() || this.getId().isPresent();
        idBox.setTextColor(validId ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateCasing() {
        boolean validCasing = casingBox.getValue().isEmpty() || this.getCasing().isPresent();
        casingBox.setTextColor(validCasing ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
    }

    private void updateAll() {
        this.updateId();
        this.updateCasing();
    }

    private void done() {
        this.sendToServer();
        minecraft.setScreen(null);
    }

    private void sendToServer() {
        minecraft.getConnection().send(new StructureUpdateControllerPacket(
                controller.getBlockPos(),
                idBox.getValue(),
                casingBox.getValue(),
                this.getBoundingBox().orElse(new BoundingBox(0, 0, 0, 0, 0, 0))));
    }

    private void cancel() {
        minecraft.setScreen(null);
    }

    private void save() {
        this.sendToServer();
        minecraft.getConnection().send(new StructureSaveControllerPacket(controller.getBlockPos()));
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

        idBox = new EditBox(font, width / 2 - 152, 50, 304, 20, MIText.StructureMultiblockStructureName.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ResourceLocation.isAllowedInResourceLocation(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        idBox.setMaxLength(Short.MAX_VALUE);
        idBox.setValue(controller.getInputId());
        idBox.setResponder(text -> this.updateId());
        this.addRenderableWidget(idBox);

        casingBox = new EditBox(font, width / 2 - 152, 90, 304, 20, MIText.StructureMultiblockHatchCasing.text()) {
            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                return ResourceLocation.isAllowedInResourceLocation(codePoint) && super.charTyped(codePoint, modifiers);
            }
        };
        casingBox.setMaxLength(Short.MAX_VALUE);
        casingBox.setValue(controller.getInputCasing());
        casingBox.setResponder(text -> this.updateCasing());
        this.addRenderableWidget(casingBox);

        BoundingBox bounds = controller.getBounds();
        if (bounds == null) {
            bounds = new BoundingBox(0, 0, 0, 0, 0, 0);
        }

        posX = new EditBox(font, width / 2 - 152, 130, 80, 20, Component.translatable("structure_block.position.x"));
        posX.setMaxLength(15);
        posX.setValue(Integer.toString(bounds.minX()));
        this.addRenderableWidget(posX);
        posY = new EditBox(font, width / 2 - 72, 130, 80, 20, Component.translatable("structure_block.position.y"));
        posY.setMaxLength(15);
        posY.setValue(Integer.toString(bounds.minY()));
        this.addRenderableWidget(posY);
        posZ = new EditBox(font, width / 2 + 8, 130, 80, 20, Component.translatable("structure_block.position.z"));
        posZ.setMaxLength(15);
        posZ.setValue(Integer.toString(bounds.minZ()));
        this.addRenderableWidget(posZ);

        sizeX = new EditBox(font, width / 2 - 152, 170, 80, 20, Component.translatable("structure_block.size.x"));
        sizeX.setMaxLength(15);
        sizeX.setValue(Integer.toString(bounds.maxX() - bounds.minX() + 1));
        this.addRenderableWidget(sizeX);
        sizeY = new EditBox(font, width / 2 - 72, 170, 80, 20, Component.translatable("structure_block.size.y"));
        sizeY.setMaxLength(15);
        sizeY.setValue(Integer.toString(bounds.maxY() - bounds.minY() + 1));
        this.addRenderableWidget(sizeY);
        sizeZ = new EditBox(font, width / 2 + 8, 170, 80, 20, Component.translatable("structure_block.size.z"));
        sizeZ.setMaxLength(15);
        sizeZ.setValue(Integer.toString(bounds.maxZ() - bounds.minZ() + 1));
        this.addRenderableWidget(sizeZ);

        this.updateAll();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        graphics.drawCenteredString(font, title, width / 2, 20, 0xFFFFFF);

        graphics.drawString(font, MIText.StructureMultiblockStructureName.text(), width / 2 - 153, 40, 0xA0A0A0);

        graphics.drawString(font, MIText.StructureMultiblockHatchCasing.text(), width / 2 - 153, 80, 0xA0A0A0);

        graphics.drawString(font, Component.translatable("structure_block.position"), width / 2 - 153, 120, 0xA0A0A0);

        graphics.drawString(font, Component.translatable("structure_block.size"), width / 2 - 153, 160, 0xA0A0A0);
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
        String idBoxValue = idBox.getValue();
        String casingBoxValue = casingBox.getValue();
        String posXValue = posX.getValue();
        String posYValue = posY.getValue();
        String posZValue = posZ.getValue();
        String sizeXValue = sizeX.getValue();
        String sizeYValue = sizeY.getValue();
        String sizeZValue = sizeZ.getValue();

        this.init(minecraft, width, height);

        idBox.setValue(idBoxValue);
        casingBox.setValue(casingBoxValue);
        posX.setValue(posXValue);
        posY.setValue(posYValue);
        posZ.setValue(posZValue);
        sizeX.setValue(sizeXValue);
        sizeY.setValue(sizeYValue);
        sizeZ.setValue(sizeZValue);
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
