package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record LockAllPacket(int containerId, boolean lock) implements BasePacket {
    public LockAllPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeByte(containerId);
        buf.writeBoolean(lock);
    }

    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu.containerId == containerId) {
            ((ConfigurableScreenHandler) menu).lockAll(lock);
        }
    }
}
