package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ReiLockSlotsPacket(int containedId, ResourceLocation recipeId) implements BasePacket {
    public ReiLockSlotsPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containedId);
        buf.writeResourceLocation(recipeId);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == containedId && sh instanceof MachineMenuServer screenHandler) {
            // Check that locking the slots is allowed in the first place
            ReiSlotLocking.Server slotLocking = screenHandler.blockEntity.getComponent(GuiComponents.REI_SLOT_LOCKING);
            if (!slotLocking.allowLocking.get())
                return;

            // Lock
            slotLocking.slotLockable.lockSlots(recipeId, ctx.getPlayer().getInventory());
        }
    }
}
