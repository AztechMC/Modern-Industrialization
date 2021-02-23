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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class MachineGuiParameters {
    public final Text title;
    public final int playerInventoryX, playerInventoryY;
    public final int backgroundWidth, backgroundHeight;
    public final boolean lockButton;

    private MachineGuiParameters(Text title, int playerInventoryX, int playerInventoryY, int backgroundWidth, int backgroundHeight,
            boolean lockButton) {
        this.title = title;
        this.playerInventoryX = playerInventoryX;
        this.playerInventoryY = playerInventoryY;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
        this.lockButton = lockButton;
    }

    public void write(PacketByteBuf buf) {
        buf.writeText(title);
        buf.writeInt(playerInventoryX);
        buf.writeInt(playerInventoryY);
        buf.writeInt(backgroundWidth);
        buf.writeInt(backgroundHeight);
        buf.writeBoolean(lockButton);
    }

    public static MachineGuiParameters read(PacketByteBuf buf) {
        return new MachineGuiParameters(buf.readText(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public static class Builder {
        private final Text title;
        public int playerInventoryX = 8, playerInventoryY = 84;
        private int backgroundSizeX = 176, backgroundSizeY = 166;
        public final boolean lockButton;

        public Builder(String blockId, boolean lockButton) {
            this(new TranslatableText("block.modern_industrialization." + blockId), lockButton);
        }

        public Builder(Text title, boolean lockButton) {
            this.title = title;
            this.lockButton = lockButton;
        }

        public Builder backgroundHeight(int height) {
            this.backgroundSizeY = height;
            this.playerInventoryY = height - 82;
            return this;
        }

        public MachineGuiParameters build() {
            return new MachineGuiParameters(title, playerInventoryX, playerInventoryY, backgroundSizeX, backgroundSizeY, lockButton);
        }
    }
}
