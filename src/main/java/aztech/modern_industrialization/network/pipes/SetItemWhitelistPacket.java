package aztech.modern_industrialization.network.pipes;

import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetItemWhitelistPacket(int syncId, boolean whitelist) implements BasePacket {
    public SetItemWhitelistPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeBoolean(whitelist);
    }

    @Override
    public void handle(Context ctx) {
        AbstractContainerMenu handler = ctx.getPlayer().containerMenu;
        if (handler.containerId == syncId) {
            ((ItemPipeScreenHandler) handler).pipeInterface.setWhitelist(whitelist);
        }
    }
}
