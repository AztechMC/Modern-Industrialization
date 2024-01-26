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
package aztech.modern_industrialization.pipes.electricity;

import static aztech.modern_industrialization.pipes.api.PipeEndpointType.*;

import aztech.modern_industrialization.api.energy.*;
import aztech.modern_industrialization.pipes.api.PipeEndpointType;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import org.jetbrains.annotations.NotNull;

public class ElectricityNetworkNode extends PipeNetworkNode {
    private List<Direction> connections = new ArrayList<>();
    private final List<BlockCapabilityCache<MIEnergyStorage, @NotNull Direction>> caches = new ArrayList<>();
    long eu = 0;

    public void appendAttributes(ServerLevel world, BlockPos pos, CableTier cableTier, List<MIEnergyStorage> storages) {
        if (caches.size() != connections.size()) {
            caches.clear();
            for (Direction direction : connections) {
                caches.add(BlockCapabilityCache.create(EnergyApi.SIDED, world, pos.relative(direction), direction.getOpposite()));
            }
        }
        for (int i = 0; i < connections.size(); ++i) {
            MIEnergyStorage storage = caches.get(i).getCapability();
            if (storage == null || !storage.canConnect(cableTier)) {
                continue;
            }
            storages.add(storage);
        }
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
        // We don't connect by default, so we just have to remove connections that have
        // become unavailable
        for (int i = 0; i < connections.size();) {
            if (canConnect(world, pos, connections.get(i))) {
                i++;
            } else {
                connections.remove(i);
                caches.clear();
            }
        }
    }

    @Override
    public PipeEndpointType[] getConnections(BlockPos pos) {
        PipeEndpointType[] connections = new PipeEndpointType[6];
        for (Direction direction : network.manager.getNodeLinks(pos)) {
            connections[direction.get3DDataValue()] = PIPE;
        }
        for (Direction connection : this.connections) {
            connections[connection.get3DDataValue()] = BLOCK;
        }
        return connections;
    }

    @Override
    public void removeConnection(Level world, BlockPos pos, Direction direction) {
        // Remove if it exists
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i) == direction) {
                connections.remove(i);
                caches.clear();
                return;
            }
        }
    }

    @Override
    public void addConnection(PipeBlockEntity pipe, Player player, Level world, BlockPos pos, Direction direction) {
        // Refuse if it already exists
        for (Direction connection : connections) {
            if (connection == direction) {
                return;
            }
        }
        // Otherwise try to connect
        if (canConnect(world, pos, direction)) {
            connections.add(direction);
            caches.clear();
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putByte("connections", NbtHelper.encodeDirections(connections));
        tag.putLong("eu", eu);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        connections = new ArrayList<>(Arrays.asList(NbtHelper.decodeDirections(tag.getByte("connections"))));
        caches.clear();
        eu = tag.getLong("eu");
    }

    private boolean canConnect(Level world, BlockPos pos, Direction direction) {
        var storage = world.getCapability(EnergyApi.SIDED, pos.relative(direction), direction.getOpposite());
        return storage != null && (storage.canReceive() || storage.canExtract());
    }

    // Used in the Waila plugin
    private long getMaxTransfer() {
        return ((ElectricityNetwork) network).tier.getMaxTransfer();
    }

    public InGameInfo collectNetworkInfo() {
        long stored = 0, capacity = 0;
        for (var posNode : network.iterateTickingNodes()) {
            var node = (ElectricityNetworkNode) posNode.getNode();
            stored += node.eu;
            capacity += getMaxTransfer(); // max transfer is also max eu capacity
        }
        return new InGameInfo(stored, capacity, ((ElectricityNetwork) network).stats.getValue(), getMaxTransfer());
    }

    public record InGameInfo(long stored, long capacity, long transfer, long maxTransfer) {
    }
}
