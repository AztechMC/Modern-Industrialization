package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
//import aztech.modern_industrialization.machines.steam.SteamBoilerBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Screen for the steam boiler.
 */
/*
public class SteamBoilerScreen extends HandledScreen<SteamBoilerScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/steam_boiler.png");
    private SteamBoilerScreenHandler handler;

    public SteamBoilerScreen(SteamBoilerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(TEXTURE);
        // Background
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
        // Fuel progress
        if(handler.getIsActive()) {
            int progressPixels = 12 - 12 * handler.getBurnTime() / handler.getTotalBurnTime();
            this.drawTexture(matrices, i + 46, j + 52 + progressPixels, 176, progressPixels, 14, 14 - progressPixels);
        }
        // Temperature
        int temperaturePixels = 46 * (handler.getTemperature() - SteamBoilerBlockEntity.MIN_TEMPERATURE) / (SteamBoilerBlockEntity.MAX_TEMPERATURE - SteamBoilerBlockEntity.MIN_TEMPERATURE);
        this.drawTexture(matrices, i + 37, j + 64 - temperaturePixels, 176, 60 - temperaturePixels, 2, temperaturePixels);
        // Arrow
        if(handler.getTemperature() > SteamBoilerBlockEntity.BOILING_TEMPERATURE) {
            this.drawTexture(matrices, i + 87, j + 39, 178, 14, 13, 19);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}*/
