package aztech.modern_industrialization.machinesv2.gui;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MachineGuiParameters {
    public final Text title;
    public final int playerInventoryX, playerInventoryY;
    public final Identifier backgroundTexture;
    public final int backgroundWidth, backgroundHeight;
    public final boolean lockButton;

    private MachineGuiParameters(Text title, int playerInventoryX, int playerInventoryY, Identifier backgroundTexture, int backgroundWidth, int backgroundHeight, boolean lockButton) {
        this.title = title;
        this.playerInventoryX = playerInventoryX;
        this.playerInventoryY = playerInventoryY;
        this.backgroundTexture = backgroundTexture;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        this.lockButton = lockButton;
    }

    public void write(PacketByteBuf buf) {
        buf.writeText(title);
        buf.writeInt(playerInventoryX);
        buf.writeInt(playerInventoryY);
        buf.writeIdentifier(backgroundTexture);
        buf.writeInt(backgroundWidth);
        buf.writeInt(backgroundHeight);
        buf.writeBoolean(lockButton);
    }

    public static MachineGuiParameters read(PacketByteBuf buf) {
        return new MachineGuiParameters(buf.readText(), buf.readInt(), buf.readInt(), buf.readIdentifier(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public static class Builder {
        private final Text title;
        public int playerInventoryX = 8, playerInventoryY = 84;
        private final Identifier backgroundTexture;
        public final int backgroundSizeX = 176, backgroundSizeY = 166;
        public final boolean lockButton;

        public Builder(Text title, Identifier backgroundTexture, boolean lockButton) {
            this.title = title;
            this.backgroundTexture = backgroundTexture;
            this.lockButton = lockButton;
        }

        public MachineGuiParameters build() {
            return new MachineGuiParameters(title, playerInventoryX, playerInventoryY, backgroundTexture, backgroundSizeX, backgroundSizeY, lockButton);
        }
    }
}
