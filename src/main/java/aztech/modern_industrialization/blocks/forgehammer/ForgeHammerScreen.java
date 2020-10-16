package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.ModernIndustrialization;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ForgeHammerScreen extends HandledScreen<ForgeHammerScreenHandler> {

    public static final Identifier FORGE_HAMMER_GUI = new Identifier(ModernIndustrialization.MOD_ID, "textures/gui/container/forge_hammer.png");
    private static final Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);

    private final int hammerSize = 30;
    private final int hammerX = 176;
    private final int hammerDrawX = 80;
    private final int hammerDrawY = 10;

    private Text tooltipHammer;
    private Text tooltipSaw;

    private ForgeHammerScreenHandler handler;

    public ForgeHammerScreen(ForgeHammerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.handler = handler;

        this.tooltipHammer = new TranslatableText("text:modern_industrialization:tooltip_hammer").setStyle(style);
        this.tooltipSaw = new TranslatableText("text:modern_industrialization:tooltip_saw").setStyle(style);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        this.renderBackground(matrices);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.client.getTextureManager().bindTexture(FORGE_HAMMER_GUI);
        this.drawTexture(matrices, this.x, this.y, 0, 0, 176, 166);

        if (this.isPointWithinBounds(hammerDrawX - 1, hammerDrawY - 1, hammerSize + 2, hammerSize + 2, mouseX, mouseY)) {
            this.drawTexture(matrices, this.x + hammerDrawX - 1, this.y + hammerDrawY - 1, hammerX, 2 * hammerSize, hammerSize + 2, hammerSize + 2);
        }

        if (this.handler.isHammer()) {
            this.drawTexture(matrices, this.x + hammerDrawX, this.y + hammerDrawY, hammerX, 0, hammerSize, hammerSize);
        } else {
            this.drawTexture(matrices, this.x + hammerDrawX, this.y + hammerDrawY, hammerX, hammerSize, hammerSize, hammerSize);
        }

        if (this.isPointWithinBounds(hammerDrawX - 1, hammerDrawY - 1, hammerSize + 2, hammerSize + 2, mouseX, mouseY)) {
            renderTooltip(matrices, this.handler.isHammer() ? tooltipHammer : tooltipSaw, mouseX, mouseY);
        }

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        super.drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isPointWithinBounds(hammerDrawX - 1, hammerDrawY - 1, hammerSize + 1, hammerSize + 1, mouseX, mouseY)) {
            boolean newHammer = !handler.isHammer();
            handler.setHammer(newHammer);
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            buf.writeInt(handler.syncId);
            buf.writeBoolean(newHammer);
            ClientSidePacketRegistry.INSTANCE.sendToServer(ForgeHammerPacket.SET_HAMMER, buf);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
