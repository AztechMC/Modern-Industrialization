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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class FluidPipeScreenHandler extends PipeScreenHandler {
    public static final int HEIGHT = 153;

    public final FluidPipeInterface iface;
    private final PlayerInventory playerInventory;
    private FluidVariant trackedNetworkFluid;
    private int trackedPriority;
    private int trackedType;

    public FluidPipeScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, FluidPipeInterface.ofBuf(buf));
    }

    public FluidPipeScreenHandler(int syncId, PlayerInventory playerInventory, FluidPipeInterface iface) {
        super(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE, syncId);
        this.iface = iface;
        this.playerInventory = playerInventory;
        this.trackedNetworkFluid = iface.getNetworkFluid();
        this.trackedPriority = iface.getPriority();
        this.trackedType = iface.getConnectionType();

        addPlayerInventorySlots(playerInventory, HEIGHT);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int index) {
        // TODO: Transfer between hotbar and main inventory?
        return ItemStack.EMPTY;
    }

    @Override
    public void sendContentUpdates() {
        super.sendContentUpdates();
        if (playerInventory.player instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) playerInventory.player;
            if (!trackedNetworkFluid.equals(iface.getNetworkFluid())) {
                trackedNetworkFluid = iface.getNetworkFluid();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                trackedNetworkFluid.toPacket(buf);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_NETWORK_FLUID, buf);
            }
            if (trackedType != iface.getConnectionType()) {
                trackedType = iface.getConnectionType();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeInt(trackedType);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_CONNECTION_TYPE, buf);
            }
            if (trackedPriority != iface.getPriority()) {
                trackedPriority = iface.getPriority();
                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeInt(syncId);
                buf.writeInt(trackedPriority);
                ServerPlayNetworking.send(serverPlayer, PipePackets.SET_PRIORITY, buf);
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return iface.canUse(player);
    }

    @Override
    protected Object getInterface() {
        return iface;
    }
}
