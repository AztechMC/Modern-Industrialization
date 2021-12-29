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
package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.gui.PipeScreenHandler;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FluidPipeScreenHandler extends PipeScreenHandler {
    public static final int HEIGHT = 153;

    public final FluidPipeInterface iface;
    private final Inventory playerInventory;
    private FluidVariant trackedNetworkFluid;
    private int trackedPriority;
    private int trackedType;

    public FluidPipeScreenHandler(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(syncId, playerInventory, FluidPipeInterface.ofBuf(buf));
    }

    public FluidPipeScreenHandler(int syncId, Inventory playerInventory, FluidPipeInterface iface) {
        super(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE, syncId);
        this.iface = iface;
        this.playerInventory = playerInventory;
        this.trackedNetworkFluid = iface.getNetworkFluid();
        this.trackedPriority = iface.getPriority(0);
        this.trackedType = iface.getConnectionType();

        addPlayerInventorySlots(playerInventory, HEIGHT);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        // TODO: Transfer between hotbar and main inventory?
        return ItemStack.EMPTY;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (playerInventory.player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) playerInventory.player;
            if (!trackedNetworkFluid.equals(iface.getNetworkFluid())) {
                trackedNetworkFluid = iface.getNetworkFluid();
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                trackedNetworkFluid.toPacket(buf);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_NETWORK_FLUID, buf);
            }
            if (trackedType != iface.getConnectionType()) {
                trackedType = iface.getConnectionType();
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeInt(trackedType);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_CONNECTION_TYPE, buf);
            }
            if (trackedPriority != iface.getPriority(0)) {
                trackedPriority = iface.getPriority(0);
                FriendlyByteBuf buf = PacketByteBufs.create();
                buf.writeInt(containerId);
                buf.writeInt(0);
                buf.writeInt(trackedPriority);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_PRIORITY, buf);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return iface.canUse(player);
    }

    @Override
    protected Object getInterface() {
        return iface;
    }
}
