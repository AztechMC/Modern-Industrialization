package aztech.modern_industrialization.network.pipes;

import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetConnectionTypePacket(int syncId, int type) implements BasePacket {
    public SetConnectionTypePacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(type);
    }

    @Override
    public void handle(Context ctx) {
        AbstractContainerMenu handler = ctx.getPlayer().containerMenu;
        if (handler.containerId == syncId) {
            ((PipeScreenHandler) handler).getInterface(ConnectionTypeInterface.class).setConnectionType(type);
        }
    }
}
