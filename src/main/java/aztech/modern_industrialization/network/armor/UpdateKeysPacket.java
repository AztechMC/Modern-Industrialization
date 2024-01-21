package aztech.modern_industrialization.network.armor;

import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.network.BasePacket;
import net.minecraft.network.FriendlyByteBuf;

public record UpdateKeysPacket(boolean up) implements BasePacket {
    public UpdateKeysPacket(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(up);
    }

    @Override
    public void handle(Context ctx) {
        MIKeyMap.update(ctx.getPlayer(), up);
    }
}
