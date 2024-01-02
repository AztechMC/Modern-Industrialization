package aztech.modern_industrialization.network.pipes;

import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record IncrementPriorityPacket(int syncId, int channel, int delta) implements BasePacket {
    public IncrementPriorityPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(channel);
        buf.writeVarInt(delta);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu handler = ctx.getPlayer().containerMenu;
        if (handler.containerId == syncId) {
            ((PipeScreenHandler) handler).getInterface(PriorityInterface.class).incrementPriority(channel, delta);
        }
    }
}
