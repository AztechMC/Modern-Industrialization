package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.MachineMenuServer;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;

public record SetAutoExtractPacket(int syncId, boolean isItem, boolean isExtract) implements BasePacket {
    public SetAutoExtractPacket(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean(), buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(syncId);
        buf.writeBoolean(isItem);
        buf.writeBoolean(isExtract);
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        if (ctx.getPlayer().containerMenu.containerId == syncId) {
            var screenHandler = (MachineMenuServer) ctx.getPlayer().containerMenu;
            AutoExtract.Server autoExtract = screenHandler.blockEntity.getComponent(GuiComponents.AUTO_EXTRACT);
            OrientationComponent orientation = autoExtract.getOrientation();
            if (isItem) {
                orientation.extractItems = isExtract;
            } else {
                orientation.extractFluids = isExtract;
            }
            screenHandler.blockEntity.setChanged();
            screenHandler.blockEntity.sync();
        }
    }
}
