package aztech.modern_industrialization.machines.impl;

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
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
    private Button[] buttons = new Button[] {
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
    };

    private static final Identifier SLOT_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");

    public MachineScreen(MachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
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
        if(factory.hasProgressBar()) {
            float progress = (float)handler.getTickProgress() / handler.getTickRecipe();

            int sx = factory.getProgressBarSizeX();
            int sy = factory.getProgressBarSizeY();

            int px = i+factory.getProgressBarDrawX();
            int py = j + factory.getProgressBarDrawY();

            int u = factory.getProgressBarX();
            int v = factory.getProgressBarY();

            if(factory.isProgressBarHorizontal()){
                int progressPixel = (int)(progress*sx);
                this.drawTexture(matrices, px, py, u, v, progressPixel, sy);
            }else if(factory.isProgressBarFlipped()){
                if(handler.getTickProgress() > 0) {
                    int progressPixel = (int) ((1 - progress) * sy);
                    this.drawTexture(matrices, px, py + progressPixel, u, v + progressPixel, sx, sy - progressPixel);
                }
            } else {
                int progressPixel = (int)(progress*sy);
                this.drawTexture(matrices, px, py, u, v, sx, progressPixel);
            }
        }
        if(factory.hasEfficiencyBar) {
            float efficiency = (float)handler.getEfficiencyTicks() / handler.getMaxEfficiencyTicks();
            int sx = factory.efficiencyBarSizeX;
            int sy = factory.efficiencyBarSizeY;
            int px = i+factory.efficiencyBarDrawX;
            int py = j+factory.efficiencyBarDrawY;
            int u = factory.efficiencyBarX;
            int v = factory.efficiencyBarY;
            int progressPixel = (int)(efficiency * sx);
            this.drawTexture(matrices, px, py, u, v, progressPixel, sy);
        }

        // TODO: move to custom screen class
        this.client.getTextureManager().bindTexture(SLOT_ATLAS);
        for(Slot slot : this.handler.slots) {
            int px = i + slot.x - 1;
            int py = j + slot.y - 1;
            int u;
            if(slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot = (ConfigurableFluidStack.ConfigurableFluidSlot) slot;
                u = fluidSlot.getConfStack().isPlayerLocked() ? 90 : fluidSlot.getConfStack().isMachineLocked() ? 126 : 18;
            } else if(slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                u = itemSlot.getConfStack().isPlayerLocked() ? 72 : itemSlot.getConfStack().isMachineLocked() ? 108 : 0;
            } else if(slot instanceof ConfigurableScreenHandler.LockingModeSlot) {
                u = this.handler.lockingMode ? 54 : 36;
            } else {
                continue;
            }
            this.drawTexture(matrices, px, py, u, 0, 18, 18);
        }
        for(Button button : buttons) {
            button.render(matrices, i, j);
        }
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        // Render items for locked slots
        for(Slot slot : this.handler.slots) {
            if(slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                ConfigurableItemStack itemStack = itemSlot.getConfStack();
                if((itemStack.isPlayerLocked() || itemStack.isMachineLocked()) && itemStack.getStack().isEmpty()) {
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
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(mouseButton == 0) {
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