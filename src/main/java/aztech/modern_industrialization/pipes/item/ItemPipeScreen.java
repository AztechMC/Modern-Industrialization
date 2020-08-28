package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

public class ItemPipeScreen extends HandledScreen<ItemPipeScreenHandler> {
    public ItemPipeScreen(ItemPipeScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(new MIIdentifier("textures/gui/pipe/item.png"));
        int i = this.x;
        int j = this.y;
        this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);

        // Whitelist/blacklist texture and transparent overlay if necessary
        this.drawTexture(matrices, i+148, j+34, 176, handler.pipeInterface.isWhitelist() ? 0 : 20, 20, 20);
        if(isPointWithinBounds(149, 35, 18, 18, mouseX, mouseY)) {
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);
            this.fillGradient(matrices, i+149, j+35, i+149+18, j+35+18, -2130706433, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
            RenderSystem.enableDepthTest();
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(isPointWithinBounds(148, 34, 20, 20, mouseX, mouseY)) {
            boolean newWhitelist = !handler.pipeInterface.isWhitelist();
            handler.pipeInterface.setWhitelist(newWhitelist);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(handler.syncId);
            buf.writeBoolean(newWhitelist);
            ClientSidePacketRegistry.INSTANCE.sendToServer(PipePackets.SET_ITEM_WHITELIST, buf);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
