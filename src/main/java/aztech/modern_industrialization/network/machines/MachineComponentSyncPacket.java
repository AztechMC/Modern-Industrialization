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

import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.network.BasePacket;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public record MachineComponentSyncPacket(int syncId, int componentIndex, byte[] data) implements BasePacket {
    public MachineComponentSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), buf.readByteArray());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(componentIndex);
        buf.writeByteArray(data);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();

        if (ctx.getPlayer().containerMenu.containerId == syncId) {
            var screenHandler = (MachineMenuCommon) ctx.getPlayer().containerMenu;
            var buf = new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
            try {
                screenHandler.readClientComponentSyncData(componentIndex, buf);
            } finally {
                buf.release();
            }
        }
    }
}
