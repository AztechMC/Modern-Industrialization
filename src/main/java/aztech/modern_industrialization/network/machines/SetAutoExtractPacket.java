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
package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;

public record SetAutoExtractPacket(int syncId, boolean isItem, boolean isExtract) implements BasePacket {
    public SetAutoExtractPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeBoolean(isItem);
        buf.writeBoolean(isExtract);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        if (ctx.getPlayer().containerMenu.containerId == syncId) {
            var screenHandler = (MachineMenuServer) ctx.getPlayer().containerMenu;
            AutoExtract.Server autoExtract = screenHandler.blockEntity.getComponent(GuiComponents.AUTO_EXTRACT);
            OrientationComponent orientation = autoExtract.getOrientation();
            if (isItem) {
                orientation.extractItems = isExtract;
            } else {
                orientation.extractFluids = isExtract;
            }
            screenHandler.blockEntity.setChanged();
            screenHandler.blockEntity.sync();
        }
    }
}
