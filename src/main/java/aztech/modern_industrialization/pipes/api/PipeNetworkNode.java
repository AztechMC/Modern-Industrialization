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

import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import java.util.List;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class PipeNetworkNode {
    protected PipeNetwork network;

    public void updateConnections(World world, BlockPos pos) {
    }

    public void buildInitialConnections(World world, BlockPos pos) {
    }

    /**
     * Get connections. Must return a size 6 array containing the 6 connections in
     * the Direction order. Null can be used to render no connection.
     */
    public abstract PipeEndpointType[] getConnections(BlockPos pos);

    public abstract void removeConnection(World world, BlockPos pos, Direction direction);

    public abstract void addConnection(World world, BlockPos pos, Direction direction);

    /**
     * Get the connection screen handler factory, or null if there is not gui for
     * this connection.
     */
    public ExtendedScreenHandlerFactory getConnectionGui(Direction direction, IPipeScreenHandlerHelper helper) {
        return null;
    }

    public abstract NbtCompound toTag(NbtCompound tag);

    public abstract void fromTag(NbtCompound tag);

    public final PipeNetworkType getType() {
        return network.manager.getType();
    }

    public final PipeNetworkManager getManager() {
        return network.manager;
    }

    public NbtCompound writeCustomData() {
        return new NbtCompound();
    }

    public void appendDroppedStacks(List<ItemStack> droppedStacks) {
    }
}
