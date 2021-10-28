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
import aztech.modern_industrialization.util.MIBlockApiCache;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ElectricityNetworkNode extends PipeNetworkNode {
    private List<Direction> connections = new ArrayList<>();
    private final List<MIBlockApiCache<EnergyMoveable, @NotNull Direction>> caches = new ArrayList<>();
    long eu = 0;

    public void appendAttributes(World world, BlockPos pos, List<EnergyInsertable> insertables, List<EnergyExtractable> extractables) {
        if (caches.size() != connections.size()) {
            caches.clear();
            for (Direction direction : connections) {
                caches.add(MIBlockApiCache.create(EnergyApi.MOVEABLE, (ServerWorld) world, pos.offset(direction)));
            }
        }
        for (int i = 0; i < connections.size(); ++i) {
            Direction targetDir = connections.get(i).getOpposite();
            EnergyMoveable moveable = caches.get(i).find(targetDir);
            if (moveable instanceof EnergyInsertable)
                insertables.add((EnergyInsertable) moveable);
            if (moveable instanceof EnergyExtractable)
                extractables.add((EnergyExtractable) moveable);
        }
    }

    @Override
    public void buildInitialConnections(World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (canConnect(world, pos, direction)) {
                connections.add(direction);
            }
        }
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
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
            connections[direction.getId()] = PIPE;
        }
        for (Direction connection : this.connections) {
            connections[connection.getId()] = BLOCK;
        }
        return connections;
    }

    @Override
    public void removeConnection(World world, BlockPos pos, Direction direction) {
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
    public void addConnection(World world, BlockPos pos, Direction direction) {
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
    public NbtCompound toTag(NbtCompound tag) {
        tag.putByte("connections", NbtHelper.encodeDirections(connections));
        tag.putLong("eu", eu);
        return tag;
    }

    @Override
    public void fromTag(NbtCompound tag) {
        connections = new ArrayList<>(Arrays.asList(NbtHelper.decodeDirections(tag.getByte("connections"))));
        caches.clear();
        eu = tag.getLong("eu");
    }

    private boolean canConnect(World world, BlockPos pos, Direction direction) {
        EnergyMoveable moveable = EnergyApi.MOVEABLE.find(world, pos.offset(direction), direction.getOpposite());
        CableTier tier = ((ElectricityNetwork) network).tier;
        return moveable instanceof EnergyInsertable && ((EnergyInsertable) moveable).canInsert(tier)
                || moveable instanceof EnergyExtractable && ((EnergyExtractable) moveable).canExtract(tier);
    }

    // Used in the Waila plugin
    public long getEu() {
        return eu;
    }

    public long getMaxEu() {
        return getTier().getMaxTransfer();
    }

    public CableTier getTier() {
        return ((ElectricityNetwork) network).tier;
    }
}
