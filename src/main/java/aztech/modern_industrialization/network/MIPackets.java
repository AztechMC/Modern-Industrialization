/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package aztech.modern_industrialization.network;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.network.armor.ActivateItemPacket;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class MIPackets {
    static final Map<Class<? extends BasePacket>, CustomPacketPayload.Type<?>> packetTypes = new HashMap<>();
    private static final List<Registration<?>> registrations = new ArrayList<>();

    private record Registration<P extends BasePacket>(CustomPacketPayload.Type<P> packetType, Class<P> clazz,
            StreamCodec<? super RegistryFriendlyByteBuf, P> packetCodec) {
    }

    private static <P extends BasePacket> void register(String path, Class<P> clazz,
            StreamCodec<? super RegistryFriendlyByteBuf, P> packetConstructor) {
        var type = new CustomPacketPayload.Type<P>(MI.id(path));
        packetTypes.put(clazz, type);
        registrations.add(new Registration<>(type, clazz, packetConstructor));
    }

    static {
        // Armor
        register("activate_chest", ActivateItemPacket.class, ActivateItemPacket.STREAM_CODEC);
        register("update_keys", UpdateKeysPacket.class, UpdateKeysPacket.STREAM_CODEC);
        // Configurable inventory
        register("adjust_slot_capacity", AdjustSlotCapacityPacket.class, AdjustSlotCapacityPacket.STREAM_CODEC);
        register("do_slot_dragging", DoSlotDraggingPacket.class, DoSlotDraggingPacket.STREAM_CODEC);
        register("lock_all", LockAllPacket.class, LockAllPacket.STREAM_CODEC);
        register("machine_component_sync", MachineComponentSyncPacket.class, MachineComponentSyncPacket.STREAM_CODEC);
        register("set_locking_mode", SetLockingModePacket.class, SetLockingModePacket.STREAM_CODEC);
        register("update_fluid_slot", UpdateFluidSlotPacket.class, UpdateFluidSlotPacket.STREAM_CODEC);
        register("update_item_slot", UpdateItemSlotPacket.class, UpdateItemSlotPacket.STREAM_CODEC);
        // Machine
        register("change_shape", ChangeShapePacket.class, ChangeShapePacket.STREAM_CODEC);
        register("forge_hammer_move_recipe", ForgeHammerMoveRecipePacket.class, ForgeHammerMoveRecipePacket.STREAM_CODEC);
        register("rei_lock_slots", ReiLockSlotsPacket.class, ReiLockSlotsPacket.STREAM_CODEC);
        register("set_auto_extract", SetAutoExtractPacket.class, SetAutoExtractPacket.STREAM_CODEC);
        // Pipes
        register("increment_priority", IncrementPriorityPacket.class, IncrementPriorityPacket.STREAM_CODEC);
        register("set_connection_type", SetConnectionTypePacket.class, SetConnectionTypePacket.STREAM_CODEC);
        register("set_item_whitelist", SetItemWhitelistPacket.class, SetItemWhitelistPacket.STREAM_CODEC);
        register("set_network_fluid", SetNetworkFluidPacket.class, SetNetworkFluidPacket.STREAM_CODEC);
        register("set_priority", SetPriorityPacket.class, SetPriorityPacket.STREAM_CODEC);
    }

    public static void init(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");

        for (var reg : registrations) {
            register(registrar, reg);
        }
    }

    private static <P extends BasePacket> void register(PayloadRegistrar registrar, Registration<P> reg) {
        registrar.playBidirectional(reg.packetType, reg.packetCodec, (packet, context) -> {
            packet.handle(new BasePacket.Context(reg.clazz, context));
        });
    }
}
