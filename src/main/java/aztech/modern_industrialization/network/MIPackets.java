package aztech.modern_industrialization.network;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.network.armor.ActivateChestPacket;
import aztech.modern_industrialization.network.armor.UpdateKeysPacket;
import aztech.modern_industrialization.network.machines.AdjustSlotCapacityPacket;
import aztech.modern_industrialization.network.machines.ChangeShapePacket;
import aztech.modern_industrialization.network.machines.DoSlotDraggingPacket;
import aztech.modern_industrialization.network.machines.ForgeHammerMoveRecipePacket;
import aztech.modern_industrialization.network.machines.LockAllPacket;
import aztech.modern_industrialization.network.machines.MachineComponentSyncPacket;
import aztech.modern_industrialization.network.machines.ReiLockSlotsPacket;
import aztech.modern_industrialization.network.machines.SetAutoExtractPacket;
import aztech.modern_industrialization.network.machines.SetLockingModePacket;
import aztech.modern_industrialization.network.machines.UpdateFluidSlotPacket;
import aztech.modern_industrialization.network.machines.UpdateItemSlotPacket;
import aztech.modern_industrialization.network.pipes.IncrementPriorityPacket;
import aztech.modern_industrialization.network.pipes.SetConnectionTypePacket;
import aztech.modern_industrialization.network.pipes.SetItemWhitelistPacket;
import aztech.modern_industrialization.network.pipes.SetNetworkFluidPacket;
import aztech.modern_industrialization.network.pipes.SetPriorityPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class MIPackets {
    static final Map<Class<? extends BasePacket>, ResourceLocation> packetLocations = new HashMap<>();
    private static final List<Registration<?>> registrations = new ArrayList<>();

    private record Registration<P extends BasePacket>(ResourceLocation resourceLocation, Class<P> clazz, FriendlyByteBuf.Reader<P> packetConstructor) {
    }

    private static <P extends BasePacket> void register(String path, Class<P> clazz, FriendlyByteBuf.Reader<P> packetConstructor) {
        packetLocations.put(clazz, MI.id(path));
        registrations.add(new Registration<>(MI.id(path), clazz, packetConstructor));
    }

    static {
        // Armor
        register("activate_chest", ActivateChestPacket.class, ActivateChestPacket::new);
        register("update_keys", UpdateKeysPacket.class, UpdateKeysPacket::new);
        // Configurable inventory
        register("adjust_slot_capacity", AdjustSlotCapacityPacket.class, AdjustSlotCapacityPacket::new);
        register("do_slot_dragging", DoSlotDraggingPacket.class, DoSlotDraggingPacket::new);
        register("lock_all", LockAllPacket.class, LockAllPacket::new);
        register("machine_component_sync", MachineComponentSyncPacket.class, MachineComponentSyncPacket::new);
        register("set_locking_mode", SetLockingModePacket.class, SetLockingModePacket::new);
        register("update_fluid_slot", UpdateFluidSlotPacket.class, UpdateFluidSlotPacket::new);
        register("update_item_slot", UpdateItemSlotPacket.class, UpdateItemSlotPacket::new);
        // Machine
        register("change_shape", ChangeShapePacket.class, ChangeShapePacket::new);
        register("forge_hammer_move_recipe", ForgeHammerMoveRecipePacket.class, ForgeHammerMoveRecipePacket::new);
        register("rei_lock_slots", ReiLockSlotsPacket.class, ReiLockSlotsPacket::new);
        register("set_auto_extract", SetAutoExtractPacket.class, SetAutoExtractPacket::new);
        // Pipes
        register("increment_priority", IncrementPriorityPacket.class, IncrementPriorityPacket::new);
        register("set_connection_type", SetConnectionTypePacket.class, SetConnectionTypePacket::new);
        register("set_item_whitelist", SetItemWhitelistPacket.class, SetItemWhitelistPacket::new);
        register("set_network_fluid", SetNetworkFluidPacket.class, SetNetworkFluidPacket::new);
        register("set_priority", SetPriorityPacket.class, SetPriorityPacket::new);
    }

    public static void init(RegisterPayloadHandlerEvent event) {
        var registrar = event.registrar(MI.ID);

        for (var reg : registrations) {
            register(registrar, reg);
        }
    }

    private static <P extends BasePacket> void register(IPayloadRegistrar registrar, Registration<P> reg) {
        registrar.play(reg.resourceLocation, reg.packetConstructor, (packet, context) -> {
            context.workHandler().execute(() -> {
                packet.handle(new BasePacket.Context(reg.clazz, context));
            });
        });
    }
}
