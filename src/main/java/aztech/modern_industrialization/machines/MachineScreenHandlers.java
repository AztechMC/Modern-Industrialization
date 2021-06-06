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
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.*;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MachineScreenHandlers {
    public static abstract class Common extends ConfigurableScreenHandler {
        public final MachineGuiParameters guiParams;

        Common(int syncId, PlayerInventory playerInventory, MIInventory inventory, MachineGuiParameters guiParams) {
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

        Server(int syncId, PlayerInventory playerInventory, MachineBlockEntity blockEntity, MachineGuiParameters guiParams) {
            super(syncId, playerInventory, blockEntity.getInventory(), guiParams);
            this.blockEntity = blockEntity;
            trackedData = new ArrayList<>();
            for (SyncedComponent.Server component : blockEntity.syncedComponents) {
                trackedData.add(component.copyData());
            }
        }

        @Override
        public void sendContentUpdates() {
            super.sendContentUpdates();
            for (int i = 0; i < blockEntity.syncedComponents.size(); ++i) {
                SyncedComponent.Server component = blockEntity.syncedComponents.get(i);
                if (component.needsSync(trackedData.get(i))) {
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(syncId);
                    buf.writeInt(i);
                    component.writeCurrentData(buf);
                    ServerPlayNetworking.send((ServerPlayerEntity) playerInventory.player, MachinePackets.S2C.COMPONENT_SYNC, buf);
                    trackedData.set(i, component.copyData());
                }
            }
        }

        @Override
        public boolean canUse(PlayerEntity player) {
            BlockPos pos = blockEntity.getPos();
            if (player.world.getBlockEntity(pos) != blockEntity) {
                return false;
            } else {
                return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static Client createClient(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        // Inventory
        int itemStackCount = buf.readInt();
        int fluidStackCount = buf.readInt();
        List<ConfigurableItemStack> itemStacks = new ArrayList<>();
        List<ConfigurableFluidStack> fluidStacks = new ArrayList<>();
        CompoundTag tag = buf.readCompoundTag();
        NbtHelper.getList(tag, "items", itemStacks, ConfigurableItemStack::fromNbt);
        NbtHelper.getList(tag, "fluids", fluidStacks, ConfigurableFluidStack::fromNbt);
        // Slot positions
        SlotPositions itemPositions = SlotPositions.read(buf);
        SlotPositions fluidPositions = SlotPositions.read(buf);
        MIInventory inventory = new MIInventory(itemStacks, fluidStacks, itemPositions, fluidPositions);
        // Components
        List<SyncedComponent.Client> components = new ArrayList<>();
        int componentCount = buf.readInt();
        for (int i = 0; i < componentCount; ++i) {
            Identifier id = buf.readIdentifier();
            components.add(SyncedComponents.Client.get(id).createFromInitialData(buf));
        }
        // GUI params
        MachineGuiParameters guiParams = MachineGuiParameters.read(buf);

        return new Client(syncId, playerInventory, inventory, components, guiParams);
    }

    public static class Client extends Common {
        public final List<SyncedComponent.Client> components;

        Client(int syncId, PlayerInventory playerInventory, MIInventory inventory, List<SyncedComponent.Client> components,
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
        public boolean canUse(PlayerEntity player) {
            return true;
        }
    }

    public static final Identifier SLOT_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");

    public static class ClientScreen extends HandledScreen<Client> implements ClientComponentRenderer.ButtonContainer {
        private final List<ClientComponentRenderer> renderers = new ArrayList<>();

        public ClientScreen(Client handler, PlayerInventory inventory, Text title) {
            super(handler, inventory, title);

            for (SyncedComponent.Client component : handler.components) {
                renderers.add(component.createRenderer());
            }

            this.backgroundHeight = handler.guiParams.backgroundHeight;
            this.backgroundWidth = handler.guiParams.backgroundWidth;
            this.playerInventoryTitleY = this.backgroundHeight - 94;
        }

        public int x() {
            return x;
        }

        public int y() {
            return y;
        }

        private int nextButtonX;
        private static final int BUTTON_Y = 4;

        private int buttonX() {
            nextButtonX -= 22;
            return nextButtonX + 22 + x;
        }

        private int buttonY() {
            return BUTTON_Y + y;
        }

        @Override
        protected void init() {
            super.init();
            this.nextButtonX = 152;
            if (handler.guiParams.lockButton) {
                addLockButton();
            }

            for (ClientComponentRenderer renderer : renderers) {
                renderer.addButtons(this);
            }
        }

        @Override
        public void addButton(int u, Text message, Consumer<Integer> pressAction, Supplier<List<Text>> tooltipSupplier, Supplier<Boolean> isPressed) {
            addButton(new MachineButton(buttonX(), buttonY(), u, message, b -> pressAction.accept(handler.syncId),
                    (button, matrices, mouseX, mouseY) -> {
                        renderTooltip(matrices, tooltipSupplier.get(), mouseX, mouseY);
                    }, isPressed));
        }

        private void addLockButton() {
            addButton(40, new LiteralText("slot locking"), syncId -> {
                boolean newLockingMode = !handler.lockingMode;
                handler.lockingMode = newLockingMode;
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeBoolean(newLockingMode);
                ClientPlayNetworking.send(ConfigurableInventoryPackets.SET_LOCKING_MODE, buf);
            }, () -> {
                List<Text> lines = new ArrayList<>();
                if (handler.lockingMode) {
                    lines.add(new TranslatableText("text.modern_industrialization.locking_mode_on"));
                    lines.add(new TranslatableText("text.modern_industrialization.click_to_disable").setStyle(TextHelper.GRAY_TEXT));
                } else {
                    lines.add(new TranslatableText("text.modern_industrialization.locking_mode_off"));
                    lines.add(new TranslatableText("text.modern_industrialization.click_to_enable").setStyle(TextHelper.GRAY_TEXT));
                }
                return lines;
            }, () -> handler.lockingMode);
        }

        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            // Shadow around the GUI
            renderBackground(matrices);
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
                renderer.renderTooltip(this, matrices, x, y, mouseX, mouseY);
            }
            drawMouseoverTooltip(matrices, mouseX, mouseY);
        }

        // drawBackground() is called too late, so it's not used at all.
        // This function is used by our custom render() function when appropriate.
        private void actualDrawBackground(MatrixStack matrices) {
            this.client.getTextureManager().bindTexture(new MIIdentifier("textures/gui/container/background.png"));
            int bw = handler.guiParams.backgroundWidth;
            int bh = handler.guiParams.backgroundHeight;
            drawTexture(matrices, x, y + 4, 0, 256 - bh + 4, bw, bh - 4);
            drawTexture(matrices, x, y, 0, 0, bw, 4);

            for (ClientComponentRenderer renderer : renderers) {
                renderer.renderBackground(this, matrices, x, y);
            }
        }

        private void renderConfigurableSlotBackgrounds(MatrixStack matrices) {
            this.client.getTextureManager().bindTexture(SLOT_ATLAS);
            for (Slot slot : this.handler.slots) {
                int px = x + slot.x - 1;
                int py = y + slot.y - 1;
                int u;
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot = (ConfigurableFluidStack.ConfigurableFluidSlot) slot;
                    u = fluidSlot.getConfStack().isPlayerLocked() ? 90 : fluidSlot.getConfStack().isMachineLocked() ? 126 : 18;
                } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    u = itemSlot.getConfStack().isPlayerLocked() ? 72 : itemSlot.getConfStack().isMachineLocked() ? 108 : 0;
                } else {
                    continue;
                }
                this.drawTexture(matrices, px, py, u, 0, 18, 18);
            }
        }

        private void renderFluidSlots(MatrixStack matrices, int mouseX, int mouseY) {
            for (Slot slot : handler.slots) {
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                    int i = x + slot.x;
                    int j = y + slot.y;

                    ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                    if (!stack.getFluid().isEmpty()) {
                        RenderHelper.drawFluidInGui(matrices, stack.getFluid(), i, j);
                    }

                    if (isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot.doDrawHoveringEffect()) {
                        this.focusedSlot = slot;
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
            for (Slot slot : this.handler.slots) {
                if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                    ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                    ConfigurableItemStack itemStack = itemSlot.getConfStack();
                    if ((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getItemKey().isEmpty()) {
                        Item item = itemStack.getLockedItem();
                        if (item != Items.AIR) {
                            this.setZOffset(100);
                            this.itemRenderer.zOffset = 100.0F;

                            RenderSystem.enableDepthTest();
                            this.itemRenderer.renderInGuiWithOverrides(this.client.player, new ItemStack(item), slot.x + this.x, slot.y + this.y);
                            this.itemRenderer.renderGuiItemOverlay(this.textRenderer, new ItemStack(item), slot.x + this.x, slot.y + this.y, "0");

                            this.itemRenderer.zOffset = 0.0F;
                            this.setZOffset(0);
                        }
                    }
                }
            }
        }

        private void renderConfigurableSlotTooltips(MatrixStack matrices, int mouseX, int mouseY) {
            for (Slot slot : handler.slots) {
                if (isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                        ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                        List<Text> tooltip = new ArrayList<>();
                        tooltip.add(FluidHelper.getFluidName(stack.getFluid(), false));
                        tooltip.add(FluidHelper.getFluidAmount(stack.getAmount(), stack.getCapacity()));

                        if (stack.canPlayerInsert()) {
                            if (stack.canPlayerExtract()) {
                                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_IO").setStyle(TextHelper.GRAY_TEXT));
                            } else {
                                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_input").setStyle(TextHelper.GRAY_TEXT));
                            }
                        } else if (stack.canPlayerExtract()) {
                            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_output").setStyle(TextHelper.GRAY_TEXT));
                        }
                        this.renderTooltip(matrices, tooltip, mouseX, mouseY);
                    } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                        ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
                        if (stack.getItemKey().isEmpty() && stack.getLockedItem() != null) {
                            this.renderTooltip(matrices, new ItemStack(stack.getLockedItem()), mouseX, mouseY);
                        }
                    }
                }
            }
        }

        @Override
        protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        }

        // This is used by the REI plugin to detect fluid slots
        public Slot getFocusedSlot() {
            return focusedSlot;
        }

        private static class MachineButton extends ButtonWidget {
            private final int u;
            private final Supplier<Boolean> isPressed;

            private MachineButton(int x, int y, int u, Text message, PressAction onPress, TooltipSupplier tooltipSupplier,
                    Supplier<Boolean> isPressed) {
                super(x, y, 20, 20, message, onPress, tooltipSupplier);
                this.u = u;
                this.isPressed = isPressed;
            }

            @Override
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                MinecraftClient minecraftClient = MinecraftClient.getInstance();
                minecraftClient.getTextureManager().bindTexture(SLOT_ATLAS);

                int v = 18;
                if (isPressed.get()) {
                    v += 20;
                }
                drawTexture(matrices, x, y, u, v, 20, 20);
                if (isHovered()) {
                    drawTexture(matrices, x, y, 60, 18, 20, 20);
                    this.renderToolTip(matrices, mouseX, mouseY);
                }
            }
        }
    }
}
