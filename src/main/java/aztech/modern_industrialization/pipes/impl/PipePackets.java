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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreenHandler;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.gui.iface.ConnectionTypeInterface;
import aztech.modern_industrialization.pipes.gui.iface.PriorityInterface;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import aztech.modern_industrialization.util.UnsidedPacketHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PipePackets {
    public static final ResourceLocation SET_ITEM_WHITELIST = new MIIdentifier("set_item_whitelist");
    public static final UnsidedPacketHandler ON_SET_ITEM_WHITELIST = (player, buf) -> {
        int syncId = buf.readInt();
        boolean whitelist = buf.readBoolean();
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                ((ItemPipeScreenHandler) handler).pipeInterface.setWhitelist(whitelist);
            }
        };
    };
    public static final ResourceLocation SET_CONNECTION_TYPE = new MIIdentifier("set_connection_type");
    public static final UnsidedPacketHandler ON_SET_CONNECTION_TYPE = (player, buf) -> {
        int syncId = buf.readInt();
        int type = buf.readInt();
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                ((PipeScreenHandler) handler).getInterface(ConnectionTypeInterface.class).setConnectionType(type);
            }
        };
    };
    public static final ResourceLocation INCREMENT_PRIORITY = new MIIdentifier("increment_priority");
    public static final ServerPlayNetworking.PlayChannelHandler ON_INCREMENT_PRIORITY = (ms, player, h, buf, sender) -> {
        int syncId = buf.readInt();
        int channel = buf.readInt();
        int priority = buf.readInt();
        ms.execute(() -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                ((PipeScreenHandler) handler).getInterface(PriorityInterface.class).incrementPriority(channel, priority);
            }
        });
    };
    public static final ResourceLocation SET_PRIORITY = new MIIdentifier("set_priority");
    public static final ResourceLocation SET_NETWORK_FLUID = new MIIdentifier("set_network_fluid");
    public static final UnsidedPacketHandler ON_SET_NETWORK_FLUID = (player, buf) -> {
        int syncId = buf.readInt();
        FluidVariant fluid = FluidVariant.fromPacket(buf);
        return () -> {
            AbstractContainerMenu handler = player.containerMenu;
            if (handler.containerId == syncId) {
                ((FluidPipeScreenHandler) handler).iface.setNetworkFluid(fluid);
            }
        };
    };
}
