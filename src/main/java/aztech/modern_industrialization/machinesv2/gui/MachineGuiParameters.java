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
package aztech.modern_industrialization.machinesv2.gui;

import aztech.modern_industrialization.MIIdentifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MachineGuiParameters {
    public final Text title;
    public final int playerInventoryX, playerInventoryY;
    public final Identifier backgroundTexture;
    public final int backgroundWidth, backgroundHeight;
    public final boolean lockButton;

    private MachineGuiParameters(Text title, int playerInventoryX, int playerInventoryY, Identifier backgroundTexture, int backgroundWidth,
            int backgroundHeight, boolean lockButton) {
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
        return new MachineGuiParameters(buf.readText(), buf.readInt(), buf.readInt(), buf.readIdentifier(), buf.readInt(), buf.readInt(),
                buf.readBoolean());
    }

    public static class Builder {
        private final Text title;
        public int playerInventoryX = 8, playerInventoryY = 84;
        private final Identifier backgroundTexture;
        public final int backgroundSizeX = 176, backgroundSizeY = 166;
        public final boolean lockButton;

        public Builder(Text title, String name, boolean lockButton) {
            this.title = title;
            this.backgroundTexture = new MIIdentifier("textures/gui/container/" + name + ".png");
            this.lockButton = lockButton;
        }

        public MachineGuiParameters build() {
            return new MachineGuiParameters(title, playerInventoryX, playerInventoryY, backgroundTexture, backgroundSizeX, backgroundSizeY,
                    lockButton);
        }
    }
}
