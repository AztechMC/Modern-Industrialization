package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;

public class MachinePackets {
    public static class S2C {
        public static final Identifier COMPONENT_SYNC = new MIIdentifier("machine_component_sync");
        @SuppressWarnings("unchecked")
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
}
