package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.factory.MachineFactory;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
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
        if(factory.hasProgressBar() && handler.getIsActive()) {
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

        this.client.getTextureManager().bindTexture(SLOT_ATLAS);
        for(int l = 0 ; l < factory.getSlots(); l++){
            int px = i+factory.getSlotPosX(l)-1;
            int py = j+factory.getSlotPosY(l)-1;
            if(!factory.isFluidSlot(l)){
                this.drawTexture(matrices, px, py, 0, 0, 18, 18);
            }else{
                this.drawTexture(matrices, px, py, 18, 0, 18, 18);
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}