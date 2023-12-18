package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.ConfigurableScreenHandler;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

public record UpdateItemSlotPacket(int syncId, int stackId, ConfigurableItemStack newStack) implements BasePacket {
    public UpdateItemSlotPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readVarInt(), new ConfigurableItemStack(buf.readNbt()));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(syncId);
        buf.writeVarInt(stackId);
        buf.writeNbt(newStack.toNbt());
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnClient();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == syncId) {
            ConfigurableScreenHandler csh = (ConfigurableScreenHandler) sh;
            ConfigurableItemStack oldStack = csh.inventory.getItemStacks().get(stackId);
            // update stack
            csh.inventory.getItemStacks().set(stackId, newStack);
            // update slot
            for (int i = 0; i < csh.slots.size(); ++i) {
                Slot slot = csh.slots.get(i);
                if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot is) {
                    if (is.getConfStack() == oldStack) {
                        csh.slots.set(i, newStack.new ConfigurableItemSlot(is));
                        return;
                    }
                }
            }
            throw new RuntimeException("Could not find slot to replace!");
        }
    }
}
