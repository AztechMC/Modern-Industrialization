package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MachineScreen extends HandledScreen<MachineScreenHandler> {

    private MachineScreenHandler handler;

    private static final Identifier SLOT_ATLAS = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/slot_atlas.png");

    public MachineScreen(MachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
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
            }else{
                int progressPixel = (int)(progress*sy);
                this.drawTexture(matrices, px, py, u, v, sx, progressPixel);
            }
        }

        // TOOD: move to custom screen class
        this.client.getTextureManager().bindTexture(SLOT_ATLAS);
        for(Slot slot : this.handler.slots) {
            int px = i + slot.x - 1;
            int py = j + slot.y - 1;
            int u;
            if(slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                ConfigurableFluidStack.ConfigurableFluidSlot fluidSlot = (ConfigurableFluidStack.ConfigurableFluidSlot) slot;
                u = fluidSlot.getConfStack().isVisiblyLocked() ? 90 : 18;
            } else if(slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                ConfigurableItemStack.ConfigurableItemSlot itemSlot = (ConfigurableItemStack.ConfigurableItemSlot) slot;
                u = itemSlot.getConfStack().isVisiblyLocked() ? 72 : 0;
            } else if(slot instanceof ConfigurableScreenHandler.LockingModeSlot) {
                u = this.handler.lockingMode ? 54 : 36;
            } else {
                continue;
            }
            this.drawTexture(matrices, px, py, u, 0, 18, 18);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}