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

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.core.AELog;
import appeng.me.GridConnection;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public class MENetwork extends PipeNetwork {

    public MENetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new MENetworkData() : data);
    }

    @Override
    public void setNode(BlockPos pos, @Nullable PipeNetworkNode node) {
        super.setNode(pos, node);
    }

    @Override
    public void removeNode(BlockPos pos) {
        if (getNode(pos) instanceof MENetworkNode me) {
            me.getMainNode().destroy();
        }

        super.removeNode(pos);
    }

    @Override
    public void tick(ServerLevel world) {
        var mainNode = ((MENetworkData) data).getMainNode();

        if (!mainNode.isReady()) {
            mainNode.create(world, null);
        }

        for (PosNode posNode : iterateTickingNodes()) {
            var me = (MENetworkNode) posNode.getNode();
            me.getMainNode().setExposedOnSides(me.getConnections());

            if (!me.getMainNode().isReady()) {
                me.getMainNode().setVisualRepresentation(MIPipes.INSTANCE.getPipeItem(manager.getType()));
                me.getMainNode().create(world, posNode.getPos());

                try {
                    GridConnection.create(me.getMainNode().getNode(), mainNode.getNode(), null);
                } catch (SecurityConnectionException e) {
                    AELog.debug(e);
                    // todo: turn connection back to BLOCK
                } catch (FailedConnectionException e) {
                    AELog.debug(e);
                }
            }
        }
    }

    @Override
    public PipeNetworkData merge(PipeNetwork other) {
        return new MENetworkData();
    }
}
