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

package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.gui.PipeScreenHandlerHelper;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public abstract class PipeNetworkNode {
    @Nullable
    protected PipeNetwork network;

    public void updateConnections(Level world, BlockPos pos) {}

    public void buildInitialConnections(Level world, BlockPos pos) {}

    /**
     * Get connections. Must return a size 6 array containing the 6 connections in
     * the Direction order. Null can be used to render no connection.
     */
    public abstract @Nullable PipeEndpointType[] getConnections(BlockPos pos);

    public abstract void removeConnection(Level world, BlockPos pos, Direction direction);

    public abstract void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction);

    /**
     * Get the connection screen handler factory, or null if there is not gui for
     * this connection.
     */
    public PipeMenuProvider getConnectionGui(Direction direction, PipeScreenHandlerHelper helper) {
        return null;
    }

    public abstract CompoundTag toTag(CompoundTag tag, HolderLookup.Provider registries);

    public abstract void fromTag(CompoundTag tag, HolderLookup.Provider registries);

    public final PipeNetworkType getType() {
        return network.manager.getType();
    }

    public final PipeNetworkManager getManager() {
        return network.manager;
    }

    public CompoundTag writeCustomData(HolderLookup.Provider registries) {
        return new CompoundTag();
    }

    public void appendDroppedStacks(List<ItemStack> droppedStacks) {}

    /**
     * Return true if something was done.
     */
    public boolean customUse(PipeBlockEntity pipe, Player player, InteractionHand hand, @Nullable Direction hitDirection) {
        return false;
    }

    public void onUnload() {}
}
