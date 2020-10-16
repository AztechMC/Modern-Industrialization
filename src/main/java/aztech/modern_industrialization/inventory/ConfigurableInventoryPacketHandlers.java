package aztech.modern_industrialization.inventory;

import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.ScreenHandler;

public class ConfigurableInventoryPacketHandlers {
    // sync id, slot id, slot tag
    public static final PacketConsumer UPDATE_ITEM_SLOT = (context, data) -> {
        int syncId = data.readInt();
        int stackId = data.readInt();
        CompoundTag tag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if(handler.syncId == syncId) {
                MachineScreenHandler machineHandler = (MachineScreenHandler) handler;
                machineHandler.inventory.getItemStacks().get(stackId).readFromTag(tag);
            }
        });
    };
    // sync id, slot id, slot tag
    public static final PacketConsumer UPDATE_FLUID_SLOT = (context, data) -> {
        int syncId = data.readInt();
        int stackId = data.readInt();
        CompoundTag tag = data.readCompoundTag();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = MinecraftClient.getInstance().player.currentScreenHandler;
            if(handler.syncId == syncId) {
                MachineScreenHandler machineHandler = (MachineScreenHandler) handler;
                machineHandler.inventory.getFluidStacks().get(stackId).readFromTag(tag);
            }
        });
    };
    // sync id, new locking mode
    public static final PacketConsumer SET_LOCKING_MODE = (context, data) -> {
        int syncId = data.readInt();
        boolean lockingMode = data.readBoolean();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if(handler.syncId == syncId) {
                ConfigurableScreenHandler confHandler = (ConfigurableScreenHandler) handler;
                confHandler.lockingMode = lockingMode;
            }
        });
    };
}
