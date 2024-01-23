package aztech.modern_industrialization.network;

import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

public interface BasePacket extends CustomPacketPayload {
    void write(FriendlyByteBuf buf);

    void handle(Context ctx);

    default void sendToServer() {
        PacketDistributor.SERVER.noArg().send(this);
    }

    default void sendToClient(ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(this);
    }

    @Override
    default ResourceLocation id() {
        return MIPackets.packetLocations.get(getClass());
    }

    record Context(Class<? extends BasePacket> clazz, PlayPayloadContext inner) {
        public boolean isOnClient() {
            return inner.flow().isClientbound();
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
            return isOnClient() ? CommonProxy.INSTANCE.getClientPlayer() : Objects.requireNonNull(inner.player().get());
        }
    }
}
