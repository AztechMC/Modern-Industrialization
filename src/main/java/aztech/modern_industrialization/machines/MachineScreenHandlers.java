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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.client.screen.MIHandledScreen;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack.ConfigurableFluidSlot;
import aztech.modern_industrialization.inventory.ConfigurableItemStack.ConfigurableItemSlot;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({ "rawtypes" })
public class MachineScreenHandlers {
    public static abstract class Common extends ConfigurableScreenHandler {
        public final MachineGuiParameters guiParams;

        Common(int syncId, Inventory playerInventory, MIInventory inventory, MachineGuiParameters guiParams) {
            super(ModernIndustrialization.SCREEN_HANDLER_MACHINE, syncId, playerInventory, inventory);
            this.guiParams = guiParams;

            // Player inventory slots
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 9; j++) {
                    this.addSlot(new Slot(playerInventory, i * 9 + j + 9, guiParams.playerInventoryX + j * 18, guiParams.playerInventoryY + i * 18));
                }
            }
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j, guiParams.playerInventoryX + j * 18, guiParams.playerInventoryY + 58));
            }

            // Configurable slots
            for (int i = 0; i < inventory.getItemStacks().size(); ++i) {
                ConfigurableItemStack stack = inventory.getItemStacks().get(i);
                // FIXME: markDirty and insert predicate
                this.addSlot(stack.new ConfigurableItemSlot(() -> {
                }, inventory.itemPositions.getX(i), inventory.itemPositions.getY(i), s -> true));
            }
            for (int i = 0; i < inventory.getFluidStacks().size(); ++i) {
                ConfigurableFluidStack stack = inventory.getFluidStacks().get(i);
                // FIXME: markDirty
                this.addSlot(stack.new ConfigurableFluidSlot(() -> {
                }, inventory.fluidPositions.getX(i), inventory.fluidPositions.getY(i)));
            }
        }
    }

    static class Server extends Common {
        public final MachineBlockEntity blockEntity;
        protected final List trackedData;

        Server(int syncId, Inventory playerInventory, MachineBlockEntity blockEntity, MachineGuiParameters guiParams) {
            super(syncId, playerInventory, blockEntity.getInventory(), guiParams);
            this.blockEntity = blockEntity;
            trackedData = new ArrayList<>();
            for (SyncedComponent.Server component : blockEntity.syncedComponents) {
                trackedData.add(component.copyData());
            }
        }

        @Override
        public void broadcastChanges() {
            super.broadcastChanges();
            for (int i = 0; i < blockEntity.syncedComponents.size(); ++i) {
                SyncedComponent.Server component = blockEntity.syncedComponents.get(i);
                if (component.needsSync(trackedData.get(i))) {
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(containerId);
                    buf.writeInt(i);
                    component.writeCurrentData(buf);
                    ServerPlayNetworking.send((ServerPlayer) playerInventory.player, MachinePackets.S2C.COMPONENT_SYNC, buf);
                    trackedData.set(i, component.copyData());
                }
            }
        }

        @Override
        public boolean stillValid(Player player) {
            BlockPos pos = blockEntity.getBlockPos();
            if (player.level.getBlockEntity(pos) != blockEntity) {
                return false;
            } else {
                return player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static Client createClient(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        // Inventory
        int itemStackCount = buf.readInt();
        int fluidStackCount = buf.readInt();
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        CompoundTag tag = buf.readNbt();
        NbtHelper.getList(tag, "items", itemStacks, ConfigurableItemStack::new);
        NbtHelper.getList(tag, "fluids", fluidStacks, ConfigurableFluidStack::new);
        // Slot positions
        SlotPositions itemPositions = SlotPositions.read(buf);
        SlotPositions fluidPositions = SlotPositions.read(buf);
        MIInventory inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);
        // Components
        List<SyncedComponent.Client> components = new ArrayList<>();
        int componentCount = buf.readInt();
        for (int i = 0; i < componentCount; ++i) {
            ResourceLocation id = buf.readResourceLocation();
            components.add(SyncedComponents.Client.get(id).createFromInitialData(buf));
        }
        // GUI params
        MachineGuiParameters guiParams = MachineGuiParameters.read(buf);

        return new Client(syncId, playerInventory, inventory, components, guiParams);
    }

    public static class Client extends Common {
        public final List<SyncedComponent.Client> components;

        Client(int syncId, Inventory playerInventory, MIInventory inventory, List<SyncedComponent.Client> components,
                MachineGuiParameters guiParams) {
            super(syncId, playerInventory, inventory, guiParams);
            this.components = components;
        }

        @Nullable
        public <T extends SyncedComponent.Client> T getComponent(Class<T> klass) {
            for (SyncedComponent.Client component : components) {
                if (klass.isInstance(component)) {
                    return (T) component;
                }
            }
            return null;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    public static final ResourceLocation SLOT_ATLAS = new ResourceLocation(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");

    public static class ClientScreen extends MIHandledScreen<Client> implements ClientComponentRenderer.ButtonContainer {
        private final List<ClientComponentRenderer> renderers = new ArrayList<>();

        public ClientScreen(Client handler, Inventory inventory, Component title) {
            super(handler, inventory, title);

            for (SyncedComponent.Client component : handler.components) {
                renderers.add(component.createRenderer());
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

        @Override
        public void addButton(int u, Component message, Consumer<Integer> pressAction, Supplier<List<Component>> tooltipSupplier,
                Supplier<Boolean> isPressed) {

            addRenderableWidget(new MachineButton(buttonX(), buttonY(), 20, 20, message, b -> pressAction.accept(menu.containerId),
                    (button, matrices, mouseX, mouseY) -> renderComponentTooltip(matrices, tooltipSupplier.get(), mouseX, mouseY),

                    (screen, button, matrices, mouseX, mouseY, delta) -> {
                        RenderSystem.setShaderTexture(0, SLOT_ATLAS);
                        int v = 18;
                        if (isPressed.get()) {
                            v += 20;
                        }
                        blit(matrices, button.x, button.y, u, v, 20, 20);
                        if (button.isHoveredOrFocused()) {
                            blit(matrices, button.x, button.y, 60, 18, 20, 20);
                            button.renderToolTip(matrices, mouseX, mouseY);
                        }
                    }));

        }

        @Override
        public void addButton(int posX, int posY, int width, int height, Component message, Consumer<Integer> pressAction,
                Supplier<List<Component>> tooltipSupplier, ClientComponentRenderer.CustomButtonRenderer renderer, Supplier<Boolean> isButtonPresent) {

            addRenderableWidget(new MachineButton(posX + leftPos, posY + topPos, width, height, message, b -> {
                if (isButtonPresent.get())
                    pressAction.accept(menu.containerId);
            }, (button, matrices, mouseX, mouseY) -> {
                if (isButtonPresent.get())
                    renderComponentTooltip(matrices, tooltipSupplier.get(), mouseX, mouseY);
            }, (screen, button, matrices, mouseX, mouseY, delta) -> {
                if (isButtonPresent.get()) {
                    renderer.renderButton(screen, button, matrices, mouseX, mouseY, delta);
                }
            }) {
            });
        }

        private void addLockButton() {
            addButton(40, new TextComponent("slot locking"), syncId -> {
                boolean newLockingMode = !menu.lockingMode;
                menu.lockingMode = newLockingMode;
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeBoolean(newLockingMode);
                ClientPlayNetworking.send(ConfigurableInventoryPackets.SET_LOCKING_MODE, buf);
            }, () -> {
                List<Component> lines = new ArrayList<>();
                if (menu.lockingMode) {
                    lines.add(MIText.LockingModeOn.text());
                    lines.add(MIText.ClickToDisable.text().setStyle(TextHelper.GRAY_TEXT));
                } else {
                    lines.add(MIText.LockingModeOff.text());
                    lines.add(MIText.ClickToEnable.text().setStyle(TextHelper.GRAY_TEXT));
                }
                return lines;
            }, () -> menu.lockingMode);
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
            // Shadow around the GUI
            renderBackground(matrices);
            RenderSystem.enableBlend();
            // Background
            actualDrawBackground(matrices);
            renderConfigurableSlotBackgrounds(matrices);
            // Locked items and fluids
            renderFluidSlots(matrices, mouseX, mouseY);
            renderLockedItems();
            // Regular items and the foreground
            super.render(matrices, mouseX, mouseY, delta);
            // Tooltips
            renderConfigurableSlotTooltips(matrices, mouseX, mouseY);
            for (ClientComponentRenderer renderer : renderers) {
                renderer.renderTooltip(this, matrices, leftPos, topPos, mouseX, mouseY);
            }
        }

        // drawBackground() is called too late, so it's not used at all.
        // This function is used by our custom render() function when appropriate.
        private void actualDrawBackground(PoseStack matrices) {
            RenderSystem.setShaderTexture(0, new MIIdentifier("textures/gui/container/background.png"));
            int bw = menu.guiParams.backgroundWidth;
            int bh = menu.guiParams.backgroundHeight;
            blit(matrices, leftPos, topPos + 4, 0, 256 - bh + 4, bw, bh - 4);
            blit(matrices, leftPos, topPos, 0, 0, bw, 4);

            for (ClientComponentRenderer renderer : renderers) {
                renderer.renderBackground(this, matrices, leftPos, topPos);
            }
        }

        private void renderConfigurableSlotBackgrounds(PoseStack matrices) {
            RenderSystem.setShaderTexture(0, SLOT_ATLAS);
            for (Slot slot : this.menu.slots) {
                int px = leftPos + slot.x - 1;
                int py = topPos + slot.y - 1;
                int u;
                if (slot instanceof ConfigurableFluidSlot fluidSlot) {
                    u = fluidSlot.getConfStack().isPlayerLocked() ? 90 : fluidSlot.getConfStack().isMachineLocked() ? 126 : 18;
                } else if (slot instanceof ConfigurableItemSlot itemSlot) {
                    u = itemSlot.getConfStack().isPlayerLocked() ? 72 : itemSlot.getConfStack().isMachineLocked() ? 108 : 0;
                } else {
                    continue;
                }
                this.blit(matrices, px, py, u, 0, 18, 18);
            }
        }

        private void renderFluidSlots(PoseStack matrices, int mouseX, int mouseY) {
            for (Slot slot : menu.slots) {
                if (slot instanceof ConfigurableFluidSlot) {
                    int i = leftPos + slot.x;
                    int j = topPos + slot.y;

                    ConfigurableFluidStack stack = ((ConfigurableFluidSlot) slot).getConfStack();
                    FluidVariant renderedKey = stack.getLockedInstance() == null ? stack.getResource() : FluidVariant.of(stack.getLockedInstance());
                    if (!renderedKey.isBlank()) {
                        RenderHelper.drawFluidInGui(matrices, renderedKey, i, j);
                    }

                    if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.isActive()) {
                        this.hoveredSlot = slot;
                        RenderSystem.disableDepthTest();
                        RenderSystem.colorMask(true, true, true, false);
                        this.fillGradient(matrices, i, j, i + 16, j + 16, -2130706433, -2130706433);
                        RenderSystem.colorMask(true, true, true, true);
                        RenderSystem.enableDepthTest();
                    }
                }
            }
        }

        private void renderLockedItems() {
            for (Slot slot : this.menu.slots) {
                if (slot instanceof ConfigurableItemSlot itemSlot) {
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    if ((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getResource().isBlank()) {
                        Item item = itemStack.getLockedInstance();
                        if (item != Items.AIR) {
                            renderItemInGui(new ItemStack(item), slot.x + this.leftPos, slot.y + this.topPos, "0");
                        }
                    }
                }
            }
        }

        public void renderItemInGui(ItemStack itemStack, int x, int y) {
            renderItemInGui(itemStack, x, y, null);
        }

        public void renderItemInGui(ItemStack itemStack, int x, int y, String countLabel) {
            this.setBlitOffset(100);
            this.itemRenderer.blitOffset = 100.0F;

            RenderSystem.enableDepthTest();
            this.itemRenderer.renderAndDecorateItem(this.minecraft.player, itemStack, x, y, 0);
            this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, x, y, countLabel);

            this.itemRenderer.blitOffset = 0.0F;
            this.setBlitOffset(0);
        }

        public ItemRenderer getItemRenderer() {
            return this.itemRenderer;
        }

        private void renderConfigurableSlotTooltips(PoseStack matrices, int mouseX, int mouseY) {
            Slot slot = hoveredSlot;
            if (slot instanceof ConfigurableFluidSlot) {
                ConfigurableFluidStack stack = ((ConfigurableFluidSlot) slot).getConfStack();
                FluidVariant renderedKey = stack.isPlayerLocked() ? FluidVariant.of(stack.getLockedInstance()) : stack.getResource();
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
                this.renderComponentTooltip(matrices, tooltip, mouseX, mouseY);
            } else if (slot instanceof ConfigurableItemSlot confSlot) {
                renderConfigurableItemStackTooltip(matrices, confSlot.getConfStack(), mouseX, mouseY);
            } else if (slot != null && slot.hasItem()) {
                // regular tooltip
                renderTooltip(matrices, slot.getItem(), mouseX, mouseY);
            }
        }

        private void renderConfigurableItemStackTooltip(PoseStack matrices, ConfigurableItemStack stack, int mouseX, int mouseY) {
            ItemStack vanillaStack = stack.isEmpty() ? stack.getLockedInstance() == null ? ItemStack.EMPTY : new ItemStack(stack.getLockedInstance())
                    : stack.getResource().toStack((int) stack.getAmount());
            // Regular information
            List<Component> textTooltip;
            if (vanillaStack.isEmpty()) {
                textTooltip = new ArrayList<>();
                textTooltip.add(MIText.Empty.text());
            } else {
                textTooltip = getTooltipFromItem(vanillaStack);
            }
            Optional<TooltipComponent> data = vanillaStack.getTooltipImage();
            // Append capacity
            TextComponent capacityText = new TextComponent(String.valueOf(stack.getAdjustedCapacity()));
            if (stack.getAdjustedCapacity() != 64) {
                capacityText.setStyle(TextHelper.YELLOW_BOLD);
            }
            textTooltip.add(
                    MIText.ConfigurableSlotCapacity.text(capacityText)
                            .setStyle(TextHelper.GRAY_TEXT));
            // Render
            renderTooltip(matrices, textTooltip, data, mouseX, mouseY);
        }

        @Override
        protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
            if (hoveredSlot instanceof ConfigurableItemSlot confSlot) {
                ConfigurableItemStack stack = confSlot.getConfStack();
                boolean isIncrease = amount > 0;
                boolean isShiftDown = hasShiftDown();
                // Client side update
                stack.adjustCapacity(isIncrease, isShiftDown);
                // Server side update
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(menu.containerId);
                buf.writeVarInt(menu.slots.indexOf(hoveredSlot));
                buf.writeBoolean(isIncrease);
                buf.writeBoolean(isShiftDown);
                ClientPlayNetworking.send(ConfigurableInventoryPackets.ADJUST_SLOT_CAPACITY, buf);
                return true;
            }
            return false;
        }

        // This is used by the REI plugin to detect fluid slots
        public Slot getFocusedSlot() {
            return hoveredSlot;
        }

        public class MachineButton extends Button {

            final ClientComponentRenderer.CustomButtonRenderer renderer;

            private MachineButton(int x, int y, int width, int height, Component message, OnPress onPress, OnTooltip tooltipSupplier,
                    ClientComponentRenderer.CustomButtonRenderer renderer) {
                super(x, y, width, height, message, onPress, tooltipSupplier);
                this.renderer = renderer;
            }

            @Override
            public void renderButton(PoseStack matrices, int mouseX, int mouseY, float delta) {
                renderer.renderButton(ClientScreen.this, this, matrices, mouseX, mouseY, delta);
            }

            public void renderVanilla(PoseStack matrices, int mouseX, int mouseY, float delta) {
                super.renderButton(matrices, mouseX, mouseY, delta);
            }
        }
    }
}
