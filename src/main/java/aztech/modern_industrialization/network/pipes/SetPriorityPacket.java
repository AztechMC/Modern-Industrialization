package aztech.modern_industrialization.network.pipes;

import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetPriorityPacket(int syncId, int channel, int priority) implements BasePacket {
    public SetPriorityPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(channel);
        buf.writeVarInt(priority);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();

        AbstractContainerMenu handler = ctx.getPlayer().containerMenu;
        if (handler.containerId == syncId) {
            ((PipeScreenHandler) handler).getInterface(PriorityInterface.class).setPriority(channel, priority);
        }
    }
}
