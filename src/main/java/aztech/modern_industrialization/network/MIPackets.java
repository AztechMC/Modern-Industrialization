package aztech.modern_industrialization.network;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.network.machines.AdjustSlotCapacityPacket;
import aztech.modern_industrialization.network.machines.LockAllPacket;
import aztech.modern_industrialization.network.machines.MachineComponentSyncPacket;
import aztech.modern_industrialization.network.machines.SetAutoExtractPacket;
import aztech.modern_industrialization.network.machines.SetLockingModePacket;
import aztech.modern_industrialization.network.machines.UpdateFluidSlotPacket;
import aztech.modern_industrialization.network.machines.UpdateItemSlotPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;

import java.util.function.Function;

public class MIPackets {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MI.id("general"),
            () -> "1.0",
            v -> true,
            v -> true);
    private static int nextIndex = 0;

    private static <P extends BasePacket> void register(Class<P> clazz, Function<FriendlyByteBuf, P> packetConstructor) {
        CHANNEL.messageBuilder(clazz, nextIndex++)
                .encoder(BasePacket::write)
                .decoder(packetConstructor::apply)
                .consumerMainThread((packet, ctx) -> {
                    packet.handle(new BasePacket.Context(clazz, ctx));
                })
                .add();
    }

    static {
        // Configurable inventory
        register(AdjustSlotCapacityPacket.class, AdjustSlotCapacityPacket::new);
        register(LockAllPacket.class, LockAllPacket::new);
        register(MachineComponentSyncPacket.class, MachineComponentSyncPacket::new);
        register(SetLockingModePacket.class, SetLockingModePacket::new);
        register(UpdateFluidSlotPacket.class, UpdateFluidSlotPacket::new);
        register(UpdateItemSlotPacket.class, UpdateItemSlotPacket::new);
        // Machine
        register(SetAutoExtractPacket.class, SetAutoExtractPacket::new);
    }

    public static void init() {
        // init static
    }
}