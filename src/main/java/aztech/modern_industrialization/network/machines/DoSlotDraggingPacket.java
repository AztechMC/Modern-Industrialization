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

package aztech.modern_industrialization.network.machines;

import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.network.BasePacket;
import aztech.modern_industrialization.util.Simulation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

public record DoSlotDraggingPacket(int containerId, int slotId, boolean isItem, @Nullable ItemResource itemVariant,
        @Nullable FluidResource fluidVariant) implements BasePacket {
    public DoSlotDraggingPacket(int containerId, int slotId, ItemResource itemVariant) {
        this(containerId, slotId, true, itemVariant, null);
    }

    public DoSlotDraggingPacket(int containerId, int slotId, FluidResource fluidVariant) {
        this(containerId, slotId, false, null, fluidVariant);
    }

    private DoSlotDraggingPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readInt(), buf.readVarInt(), buf.readBoolean(), buf);
    }

    private DoSlotDraggingPacket(int containerId, int slotId, boolean isItem, RegistryFriendlyByteBuf buf) {
        this(containerId, slotId, isItem, isItem ? ItemResource.STREAM_CODEC.decode(buf) : null, isItem ? null : FluidResource.STREAM_CODEC.decode(buf));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, DoSlotDraggingPacket> STREAM_CODEC = StreamCodec.ofMember(
            DoSlotDraggingPacket::write, DoSlotDraggingPacket::new);

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeInt(containerId);
        buf.writeVarInt(slotId);
        buf.writeBoolean(isItem);
        if (isItem) {
            ItemResource.STREAM_CODEC.encode(buf, itemVariant);
        } else {
            FluidResource.STREAM_CODEC.encode(buf, fluidVariant);
        }
    }

    @Override
    public void handle(Context ctx) {
        ctx.assertOnServer();

        AbstractContainerMenu sh = ctx.getPlayer().containerMenu;
        if (sh.containerId == containerId) {
            Slot slot = sh.getSlot(slotId);
            ReiDraggable dw = (ReiDraggable) slot;
            if (isItem) {
                dw.dragItem(itemVariant, Simulation.ACT);
            } else {
                dw.dragFluid(fluidVariant, Simulation.ACT);
            }
        }
    }
}
