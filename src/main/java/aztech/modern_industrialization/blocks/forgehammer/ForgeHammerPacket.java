package aztech.modern_industrialization.blocks.forgehammer;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

public class ForgeHammerPacket  {

    public static final Identifier SET_HAMMER = new MIIdentifier("set_hammer");
    public static final PacketConsumer ON_SET_HAMMER = (context, data) -> {
        int syncId = data.readInt();
        boolean hammer = data.readBoolean();
        context.getTaskQueue().execute(() -> {
            ScreenHandler handler = context.getPlayer().currentScreenHandler;
            if(handler.syncId == syncId) {
                ((ForgeHammerScreenHandler) handler).setHammer(hammer);
            }
        });
    };
}
