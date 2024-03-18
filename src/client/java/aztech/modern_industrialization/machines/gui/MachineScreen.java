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
package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.client.DynamicTooltip;
import aztech.modern_industrialization.client.screen.MIHandledScreen;
import aztech.modern_industrialization.inventory.BackgroundRenderedSlot;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.network.machines.AdjustSlotCapacityPacket;
import aztech.modern_industrialization.network.machines.LockAllPacket;
import aztech.modern_industrialization.network.machines.SetLockingModePacket;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.Rectangle;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MachineScreen extends MIHandledScreen<MachineMenuClient> implements ClientComponentRenderer.ButtonContainer {
    public static final ResourceLocation SLOT_ATLAS = new MIIdentifier("textures/gui/container/slot_atlas.png");
    public static final ResourceLocation BACKGROUND = new MIIdentifier("textures/gui/container/background.png");

    private final List<ClientComponentRenderer> renderers = new ArrayList<>();

    public MachineScreen(MachineMenuClient handler, Inventory inventory, Component title) {
        super(handler, inventory, title);

        for (GuiComponentClient component : handler.components) {
            renderers.add(component.createRenderer(this));
        }

        this.imageHeight = handler.guiParams.backgroundHeight;
        this.imageWidth = handler.guiParams.backgroundWidth;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    public int x() {
        return leftPos;
    }

    public int y() {
        return topPos;
    }

    private int nextButtonX;
    private static final int BUTTON_Y = 4;

    private int buttonX() {
        nextButtonX -= 22;
        return nextButtonX + 22 + leftPos;
    }

    private int buttonY() {
        return BUTTON_Y + topPos;
    }

    @Override
    protected void init() {
        super.init();
        this.nextButtonX = 152;
        if (menu.guiParams.lockButton) {
            addLockButton();
        }

        for (ClientComponentRenderer renderer : renderers) {
            renderer.addButtons(this);
        }
    }

    public void blitButton(Button button, GuiGraphics guiGraphics, int baseU, int baseV, int selectedOverlayU, int selectedOverlayV) {
        guiGraphics.blit(SLOT_ATLAS, button.getX(), button.getY(), baseU, baseV, button.getWidth(), button.getHeight());
        if (button.isHoveredOrFocused()) {
            guiGraphics.blit(SLOT_ATLAS, button.getX(), button.getY(), selectedOverlayU, selectedOverlayV, button.getWidth(), button.getHeight());
        }
    }

    /**
     * Helper method to draw a 20x20 button.
     */
    public void blitButton(Button button, GuiGraphics guiGraphics, int u, int v) {
        blitButton(button, guiGraphics, u, v, 60, 18);
    }

    /**
     * Helper method to draw a 12x12 button.
     */
    public void blitButtonSmall(Button button, GuiGraphics guiGraphics, int u, int v) {
        blitButton(button, guiGraphics, u, v, 138, 58);
    }

    public void blitButtonNoHighlight(Button button, GuiGraphics guiGraphics, int u, int v) {
        guiGraphics.blit(SLOT_ATLAS, button.getX(), button.getY(), u, v, button.getWidth(), button.getHeight());
    }

    @Override
    public MachineButton addButton(int u, Consumer<Integer> pressAction, Supplier<List<Component>> tooltipSupplier,
            Supplier<Boolean> isPressed) {

        return addRenderableWidget(new MachineButton(buttonX(), buttonY(), 20, 20, b -> pressAction.accept(menu.containerId),
                tooltipSupplier,
                (screen, button, guiGraphics, mouseX, mouseY, delta) -> {
                    blitButton(button, guiGraphics, u, isPressed.get() ? 38 : 18);
                }, () -> true));

    }

    @Override
    public MachineButton addButton(int posX, int posY, int width, int height, Consumer<Integer> pressAction,
            Supplier<List<Component>> tooltipSupplier, ClientComponentRenderer.CustomButtonRenderer renderer, Supplier<Boolean> isButtonPresent) {

        return addRenderableWidget(new MachineButton(posX + leftPos, posY + topPos, width, height, b -> {
            pressAction.accept(menu.containerId);
        }, tooltipSupplier, renderer, isButtonPresent));
    }

    private void addLockButton() {
        addButton(40, syncId -> {
            if (hasShiftDown()) {
                boolean lock = menu.hasUnlockedSlot();
                menu.lockAll(lock);
                new LockAllPacket(syncId, lock).sendToServer();
            } else {
                boolean newLockingMode = !menu.lockingMode;
                menu.lockingMode = newLockingMode;
                new SetLockingModePacket(syncId, newLockingMode).sendToServer();
            }
        }, () -> {
            List<Component> lines = new ArrayList<>();
            if (menu.lockingMode) {
                lines.add(MIText.LockingModeOn.text());
                lines.add(MIText.ClickToDisable.text().setStyle(TextHelper.GRAY_TEXT));
            } else {
                lines.add(MIText.LockingModeOff.text());
                lines.add(MIText.ClickToEnable.text().setStyle(TextHelper.GRAY_TEXT));
            }
            lines.add((menu.hasUnlockedSlot() ? MIText.ShiftClickToLockAll : MIText.ShiftClickToUnlockAll).text().setStyle(TextHelper.GRAY_TEXT));
            return lines;
        }, () -> menu.lockingMode);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        // Update button visibility
        for (var element : renderables) {
            if (element instanceof MachineButton machineButton) {
                machineButton.visible = machineButton.isPresent.get();
            }
        }

        // Normal render - handles background and slots
        super.render(guiGraphics, mouseX, mouseY, delta);
        // Tooltips
        renderConfigurableSlotTooltips(guiGraphics, mouseX, mouseY);
        for (ClientComponentRenderer renderer : renderers) {
            renderer.renderTooltip(this, font, guiGraphics, leftPos, topPos, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float delta, int mouseX, int mouseY) {
        int bw = menu.guiParams.backgroundWidth;
        int bh = menu.guiParams.backgroundHeight;
        guiGraphics.blit(BACKGROUND, leftPos, topPos + 4, 0, 256 - bh + 4, bw, bh - 4);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, bw, 4);

        for (ClientComponentRenderer renderer : renderers) {
            renderer.renderBackground(guiGraphics, leftPos, topPos);
        }

        renderConfigurableSlotBackgrounds(guiGraphics);
    }

    private void renderConfigurableSlotBackgrounds(GuiGraphics guiGraphics) {
        for (Slot slot : this.menu.slots) {
            if (slot instanceof BackgroundRenderedSlot brs) {
                int px = leftPos + slot.x - 1;
                int py = topPos + slot.y - 1;
                guiGraphics.blit(SLOT_ATLAS, px, py, brs.getBackgroundU(), brs.getBackgroundV(), 18, 18);
            }
        }
    }

    @Override
    protected void renderSlot(GuiGraphics guiGraphics, Slot slot) {
        if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot cfs) {
            ConfigurableFluidStack stack = cfs.getConfStack();
            FluidVariant renderedKey = stack.getLockedInstance() == null ? stack.getResource() : FluidVariant.of(stack.getLockedInstance());
            if (!renderedKey.isBlank()) {
                RenderHelper.drawFluidInGui(guiGraphics, renderedKey, slot.x, slot.y);
            }
            return;
        }

        if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot itemSlot) {
            ConfigurableItemStack itemStack = itemSlot.getConfStack();
            if ((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getResource().isBlank()) {
                Item item = itemStack.getLockedInstance();
                if (item != Items.AIR) {
                    RenderHelper.renderAndDecorateItem(guiGraphics, font, new ItemStack(item), slot.x, slot.y, "0");
                }
            }
        }
        super.renderSlot(guiGraphics, slot);
    }

    private void renderConfigurableSlotTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Slot slot = hoveredSlot;
        if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
            ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
            FluidVariant renderedKey = stack.getLockedInstance() != null ? FluidVariant.of(stack.getLockedInstance()) : stack.getResource();
            List<Component> tooltip = new ArrayList<>(
                    FluidHelper.getTooltipForFluidStorage(renderedKey, stack.getAmount(), stack.getCapacity(), false));

            if (stack.canPlayerInsert()) {
                if (stack.canPlayerExtract()) {
                    tooltip.add(MIText.FluidSlotIO.text().setStyle(TextHelper.GRAY_TEXT));
                } else {
                    tooltip.add(MIText.FluidSlotInput.text().setStyle(TextHelper.GRAY_TEXT));
                }
            } else if (stack.canPlayerExtract()) {
                tooltip.add(MIText.FluidSlotOutput.text().setStyle(TextHelper.GRAY_TEXT));
            }
            guiGraphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
        } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot confSlot) {
            renderConfigurableItemStackTooltip(guiGraphics, confSlot.getConfStack(), mouseX, mouseY);
        } else if (slot != null && slot.hasItem()) {
            // regular tooltip
            guiGraphics.renderTooltip(font, slot.getItem(), mouseX, mouseY);
        }
    }

    private void renderConfigurableItemStackTooltip(GuiGraphics guiGraphics, ConfigurableItemStack stack, int mouseX, int mouseY) {
        ItemStack vanillaStack = stack.isEmpty() ? stack.getLockedInstance() == null ? ItemStack.EMPTY : new ItemStack(stack.getLockedInstance())
                : stack.getResource().toStack((int) stack.getAmount());
        // Regular information
        List<Component> textTooltip;
        if (vanillaStack.isEmpty()) {
            textTooltip = new ArrayList<>();
            textTooltip.add(MIText.Empty.text());
        } else {
            textTooltip = getTooltipFromItem(Minecraft.getInstance(), vanillaStack);
        }
        Optional<TooltipComponent> data = vanillaStack.getTooltipImage();
        // Append capacity
        var capacityText = Component.literal(String.valueOf(stack.getAdjustedCapacity()));
        if (stack.getAdjustedCapacity() != 64) {
            capacityText.setStyle(TextHelper.YELLOW_BOLD);
        }
        textTooltip.add(MIText.ConfigurableSlotCapacity.text(capacityText).setStyle(TextHelper.GRAY_TEXT));
        // Render
        guiGraphics.renderTooltip(font, textTooltip, data, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amountX, double amountY) {
        if (hoveredSlot instanceof ConfigurableItemStack.ConfigurableItemSlot confSlot) {
            ConfigurableItemStack stack = confSlot.getConfStack();
            boolean isIncrease = amountY > 0;
            boolean isShiftDown = hasShiftDown();
            // Client side update
            stack.adjustCapacity(isIncrease, isShiftDown);
            // Server side update
            new AdjustSlotCapacityPacket(menu.containerId, menu.slots.indexOf(hoveredSlot), isIncrease, isShiftDown)
                    .sendToServer();
            return true;
        }
        return false;
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
        return getExtraBoxes().stream().noneMatch(r -> r.contains(mouseX, mouseY))
                && super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop, mouseButton);
    }

    public MachineGuiParameters getGuiParams() {
        return menu.getGuiParams();
    }

    // This is used by the REI plugin to detect fluid slots
    public Slot getFocusedSlot() {
        return hoveredSlot;
    }

    public List<Rectangle> getExtraBoxes() {
        var list = new ArrayList<Rectangle>();
        for (var renderer : renderers) {
            renderer.addExtraBoxes(list, leftPos, topPos);
        }
        return list;
    }

    public class MachineButton extends Button {

        final ClientComponentRenderer.CustomButtonRenderer renderer;
        final Supplier<Boolean> isPresent;

        private MachineButton(int x, int y, int width, int height, OnPress onPress, Supplier<List<Component>> tooltipSupplier,
                ClientComponentRenderer.CustomButtonRenderer renderer, Supplier<Boolean> isPresent) {
            super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
            this.setTooltip(new DynamicTooltip(tooltipSupplier));
            this.renderer = renderer;
            this.isPresent = isPresent;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            this.visible = isPresent.get();
            super.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            renderer.renderButton(MachineScreen.this, this, guiGraphics, mouseX, mouseY, partialTick);
        }

        public void renderVanilla(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
            super.renderWidget(guiGraphics, mouseX, mouseY, delta);
        }
    }
}
