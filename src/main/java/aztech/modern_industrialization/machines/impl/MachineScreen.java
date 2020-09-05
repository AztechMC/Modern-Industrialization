package aztech.modern_industrialization.machines.impl;

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace;
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MachineScreen extends HandledScreen<MachineScreenHandler> {

    private MachineScreenHandler handler;

    private interface Button {
        int getX();

        int getY();

        int getSizeX();

        int getSizeY();

        void clicked();

        void render(MatrixStack matrices, int i, int j);
    }

    private Button[] buttons;

    private static final Identifier SLOT_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");

    public MachineScreen(MachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;

        this.buttons = handler.inventory.hasOutput() ? new Button[]{
                new Button() {
                    @Override
                    public int getX() {
                        return 112;
                    }

                    @Override
                    public int getY() {
                        return 6;
                    }

                    @Override
                    public int getSizeX() {
                        return 18;
                    }

                    @Override
                    public int getSizeY() {
                        return 18;
                    }

                    @Override
                    public void clicked() {
                        boolean newItemExtract = !handler.inventory.getItemExtract();
                        handler.inventory.setItemExtract(newItemExtract);
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeInt(handler.syncId);
                        buf.writeBoolean(true);
                        buf.writeBoolean(newItemExtract);
                        ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                    }

                    @Override
                    public void render(MatrixStack matrices, int i, int j) {
                        drawTexture(matrices, i + getX(), j + getY(), handler.inventory.getItemExtract() ? 54 : 36, 18, 18, 18);
                    }
                },
                new Button() {
                    @Override
                    public int getX() {
                        return 132;
                    }

                    @Override
                    public int getY() {
                        return 6;
                    }

                    @Override
                    public int getSizeX() {
                        return 18;
                    }

                    @Override
                    public int getSizeY() {
                        return 18;
                    }

                    @Override
                    public void clicked() {
                        boolean newFluidExtract = !handler.inventory.getFluidExtract();
                        handler.inventory.setFluidExtract(newFluidExtract);
                        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
                        buf.writeInt(handler.syncId);
                        buf.writeBoolean(false);
                        buf.writeBoolean(newFluidExtract);
                        ClientSidePacketRegistry.INSTANCE.sendToServer(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                    }

                    @Override
                    public void render(MatrixStack matrices, int i, int j) {
                        drawTexture(matrices, i + getX(), j + getY(), handler.inventory.getFluidExtract() ? 18 : 0, 18, 18, 18);
                    }
                }
        } : new Button[0];
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
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
            } else if (slot instanceof ConfigurableScreenHandler.LockingModeSlot) {
                u = this.handler.lockingMode ? 54 : 36;
            } else {
                continue;
            }
            this.drawTexture(matrices, px, py, u, 0, 18, 18);
        }
        for (Button button : buttons) {
            button.render(matrices, i, j);
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
        super.render(matrices, mouseX, mouseY, delta);

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

        if(factory.hasEfficiencyBar) {
            if(isPointWithinBounds(factory.efficiencyBarDrawX, factory.efficiencyBarDrawY, factory.efficiencyBarSizeX, factory.efficiencyBarSizeY, mouseX, mouseY)) {
                DecimalFormat factorFormat = new DecimalFormat("#.#");
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_ticks", handler.getEfficiencyTicks(), handler.getMaxEfficiencyTicks()));
                tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_factor", factorFormat.format(MachineBlockEntity.getOverclock(factory.tier, handler.getEfficiencyTicks()))));
                this.renderTooltip(matrices, tooltip, mouseX, mouseY);
            }
        }

        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (Button button : buttons) {
                if (isPointWithinBounds(button.getX(), button.getY(), button.getSizeX(), button.getSizeY(), mouseX, mouseY)) {
                    button.clicked();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}