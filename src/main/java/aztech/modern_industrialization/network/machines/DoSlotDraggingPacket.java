package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

public record DoSlotDraggingPacket(int containerId, int slotId, boolean isItem, @Nullable ItemVariant itemVariant, @Nullable FluidVariant fluidVariant) implements BasePacket {
    public DoSlotDraggingPacket(int containerId, int slotId, ItemVariant itemVariant) {
        this(containerId, slotId, true, itemVariant, null);
    }

    public DoSlotDraggingPacket(int containerId, int slotId, FluidVariant fluidVariant) {
        this(containerId, slotId, false, null, fluidVariant);
    }

    public DoSlotDraggingPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readVarInt(), buf.readBoolean(), buf);
    }

    private DoSlotDraggingPacket(int containerId, int slotId, boolean isItem, FriendlyByteBuf buf) {
        this(containerId, slotId, isItem, isItem ? ItemVariant.fromPacket(buf) : null, isItem ? null : FluidVariant.fromPacket(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeVarInt(slotId);
        buf.writeBoolean(isItem);
        if (isItem) {
            itemVariant.toPacket(buf);
        } else {
            fluidVariant.toPacket(buf);
        }
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == containerId) {
            Slot slot = sh.getSlot(slotId);
            ReiDraggable dw = (ReiDraggable) slot;
            if (isItem) {
                dw.dragItem(itemVariant, Simulation.ACT);
            } else {
                dw.dragFluid(fluidVariant, Simulation.ACT);
            }
        }
    }
}
