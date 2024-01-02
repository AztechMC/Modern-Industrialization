package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public record AdjustSlotCapacityPacket(int syncId, int slotId, boolean isIncrease, boolean isShiftDown) implements BasePacket {
    public AdjustSlotCapacityPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(slotId);
        buf.writeBoolean(isIncrease);
        buf.writeBoolean(isShiftDown);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == syncId) {
            Slot slot = sh.getSlot(slotId);
            if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot confSlot) {
                confSlot.getConfStack().adjustCapacity(isIncrease, isShiftDown);
            }
        }
    }
}
