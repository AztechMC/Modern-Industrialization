package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record SetLockingModePacket(int syncId, boolean lockingMode) implements BasePacket {
    public SetLockingModePacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeBoolean(lockingMode);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == syncId) {
            ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
            csh.lockingMode = lockingMode;
        }
    }
}
