package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record ChangeShapePacket(int syncId, int shapeLine, boolean clickedLeftButton) implements BasePacket {
    public ChangeShapePacket(FriendlyByteBuf buf) {
        this(buf.readUnsignedByte(), buf.readVarInt(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeByte(syncId);
        buf.writeVarInt(shapeLine);
        buf.writeBoolean(clickedLeftButton);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu menu = ctx.getPlayer().containerMenu;
        if (menu.containerId == syncId && menu instanceof MachineMenuServer machineMenu) {
            ShapeSelection.Server shapeSelection = machineMenu.blockEntity.getComponent(GuiComponents.SHAPE_SELECTION);
            shapeSelection.behavior.handleClick(shapeLine, clickedLeftButton ? -1 : +1);
        }
    }
}
