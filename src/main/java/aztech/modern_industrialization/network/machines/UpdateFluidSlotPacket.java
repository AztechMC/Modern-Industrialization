package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public record UpdateFluidSlotPacket(int syncId, int stackId, ConfigurableFluidStack newStack) implements BasePacket {
    public UpdateFluidSlotPacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), new ConfigurableFluidStack(buf.readNbt()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(stackId);
        buf.writeNbt(newStack.toNbt());
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == syncId) {
            ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
            ConfigurableFluidStack oldStack = csh.inventory.getFluidStacks().get(stackId);
            // update stack
            csh.inventory.getFluidStacks().set(stackId, newStack);
            // update slot
            for (int i = 0; i < csh.slots.size(); ++i) {
                Slot slot = csh.slots.get(i);
                if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot fs) {
                    if (fs.getConfStack() == oldStack) {
                        csh.slots.set(i, newStack.new ConfigurableFluidSlot(fs));
                        return;
                    }
                }
            }
            throw new RuntimeException("Could not find slot to replace!");
        }
    }
}
