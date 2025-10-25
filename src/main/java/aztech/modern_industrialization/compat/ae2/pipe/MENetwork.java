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

import appeng.api.networking.GridHelper;
import appeng.api.util.AEColor;
import appeng.me.GridConnection;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import com.google.common.collect.Sets;
import java.util.HashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public class MENetwork extends PipeNetwork {
    final AEColor color;

    public MENetwork(int id, PipeNetworkData data, AEColor color) {
        super(id, data == null ? new MENetworkData() : data);

        this.color = color;
    }

    @Override
    public void setNode(BlockPos pos, @Nullable PipeNetworkNode maybeNode) {
        if (maybeNode instanceof MENetworkNode node) {
            var aeManagedNode = node.mainNode;
            if (aeManagedNode != null && aeManagedNode.isReady()) {
                // Disconnect node from previous network
                for (var connection : aeManagedNode.getNode().getConnections()) {
                    var otherSide = connection.getOtherSide(aeManagedNode.getNode());
                    if (otherSide.getService(INetworkInternalNode.class) != null) {
                        // Internal connection to old network, destroy it!
                        connection.destroy();
                        break; // max 1 connection to break
                    }
                }

                // Connect to this network
                if (data instanceof MENetworkData meData && meData.getMainNode().isReady()) {
                    GridHelper.createConnection(meData.getMainNode().getNode(), aeManagedNode.getNode());
                }
            }
        }

        super.setNode(pos, maybeNode);
    }

    @Override
    public void onRemove() {
        if (data instanceof MENetworkData meData) {
            meData.getMainNode().destroy();
        }

        for (var node : getRawNodeMap().values()) {
            if (node != null) {
                node.onUnload();
            }
        }
    }

    @Override
    public void tick(ServerLevel world) {
        var mainNode = ((MENetworkData) data).getMainNode();

        if (!mainNode.isReady()) {
            mainNode.create(world, null);
        }

        for (PosNode posNode : iterateTickingNodes()) {
            var node = (MENetworkNode) posNode.getNode();

            if (node.connectDelay-- > 0) {
                continue;
            }
            node.connectDelay = 100;

            node.updateNode(); // Recreate node if needed

            if (node.mainNode == null) {
                continue; // no connections for this node
            }

            boolean wasReady = node.mainNode.isReady();
            boolean hasInternalConnection = false;

            if (node.mainNode.isReady()) {
                for (var conn : node.mainNode.getNode().getConnections()) {
                    if (conn.getOtherSide(node.mainNode.getNode()).getService(INetworkInternalNode.class) != null) {
                        hasInternalConnection = true;
                        break;
                    }
                }
            } else {
                node.mainNode.setVisualRepresentation(MIPipes.INSTANCE.getPipeItem(manager.getType()));
                node.mainNode.create(world, posNode.getPos());
            }

            if (!wasReady || !hasInternalConnection) {
                // Connect to network's node
                GridHelper.createConnection(mainNode.getNode(), node.mainNode.getNode());
            }

            var failedConnections = new HashSet<Direction>();
            for (var missingConnection : Sets.difference(node.connections, node.mainNode.getNode().getConnectedSides())) {
                // Try to find node
                var otherNode = GridHelper.getExposedNode(world, posNode.getPos().relative(missingConnection), missingConnection.getOpposite());
                if (otherNode == null) {
                    continue;
                }

                if (!MENetworkNode.areColorsCompatible(color, otherNode.getGridColor())) {
                    failedConnections.add(missingConnection);
                    continue;
                }

                GridConnection.create(node.mainNode.getNode(), otherNode, missingConnection);
            }

            node.connections.removeAll(failedConnections);
            node.updateNode();
            world.blockEntityChanged(posNode.getPos()); // setChanged
            world.getChunkSource().blockChanged(posNode.getPos()); // mark for s2c update
        }
    }

    @Override
    public PipeNetworkData merge(PipeNetwork other) {
        throw new UnsupportedOperationException("Unreachable!");
    }
}
