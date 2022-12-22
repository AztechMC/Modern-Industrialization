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
package aztech.modern_industrialization.compat.ae2.pipe;

import appeng.api.networking.*;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MENetworkNode extends PipeNetworkNode {

    private final IManagedGridNode mainNode;
    private final Set<Direction> connections = EnumSet.noneOf(Direction.class);

    public MENetworkNode() {
        this.mainNode = GridHelper.createManagedNode(this, new IGridNodeListener<>() {
            @Override
            public void onSecurityBreak(MENetworkNode nodeOwner, IGridNode node) {
                throw new UnsupportedOperationException("How did we get here?");
            }

            @Override
            public void onSaveChanges(MENetworkNode nodeOwner, IGridNode node) {
            }
        })
                .setFlags(GridFlags.PREFERRED)
                .setInWorldNode(true)
                .setExposedOnSides(EnumSet.allOf(Direction.class))
                .setIdlePowerUsage(0.0);
    }

    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    public Set<Direction> getConnections() {
        return connections;
    }

    public IGridNode getGridNode(Direction dir) {
        var node = getMainNode().getNode();

        // Check if the proxy exposes the node on this side
        if (node != null && connections.contains(dir)) {
            return node;
        }

        return null;
    }

    @Override
    public void buildInitialConnections(Level world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (canConnect(world, pos, direction)) {
                connections.add(direction);
            }
        }
    }

    @Override
    public void updateConnections(Level world, BlockPos pos) {
        connections.removeIf(side -> !canConnect(world, pos, side));
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.get3DDataValue()] = PipeEndpointType.PIPE;
        }
        for (Direction connection : this.connections) {
            connections[connection.get3DDataValue()] = PipeEndpointType.BLOCK;
        }
        return connections;
    }

    @Override
    public void removeConnection(Level world, BlockPos pos, Direction direction) {
        // Remove if it exists
        connections.remove(direction);
    }

    @Override
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
        if (canConnect(world, pos, direction)) {
            connections.add(direction);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putByte("connections", NbtHelper.encodeDirections(connections));
        this.getMainNode().saveToNBT(tag);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        connections.clear();
        connections.addAll(Arrays.asList(NbtHelper.decodeDirections(tag.getByte("connections"))));
        this.getMainNode().loadFromNBT(tag);
    }

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        return GridHelper.getExposedNode(world, pos.relative(direction), direction.getOpposite()) != null;
    }
}
