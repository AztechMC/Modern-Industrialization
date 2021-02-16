package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.components.sync.AutoExtract;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class MachinePackets {
    public static class S2C {
        public static final Identifier COMPONENT_SYNC = new MIIdentifier("machine_component_sync");
        @Environment(EnvType.CLIENT)
        public static final ClientPlayNetworking.PlayChannelHandler ON_COMPONENT_SYNC = (mc, handler, buf, sender) -> {
            int syncId = buf.readInt();
            int componentIndex = buf.readInt();
            buf.retain();
            mc.execute(() -> {
                try {
                    if (mc.player.currentScreenHandler.syncId == syncId) {
                        MachineScreenHandlers.Client screenHandler = (MachineScreenHandlers.Client) mc.player.currentScreenHandler;
                        screenHandler.components.get(componentIndex).read(buf);
                    }
                } finally {
                    buf.release();
                }
            });
        };
    }
    public static class C2S {
        public static final Identifier SET_AUTO_EXTRACT = new MIIdentifier("set_auto_extract");
        public static final ServerPlayNetworking.PlayChannelHandler ON_SET_AUTO_EXTRACT = (ms, player, handler, buf, sender) -> {
            int syncId = buf.readInt();
            boolean isItem = buf.readBoolean();
            boolean isExtract = buf.readBoolean();
            ms.execute(() -> {
                if (player.currentScreenHandler.syncId == syncId) {
                    MachineScreenHandlers.Server screenHandler = (MachineScreenHandlers.Server) player.currentScreenHandler;
                    AutoExtract.Server autoExtract = screenHandler.blockEntity.getComponent(SyncedComponents.AUTO_EXTRACT);
                    OrientationComponent orientation = autoExtract.getOrientation();
                    if (isItem) {
                        orientation.extractItems = isExtract;
                    } else {
                        orientation.extractFluids = isExtract;
                    }
                    screenHandler.blockEntity.markDirty();
                    screenHandler.blockEntity.sync();
                }
            });
        };
    }
}
