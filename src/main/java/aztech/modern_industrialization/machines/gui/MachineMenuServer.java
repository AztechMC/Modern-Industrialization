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

package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.network.machines.MachineComponentSyncPacket;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class MachineMenuServer extends MachineMenuCommon {
    public final MachineBlockEntity blockEntity;
    protected final List<Object> trackedData;

    public MachineMenuServer(int syncId, Inventory playerInventory, MachineBlockEntity blockEntity, MachineGuiParameters guiParams) {
        super(syncId, playerInventory, blockEntity.getInventory(), guiParams, blockEntity.guiComponents);
        this.blockEntity = blockEntity;
        trackedData = new ArrayList<>();
        for (GuiComponentServer component : blockEntity.guiComponents) {
            trackedData.add(component.extractData());
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        blockEntity.guiComponents.forEachIndexed((i, component) -> {
            var newData = component.extractData();
            if (!Objects.equals(trackedData.get(i), newData)) {
                var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), blockEntity.getLevel().registryAccess());
                ((GuiComponentServer.Type<?, Object>) component.getType()).dataCodec().encode(buf, newData);
                byte[] bytes = new byte[buf.writerIndex()];
                buf.readBytes(bytes);
                new MachineComponentSyncPacket(containerId, i, bytes).sendToClient((ServerPlayer) playerInventory.player);
                trackedData.set(i, newData);
                buf.release();
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(blockEntity, player);
    }

    @Override
    public void readClientComponentSyncData(int componentIndex, RegistryFriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Data can only be read on the client side!");
    }
}
