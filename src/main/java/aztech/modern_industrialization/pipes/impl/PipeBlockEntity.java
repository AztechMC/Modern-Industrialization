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
package aztech.modern_industrialization.pipes.impl;

import static net.minecraft.util.math.Direction.NORTH;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.*;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * The BlockEntity for a pipe.
 */
public class PipeBlockEntity extends FastBlockEntity
        implements IPipeScreenHandlerHelper, BlockEntityClientSerializable, RenderAttachmentBlockEntity, WrenchableBlockEntity {
    private static final int MAX_PIPES = 3;
    private static final VoxelShape[][][] SHAPE_CACHE;
    static final VoxelShape DEFAULT_SHAPE;
    /**
     * The current collision shape, i.e. the union of the shapes of the pipe parts.
     */
    VoxelShape currentCollisionShape = VoxelShapes.empty();
    /**
     * The loaded nodes, server-side only.
     */
    private final SortedSet<PipeNetworkNode> pipes = new TreeSet<>(Comparator.comparing(PipeNetworkNode::getType));
    /**
     * The rendered connections, both client-side for rendering and server-side for
     * bounds check.
     */
    SortedMap<PipeNetworkType, PipeEndpointType[]> connections = new TreeMap<>();
    /**
     * Extra rendering data
     */
    SortedMap<PipeNetworkType, NbtCompound> customData = new TreeMap<>();

    // Because we can't access the PipeNetworksComponent in fromTag because the
    // world is null, we defer the node loading.
    private final List<Pair<PipeNetworkType, PipeNetworkNode>> unloadedPipes = new ArrayList<>();
    /**
     * Set to true in PipeBlock to tell apart unloads and removals.
     */
    boolean stateReplaced = false;

    public void loadPipes() {
        if (world.isClient)
            return;

        boolean changed = false;
        for (Pair<PipeNetworkType, PipeNetworkNode> unloaded : unloadedPipes) {
            PipeNetworks.get((ServerWorld) world).getManager(unloaded.getLeft()).nodeLoaded(unloaded.getRight(), pos);
            pipes.add(unloaded.getRight());
            unloaded.getRight().updateConnections(world, pos);
            changed = true;
        }
        unloadedPipes.clear();
        if (changed) {
            onConnectionsChanged();
        }
    }

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(MIPipes.BLOCK_ENTITY_TYPE_PIPE, pos, state);
    }

    void updateConnections() {
        loadPipes();
        for (PipeNetworkNode pipe : pipes) {
            pipe.updateConnections(world, pos);
        }
        onConnectionsChanged();
    }

    public SortedSet<PipeNetworkNode> getNodes() {
        loadPipes();
        return Collections.unmodifiableSortedSet(pipes);
    }

    /**
     * Check if it's possible to add a pipe.
     * 
     * @param type The type to add.
     * @return True if the pipe can be added, false otherwise.
     */
    boolean canAddPipe(PipeNetworkType type) {
        loadPipes();
        if (world.isClient) {
            return connections.size() < MAX_PIPES && !connections.containsKey(type);
        } else {
            if (pipes.size() == MAX_PIPES)
                return false;
            for (PipeNetworkNode pipe : pipes) {
                if (pipe.getType() == type)
                    return false;
            }
            return true;
        }
    }

    /**
     * Add a pipe type. Will not do anything if the pipe couldn't be added.
     * 
     * @param type The type to add.
     */
    void addPipe(PipeNetworkType type, PipeNetworkData data) {
        if (!canAddPipe(type))
            return;

        PipeNetworkNode node = type.getNodeCtor().get();
        PipeNetworkManager manager = PipeNetworks.get((ServerWorld) world).getManager(type);
        manager.addNode(node, pos, data);
        for (Direction direction : Direction.values()) {
            manager.addLink(pos, direction, false);
        }
        pipes.add(node);
        node.buildInitialConnections(world, pos);
        onConnectionsChanged();
    }

    /**
     * Remove a pipe type.
     * 
     * @param type The type to remove.
     */
    public void removePipeAndDropContainedItems(PipeNetworkType type) {
        loadPipes();
        PipeNetworkNode removedPipe = null;
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                removedPipe = pipe;
                break;
            }
        }
        if (removedPipe == null) {
            throw new IllegalArgumentException("Can't remove type " + type.getIdentifier() + " from BlockEntity at pos " + pos);
        }
        pipes.remove(removedPipe);
        removedPipe.getManager().removeNode(pos);
        onConnectionsChanged();

        // Drop items
        List<ItemStack> droppedStacks = new ArrayList<>();
        removedPipe.appendDroppedStacks(droppedStacks);
        for (ItemStack droppedStack : droppedStacks) {
            world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), droppedStack));
        }
    }

    /**
     * Remove a pipe connection.
     */
    public void removeConnection(PipeNetworkType type, Direction direction) {
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                pipe.removeConnection(world, pos, direction);
                pipe.getManager().removeLink(pos, direction);
                onConnectionsChanged();
                return;
            }
        }
    }

    /**
     * Add a pipe connection.
     */
    public void addConnection(PipeNetworkType type, Direction direction) {
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                pipe.addConnection(world, pos, direction);
                pipe.getManager().addLink(pos, direction, true);
                onConnectionsChanged();
                return;
            }
        }
    }

    public ExtendedScreenHandlerFactory getGui(PipeNetworkType type, Direction direction) {
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                return pipe.getConnectionGui(direction, this);
            }
        }
        return null;
    }

    @Override
    public void markRemoved() {
        if (stateReplaced) {
            loadPipes();
            for (PipeNetworkNode pipe : pipes) {
                pipe.getManager().removeNode(pos);
            }
            // Don't clear pipes, otherwise they can't be dropped when broken by hand.
        } else {
            for (PipeNetworkNode pipe : pipes) {
                pipe.getManager().nodeUnloaded(pipe, pos);
            }
        }

        super.markRemoved();
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        int i = 0;
        for (PipeNetworkNode pipe : pipes) {
            tag.putString("pipe_type_" + i, pipe.getType().getIdentifier().toString());
            tag.put("pipe_data_" + i, pipe.toTag(new NbtCompound()));
            i++;
        }
        for (Pair<PipeNetworkType, PipeNetworkNode> entry : unloadedPipes) {
            tag.putString("pipe_type_" + i, entry.getLeft().getIdentifier().toString());
            tag.put("pipe_data_" + i, entry.getRight().toTag(new NbtCompound()));
            i++;
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);
        pipes.clear();

        int i = 0;
        while (tag.contains("pipe_type_" + i)) {
            Identifier typeId = new Identifier(tag.getString("pipe_type_" + i));
            PipeNetworkType type = PipeNetworkType.get(typeId);
            PipeNetworkNode node = type.getNodeCtor().get();
            node.fromTag(tag.getCompound("pipe_data_" + i));
            unloadedPipes.add(new Pair<>(type, node));
            i++;
        }
    }

    @Override
    public void cancelRemoval() {
        PipeNetworks.scheduleLoadPipe(world, this);
    }

    public void onConnectionsChanged() {
        // Update connections on the server side, we need them for the bounding box.
        Map<PipeNetworkType, PipeEndpointType[]> oldRendererConnections = connections;
        connections = new TreeMap<>();
        for (PipeNetworkNode pipe : pipes) {
            connections.put(pipe.getType(), pipe.getConnections(pos));
        }
        // Then send the update to the client if there was a change.
        if (!connections.equals(oldRendererConnections)) {
            rebuildCollisionShape();
            sync();
        }
        markDirty();
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        connections.clear();
        customData.clear();
        NbtCompound pipesTag = tag.getCompound("pipes");
        for (String key : pipesTag.getKeys()) {
            NbtCompound nodeTag = pipesTag.getCompound(key);
            PipeNetworkType type = PipeNetworkType.get(new Identifier(key));
            connections.put(type, NbtHelper.decodeConnections(nodeTag.getByteArray("connections")));
            customData.put(type, nodeTag.getCompound("custom").copy());
        }
        rebuildCollisionShape();

        RenderHelper.forceChunkRemesh(world, pos);
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        loadPipes();
        NbtCompound pipesTag = new NbtCompound();
        for (PipeNetworkNode pipe : pipes) {
            NbtCompound nodeTag = new NbtCompound();
            nodeTag.put("custom", pipe.writeCustomData());
            nodeTag.putByteArray("connections", NbtHelper.encodeConnections(pipe.getConnections(pos)));
            pipesTag.put(pipe.getType().getIdentifier().toString(), nodeTag);
        }
        tag.put("pipes", pipesTag);
        return tag;
    }

    @Override
    public Object getRenderAttachmentData() {
        PipeNetworkType[] types = new PipeNetworkType[connections.size()];
        PipeEndpointType[][] renderedConnections = new PipeEndpointType[connections.size()][];
        NbtCompound[] customData = new NbtCompound[connections.size()];
        int i = 0;
        for (Map.Entry<PipeNetworkType, PipeEndpointType[]> entry : connections.entrySet()) {
            types[i] = entry.getKey();
            renderedConnections[i] = Arrays.copyOf(entry.getValue(), 6);
            customData[i] = this.customData.get(entry.getKey());
            i++;
        }
        return new RenderAttachment(types, renderedConnections, customData);
    }

    @Override
    public void callSync() {
        sync();
    }

    @Override
    public void callMarkDirty() {
        markDirty();
    }

    @Override
    public boolean isWithinUseDistance(PlayerEntity player) {
        if (this.world.getBlockEntity(this.pos) != this) {
            return false;
        } else {
            return player.squaredDistanceTo((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
                    (double) this.pos.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public boolean doesNodeStillExist(PipeNetworkNode node) {
        return pipes.contains(node);
    }

    @Override
    public boolean useWrench(PlayerEntity player, Hand hand, BlockHitResult hitResult) {
        return PipeBlock.useWrench(this, player, hand, hitResult);
    }

    static class RenderAttachment {
        PipeNetworkType[] types;
        PipeEndpointType[][] renderedConnections;
        NbtCompound[] customData;

        private RenderAttachment(PipeNetworkType[] types, PipeEndpointType[][] renderedConnections, NbtCompound[] customData) {
            this.types = types;
            this.renderedConnections = renderedConnections;
            this.customData = customData;
        }
    }

    /**
     * Get the currently visible shapes.
     */
    public Collection<PipeVoxelShape> getPartShapes() {
        Collection<PipeVoxelShape> shapes = new ArrayList<>();

        PipeEndpointType[][] renderedConnections = new PipeEndpointType[connections.size()][];
        PipeNetworkType[] types = new PipeNetworkType[this.connections.size()];
        int slot = 0;
        for (Map.Entry<PipeNetworkType, PipeEndpointType[]> connections : this.connections.entrySet()) {
            renderedConnections[slot] = connections.getValue();
            types[slot] = connections.getKey();
            slot++;
        }
        for (slot = 0; slot < renderedConnections.length; ++slot) {
            // Center connector
            shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][NORTH.getId()][0], types[slot], null, false));

            // Side connectors
            for (Direction direction : Direction.values()) {
                int connectionType = PipePartBuilder.getRenderType(slot, direction, renderedConnections);
                if (connectionType != 0) {
                    PipeEndpointType connType = renderedConnections[slot][direction.getId()];
                    boolean opensGui = connType != null && connType != PipeEndpointType.PIPE && types[slot].opensGui();
                    shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][direction.getId()][connectionType], types[slot], direction, opensGui));
                }
            }
        }

        return shapes;
    }

    private void rebuildCollisionShape() {
        currentCollisionShape = getPartShapes().stream().map(vs -> vs.shape).reduce(VoxelShapes.empty(), VoxelShapes::union);
    }

    static {
        // Note: the centor connector are at connectionType 0.
        SHAPE_CACHE = new VoxelShape[3][6][5];
        for (int slot = 0; slot < 3; slot++) {
            for (Direction direction : Direction.values()) {
                int connectionTypes = slot == 0 ? 2 : slot == 1 ? 3 : 5;
                for (int connectionType = 0; connectionType < connectionTypes; connectionType++) {
                    PipeShapeBuilder psb = new PipeShapeBuilder(PipePartBuilder.getSlotPos(slot), direction);
                    if (connectionType == 0)
                        psb.centerConnector();
                    else if (connectionType == 1)
                        psb.straightLine(false, false);
                    else if (connectionType == 2)
                        psb.shortBend(false, false);
                    else if (connectionType == 3)
                        psb.farShortBend(false, false);
                    else
                        psb.longBend(false, false);
                    SHAPE_CACHE[slot][direction.getId()][connectionType] = psb.getShape();
                }
            }
        }

        DEFAULT_SHAPE = SHAPE_CACHE[0][0][0];
    }
}
