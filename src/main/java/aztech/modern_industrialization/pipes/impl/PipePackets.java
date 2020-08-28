package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class PipePackets {
    public static final Identifier SET_ITEM_WHITELIST = new MIIdentifier("set_item_whitelist");
    public static final PacketConsumer ON_SET_ITEM_WHITELIST = (context, data) -> {
        int syncId = data.readInt();
        boolean whitelist = data.readBoolean();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if(handler.syncId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.setWhitelist(whitelist);
            }
        });
    };
}
