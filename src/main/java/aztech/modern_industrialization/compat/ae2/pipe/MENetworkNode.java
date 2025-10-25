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
import appeng.api.util.AEColor;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeNetworks;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class MENetworkNode extends PipeNetworkNode {
    @Nullable
    IManagedGridNode mainNode;
    int connectDelay = 0;

    final Set<Direction> connections = EnumSet.noneOf(Direction.class);

    void updateNode() {
        if (this.mainNode == null && this.connections.size() > 0) {
            this.mainNode = GridHelper.createManagedNode(this, (nodeOwner, node) -> {}).setFlags(GridFlags.PREFERRED).setIdlePowerUsage(0.0);
        }
        if (this.mainNode != null && this.connections.size() == 0) {
            // Destroying the node might cause a block update,
            // which might trigger updateConnections,
            // which might call this code again.
            // So, we first clear the reference before clearing the node
            // to ensure that if this code is called again nothing will happen.
            var toDestroy = this.mainNode;
            this.mainNode = null;
            toDestroy.destroy();
        }
    }

    @Override
    public void buildInitialConnections(Level world, BlockPos pos) {}

    @Override
    public void updateConnections(Level world, BlockPos pos) {
        // Remove the connection to the outside world if a connection to another pipe is made.
        var levelNetworks = PipeNetworks.get((ServerLevel) world);
        connections.removeIf(connection -> {
            for (var type : PipeNetworkType.getTypes().values()) {
                var manager = levelNetworks.getOptionalManager(type);
                if (manager != null && manager.hasLink(pos, connection)) {
                    return true;
                }
            }
            return false;
        });
        updateNode();

        // Request immediate connection update in case a new pipe was placed.
        connectDelay = 0;
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
        // Also remove the actual connection
        if (mainNode != null && mainNode.isReady()) {
            for (var conn : mainNode.getNode().getConnections()) {
                if (conn.getDirection(mainNode.getNode()) == direction) {
                    conn.destroy();
                    break; // only 1 to destroy
                }
            }
        }
        updateNode();
    }

    @Override
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
        if (canConnect(world, pos, direction)) {
            connections.add(direction);
            updateNode();
            connectDelay = 0;
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByte("connections", NbtHelper.encodeDirections(connections));
        if (mainNode != null) {
            mainNode.saveToNBT(tag);
        }
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag, HolderLookup.Provider registries) {
        connections.clear();
        connections.addAll(Arrays.asList(NbtHelper.decodeDirections(tag.getByte("connections"))));
        updateNode();
        if (mainNode != null) {
            mainNode.loadFromNBT(tag);
        }
    }

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        var node = GridHelper.getExposedNode(world, pos.relative(direction), direction.getOpposite());

        return node != null && areColorsCompatible(((MENetwork) network).color, node.getGridColor());
    }

    static boolean areColorsCompatible(AEColor color1, AEColor color2) {
        return color1 == AEColor.TRANSPARENT || color2 == AEColor.TRANSPARENT || color1 == color2;
    }

    @Override
    public void onUnload() {
        if (mainNode != null) {
            mainNode.destroy();
            mainNode = null; // in case it's used again later
        }
    }
}
