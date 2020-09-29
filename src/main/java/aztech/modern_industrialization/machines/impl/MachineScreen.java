package aztech.modern_industrialization.machines.impl;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MachineScreen extends HandledScreen<MachineScreenHandler> {

    private MachineScreenHandler handler;

    private static final Identifier SLOT_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");
    private static final Style SECONDARY_INFO = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

    public MachineScreen(MachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
        this.backgroundHeight = handler.getMachineFactory().getBackgroundHeight();
        this.backgroundWidth = handler.getMachineFactory().getBackgroundWidth();
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    private int nextButtonX;
    private int buttonX() {
        nextButtonX -= 22;
        return nextButtonX + 22 + x;
    }

    private boolean hasLock() {
        for(ConfigurableItemStack stack : handler.inventory.getItemStacks()) {
            if(stack.canPlayerLock()) {
                return true;
            }
        }
        for(ConfigurableFluidStack stack : handler.inventory.getFluidStacks()) {
            if(stack.canPlayerLock()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasItemOutput() {
        for(ConfigurableItemStack stack : handler.inventory.getItemStacks()) {
            if(stack.canPipesExtract()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFluidOutput() {
        for(ConfigurableFluidStack stack : handler.inventory.getFluidStacks()) {
            if(stack.canPipesExtract()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void init() {
        super.init();
        nextButtonX = 152;
        if(hasLock()) {
            addButton(new MachineButton(buttonX(), 4 + y, 40, new LiteralText("slot locking"), b -> {
                boolean newLockingMode = !handler.lockingMode;
                handler.lockingMode = newLockingMode;
                PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                buf.writeInt(handler.syncId);
                buf.writeBoolean(newLockingMode);
                ClientSidePacketRegistry.INSTANCE.sendToServer(ConfigurableInventoryPackets.SET_LOCKING_MODE, buf);
            }, (button, matrices, mouseX, mouseY) -> {
                List<Text> lines = new ArrayList<>();
                if(handler.lockingMode) {
                    lines.add(new TranslatableText("text.modern_industrialization.locking_mode_on"));
                    lines.add(new TranslatableText("text.modern_industrialization.click_to_disable").setStyle(SECONDARY_INFO));
                } else {
                    lines.add(new TranslatableText("text.modern_industrialization.locking_mode_off"));
                    lines.add(new TranslatableText("text.modern_industrialization.click_to_enable").setStyle(SECONDARY_INFO));
                }
                renderTooltip(matrices, lines, mouseX, mouseY);
            }, () -> handler.lockingMode));
        }
        if(handler.inventory.hasOutput()) {
            if(hasFluidOutput()) {
                addButton(new MachineButton(buttonX(), 4 + y, 0, new LiteralText("fluid auto-extract"), b -> {
                    boolean newFluidExtract = !handler.inventory.getFluidExtract();
                    handler.inventory.setFluidExtract(newFluidExtract);
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(handler.syncId);
                    buf.writeBoolean(false);
                    buf.writeBoolean(newFluidExtract);
                    ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                }, (button, matrices, mouseX, mouseY) -> {
                    List<Text> lines = new ArrayList<>();
                    if(handler.inventory.getFluidExtract()) {
                        lines.add(new TranslatableText("text.modern_industrialization.fluid_auto_extract_on"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_disable").setStyle(SECONDARY_INFO));
                    } else {
                        lines.add(new TranslatableText("text.modern_industrialization.fluid_auto_extract_off"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_enable").setStyle(SECONDARY_INFO));
                    }
                    renderTooltip(matrices, lines, mouseX, mouseY);
                }, () -> handler.inventory.getFluidExtract()));
            }
            if(hasItemOutput()) {
                addButton(new MachineButton(buttonX(), 4 + y, 20, new LiteralText("item auto-extract"), b -> {
                    boolean newItemExtract = !handler.inventory.getItemExtract();
                    handler.inventory.setItemExtract(newItemExtract);
                    PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                    buf.writeInt(handler.syncId);
                    buf.writeBoolean(true);
                    buf.writeBoolean(newItemExtract);
                    ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                }, (button, matrices, mouseX, mouseY) -> {
                    List<Text> lines = new ArrayList<>();
                    if(handler.inventory.getItemExtract()) {
                        lines.add(new TranslatableText("text.modern_industrialization.item_auto_extract_on"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_disable").setStyle(SECONDARY_INFO));
                    } else {
                        lines.add(new TranslatableText("text.modern_industrialization.item_auto_extract_off"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_enable").setStyle(SECONDARY_INFO));
                    }
                    renderTooltip(matrices, lines, mouseX, mouseY);
                }, () -> handler.inventory.getItemExtract()));
            }
        }
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
    }

    protected void actualDrawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        MachineFactory factory = handler.getMachineFactory();
        this.client.getTextureManager().bindTexture(factory.getBackgroundIdentifier());
        // Background
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, factory.getBackgroundWidth(), factory.getBackgroundHeight());
        // Fuel progress
        if (factory.hasProgressBar()) {
            float progress = (float) handler.getTickProgress() / handler.getTickRecipe();

            int sx = factory.getProgressBarSizeX();
            int sy = factory.getProgressBarSizeY();

            int px = i + factory.getProgressBarDrawX();
            int py = j + factory.getProgressBarDrawY();

            int u = factory.getProgressBarX();
            int v = factory.getProgressBarY();

            if (factory.isProgressBarHorizontal()) {
                int progressPixel = (int) (progress * sx);
                this.drawTexture(matrices, px, py, u, v, progressPixel, sy);
            } else if (factory.isProgressBarFlipped()) {
                if (handler.getTickProgress() > 0) {
                    int progressPixel = (int) ((1 - progress) * sy);
                    this.drawTexture(matrices, px, py + progressPixel, u, v + progressPixel, sx, sy - progressPixel);
                }
            } else {
                int progressPixel = (int) (progress * sy);
                this.drawTexture(matrices, px, py, u, v, sx, progressPixel);
            }
        }
        if (factory.hasEfficiencyBar) {
            float efficiency = (float) handler.getEfficiencyTicks() / handler.getMaxEfficiencyTicks();
            int sx = factory.efficiencyBarSizeX;
            int sy = factory.efficiencyBarSizeY;
            int px = i + factory.efficiencyBarDrawX;
            int py = j + factory.efficiencyBarDrawY;
            int u = factory.efficiencyBarX;
            int v = factory.efficiencyBarY;
            int progressPixel = (int) (efficiency * sx);
            // background of the bar
            this.drawTexture(matrices, px-1, py-1, u, v + sy, sx+2, sy+2);
            // the bar itself
            this.drawTexture(matrices, px, py, u, v, progressPixel, sy);
        }

        this.client.getTextureManager().bindTexture(SLOT_ATLAS);
        if(factory.hasEnergyBar && handler.getMaxStoredEu() > 0) {
            int px = i + factory.electricityBarX;
            int py = j + factory.electricityBarY;
            int sx = 13; // FIXME: harcoded
            int sy = 18; // FIXME: harcoded
            this.drawTexture(matrices, px, py, 230, 0, sx, sy);
            float fill = (float) handler.getStoredEu() / handler.getMaxStoredEu();
            int fillPixels = (int) (fill * sy);
            if(fill > 0.95) fillPixels = sy;
            this.drawTexture(matrices, px, py + sy - fillPixels, 243, sy - fillPixels, sx, fillPixels);
        }

        for (Slot slot : this.handler.slots) {
            int px = i + slot.x - 1;
            int py = j + slot.y - 1;
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

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        // Render items for locked slots
        for (Slot slot : this.handler.slots) {
            if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                ConfigurableItemStack itemStack = itemSlot.getConfStack();
                if ((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getStack().isEmpty()) {
                    Item item = itemStack.getLockedItem();
                    if (item != Items.AIR) {
                        this.setZOffset(100);
                        this.itemRenderer.zOffset = 100.0F;

                        RenderSystem.enableDepthTest();
                        this.itemRenderer.renderInGuiWithOverrides(this.client.player, new ItemStack(item), slot.x, slot.y);
                        this.itemRenderer.renderGuiItemOverlay(this.textRenderer, new ItemStack(item), slot.x, slot.y, "0");

                        this.itemRenderer.zOffset = 0.0F;
                        this.setZOffset(0);
                    }
                }
            }
        }
        super.drawForeground(matrices, mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        actualDrawBackground(matrices, delta, mouseX, mouseY);

        // Render fluid slots
        for(Slot slot : handler.slots) {
            if(slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                int i = x + slot.x;
                int j = y + slot.y;

                ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                if(!stack.getFluid().isEmpty()) {
                    List<FluidRenderFace> faces = new ArrayList<>();
                    faces.add(FluidRenderFace.createFlatFaceZ(i, j, 0, i + 16, j + 16, 0, 1 / 16., false, false));
                    FluidVolume vol = stack.getFluid().withAmount(FluidAmount.of(stack.getAmount(), 1000));
                    vol.render(faces, FluidVolumeRenderer.VCPS, matrices);
                }
                RenderSystem.runAsFancy(FluidVolumeRenderer.VCPS::draw);

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

        super.render(matrices, mouseX, mouseY, delta);

        // Render fluid slot tooltips
        for(Slot slot : handler.slots) {
            if(isPointWithinBounds(slot.x, slot.y, 16, 16, mouseX, mouseY) && slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                List<Text> tooltip = new ArrayList<>();
                FluidKey fluid = stack.getFluid();
                if(fluid.isEmpty()){
                    tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty"));
                }else{
                    tooltip.add(fluid.name);
                }
                String quantity = stack.getAmount() + " / " + stack.getCapacity();
                tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity));

                Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

                if(stack.canPlayerInsert()) {
                    if (stack.canPlayerExtract()) {
                        tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_IO").setStyle(style));
                    } else {
                        tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_input").setStyle(style));
                    }
                } else if(stack.canPlayerExtract()) {
                    tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_output").setStyle(style));
                }
                this.renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }

        MachineFactory factory = handler.getMachineFactory();
        if(factory.hasEnergyBar && handler.getMaxStoredEu() > 0) {
            if(isPointWithinBounds(factory.electricityBarX, factory.electricityBarY, 13, 18, mouseX, mouseY)) { // FIXME: harcoded
                this.renderTooltip(matrices, Collections.singletonList(new TranslatableText("text.modern_industrialization.energy_bar", handler.getStoredEu(), handler.getMaxStoredEu())), mouseX, mouseY);
            }
        }

        if(factory.hasEfficiencyBar && factory.efficiencyBarDrawTooltip) {
            if(isPointWithinBounds(factory.efficiencyBarDrawX, factory.efficiencyBarDrawY, factory.efficiencyBarSizeX, factory.efficiencyBarSizeY, mouseX, mouseY)) {
                DecimalFormat factorFormat = new DecimalFormat("#.#");
                List<Text> tooltip = new ArrayList<>();
                if(handler.getMaxEfficiencyTicks() > 0) {
                    tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_ticks", handler.getEfficiencyTicks(), handler.getMaxEfficiencyTicks()));
                }
                if(handler.getRecipeEu() != 0) {
                    tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_factor", factorFormat.format((double) handler.getRecipeMaxEu() / handler.getRecipeEu())));
                } else {
                    tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_default_message"));
                }
                this.renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }

        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    private static class MachineButton extends ButtonWidget {
        private final int u;
        private final Supplier<Boolean> isPressed;
        private MachineButton(int x, int y, int u, Text message, PressAction onPress, TooltipSupplier tooltipSupplier, Supplier<Boolean> isPressed) {
            super(x, y, 20, 20, message, onPress, tooltipSupplier);
            this.u = u;
            this.isPressed = isPressed;
        }

        @Override
        public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            MinecraftClient minecraftClient = MinecraftClient.getInstance();
            minecraftClient.getTextureManager().bindTexture(SLOT_ATLAS);

            int v = 18;
            if(isPressed.get()) {
                v += 20;
            }
            drawTexture(matrices, x, y, u, v, 20, 20);
            if(isHovered()) {
                drawTexture(matrices, x, y, 60, 18, 20, 20);
                this.renderToolTip(matrices, mouseX, mouseY);
            }
        }
    }
}