package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;

public record MachineComponentSyncPacket(int syncId, int componentIndex, FriendlyByteBuf buf) implements BasePacket {
    public MachineComponentSyncPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), new FriendlyByteBuf(buf.copy()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(componentIndex);
        buf.writeBytes(this.buf, 0, this.buf.readableBytes());
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();

        if (ctx.getPlayer().containerMenu.containerId == syncId) {
            var screenHandler = (MachineMenuCommon) ctx.getPlayer().containerMenu;
            screenHandler.readClientComponentSyncData(componentIndex, buf);
        }
    }
}
