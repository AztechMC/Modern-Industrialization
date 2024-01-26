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

import aztech.modern_industrialization.proxy.CommonProxy;
import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

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
