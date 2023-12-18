package aztech.modern_industrialization.network;

import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Objects;

public interface BasePacket {
    void write(FriendlyByteBuf buf);

    void handle(Context ctx);

    default void sendToServer() {
        MIPackets.CHANNEL.sendToServer(this);
    }

    default void sendToClient(ServerPlayer player) {
        MIPackets.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), this);
    }

    record Context(Class<? extends BasePacket> clazz, NetworkEvent.Context inner) {
        public boolean isOnClient() {
            return inner.getDirection().getReceptionSide().isClient();
        }
        public void assertOnServer() {
            if (isOnClient()) {
                throw new IllegalArgumentException("Cannot handle packet on client: " + clazz);
            }
        }
        public void assertOnClient() {
            if (!isOnClient()) {
                throw new IllegalArgumentException("Cannot handle packet on server: " + clazz);
            }
        }
        public Player getPlayer() {
            return isOnClient() ? CommonProxy.INSTANCE.getClientPlayer() : Objects.requireNonNull(inner.getSender());
        }
    }
}
