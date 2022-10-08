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

import static net.minecraft.core.Direction.NORTH;

import aztech.modern_industrialization.api.FastBlockEntity;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.gui.IPipeScreenHandlerHelper;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.*;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * The BlockEntity for a pipe.
 */
public class PipeBlockEntity extends FastBlockEntity implements IPipeScreenHandlerHelper, RenderAttachmentBlockEntity, WrenchableBlockEntity {
    private static final int MAX_PIPES = 3;
    private static final VoxelShape[][][] SHAPE_CACHE;
    static final VoxelShape DEFAULT_SHAPE;
    /**
     * The current collision shape, i.e. the union of the shapes of the pipe parts.
     */
    VoxelShape currentCollisionShape = Shapes.empty();
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
    SortedMap<PipeNetworkType, CompoundTag> customData = new TreeMap<>();

    // Because we can't access the PipeNetworksComponent in fromTag because the
    // world is null, we defer the node loading.
    private final List<Tuple<PipeNetworkType, PipeNetworkNode>> unloadedPipes = new ArrayList<>();
    /**
     * Set to true in PipeBlock to tell apart unloads and removals.
     */
    boolean stateReplaced = false;

    public void loadPipes() {
        if (level.isClientSide)
            return;

        boolean changed = false;
        for (Tuple<PipeNetworkType, PipeNetworkNode> unloaded : unloadedPipes) {
            PipeNetworks.get((ServerLevel) level).getManager(unloaded.getA()).nodeLoaded(unloaded.getB(), worldPosition);
            pipes.add(unloaded.getB());
            unloaded.getB().updateConnections(level, worldPosition);
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
            pipe.updateConnections(level, worldPosition);
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
        if (level.isClientSide) {
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
        PipeNetworkManager manager = PipeNetworks.get((ServerLevel) level).getManager(type);
        manager.addNode(node, worldPosition, data);
        for (Direction direction : Direction.values()) {
            manager.addLink(worldPosition, direction, false);
        }
        pipes.add(node);
        node.buildInitialConnections(level, worldPosition);
        updateConnections();
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
            throw new IllegalArgumentException("Can't remove type " + type.getIdentifier() + " from BlockEntity at pos " + worldPosition);
        }
        pipes.remove(removedPipe);
        removedPipe.getManager().removeNode(worldPosition);
        onConnectionsChanged();

        // Drop items
        List<ItemStack> droppedStacks = new ArrayList<>();
        removedPipe.appendDroppedStacks(droppedStacks);
        for (ItemStack droppedStack : droppedStacks) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), droppedStack));
        }
    }

    /**
     * Remove a pipe connection.
     */
    public void removeConnection(PipeNetworkType type, Direction direction) {
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                pipe.removeConnection(level, worldPosition, direction);
                pipe.getManager().removeLink(worldPosition, direction);
                onConnectionsChanged();
                return;
            }
        }
    }

    /**
     * Add a pipe connection.
     */
    public void addConnection(Player player, PipeNetworkType type, Direction direction) {
        for (PipeNetworkNode pipe : pipes) {
            if (pipe.getType() == type) {
                pipe.addConnection(this, player, level, worldPosition, direction);
                pipe.getManager().addLink(worldPosition, direction, true);
                pipe.updateConnections(level, worldPosition);

                onConnectionsChanged();
                return;
            }
        }
    }

    public boolean customUse(PipeVoxelShape shape, Player player, InteractionHand hand) {
        for (var node : pipes) {
            if (node.getType() == shape.type) {
                return node.customUse(this, player, hand, shape.direction);
            }
        }
        return false;
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
    public void setRemoved() {
        if (stateReplaced) {
            loadPipes();
            for (PipeNetworkNode pipe : pipes) {
                pipe.getManager().removeNode(worldPosition);
            }
            // Don't clear pipes, otherwise they can't be dropped when broken by hand.
        } else {
            for (PipeNetworkNode pipe : pipes) {
                pipe.getManager().nodeUnloaded(pipe, worldPosition);
            }
        }

        super.setRemoved();
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        int i = 0;
        for (PipeNetworkNode pipe : pipes) {
            tag.putString("pipe_type_" + i, pipe.getType().getIdentifier().toString());
            tag.put("pipe_data_" + i, pipe.toTag(new CompoundTag()));
            i++;
        }
        for (Tuple<PipeNetworkType, PipeNetworkNode> entry : unloadedPipes) {
            tag.putString("pipe_type_" + i, entry.getA().getIdentifier().toString());
            tag.put("pipe_data_" + i, entry.getB().toTag(new CompoundTag()));
            i++;
        }
    }

    @Override
    public void load(CompoundTag tag) {
        if (!tag.contains("pipes")) {
            pipes.clear();

            int i = 0;
            while (tag.contains("pipe_type_" + i)) {
                ResourceLocation typeId = new ResourceLocation(tag.getString("pipe_type_" + i));
                PipeNetworkType type = PipeNetworkType.get(typeId);
                PipeNetworkNode node = type.getNodeCtor().get();
                node.fromTag(tag.getCompound("pipe_data_" + i));
                unloadedPipes.add(new Tuple<>(type, node));
                i++;
            }
        } else {
            connections.clear();
            customData.clear();
            CompoundTag pipesTag = tag.getCompound("pipes");
            for (String key : pipesTag.getAllKeys()) {
                CompoundTag nodeTag = pipesTag.getCompound(key);
                PipeNetworkType type = PipeNetworkType.get(new ResourceLocation(key));
                connections.put(type, NbtHelper.decodeConnections(nodeTag.getByteArray("connections")));
                customData.put(type, nodeTag.getCompound("custom").copy());
            }
            rebuildCollisionShape();

            RenderHelper.forceChunkRemesh(level, worldPosition);
        }
    }

    @Override
    public void clearRemoved() {
        PipeNetworks.scheduleLoadPipe(level, this);
    }

    public void onConnectionsChanged() {
        // Update connections on the server side, we need them for the bounding box.
        Map<PipeNetworkType, PipeEndpointType[]> oldRendererConnections = connections;
        connections = new TreeMap<>();
        for (PipeNetworkNode pipe : pipes) {
            connections.put(pipe.getType(), pipe.getConnections(worldPosition));
        }
        // Then send the update to the client if there was a change.
        if (!connections.equals(oldRendererConnections)) {
            rebuildCollisionShape();
            sync();
        }
        setChanged();
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        loadPipes();
        CompoundTag pipesTag = new CompoundTag();
        for (PipeNetworkNode pipe : pipes) {
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.put("custom", pipe.writeCustomData());
            nodeTag.putByteArray("connections", NbtHelper.encodeConnections(pipe.getConnections(worldPosition)));
            pipesTag.put(pipe.getType().getIdentifier().toString(), nodeTag);
        }
        tag.put("pipes", pipesTag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public Object getRenderAttachmentData() {
        PipeNetworkType[] types = new PipeNetworkType[connections.size()];
        PipeEndpointType[][] renderedConnections = new PipeEndpointType[connections.size()][];
        CompoundTag[] customData = new CompoundTag[connections.size()];
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
        setChanged();
    }

    @Override
    public boolean isWithinUseDistance(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D,
                    (double) this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public boolean doesNodeStillExist(PipeNetworkNode node) {
        return pipes.contains(node);
    }

    @Override
    public boolean useWrench(Player player, InteractionHand hand, BlockHitResult hitResult) {
        return PipeBlock.useWrench(this, player, hand, hitResult);
    }

    static class RenderAttachment {
        PipeNetworkType[] types;
        PipeEndpointType[][] renderedConnections;
        CompoundTag[] customData;

        private RenderAttachment(PipeNetworkType[] types, PipeEndpointType[][] renderedConnections, CompoundTag[] customData) {
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
            shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][NORTH.get3DDataValue()][0], types[slot], null, false));

            // Side connectors
            for (Direction direction : Direction.values()) {
                int connectionType = PipePartBuilder.getRenderType(slot, direction, renderedConnections);
                if (connectionType != 0) {
                    PipeEndpointType connType = renderedConnections[slot][direction.get3DDataValue()];
                    boolean opensGui = connType != null && connType != PipeEndpointType.PIPE && types[slot].opensGui();
                    shapes.add(new PipeVoxelShape(SHAPE_CACHE[slot][direction.get3DDataValue()][connectionType], types[slot], direction, opensGui));
                }
            }
        }

        return shapes;
    }

    private void rebuildCollisionShape() {
        currentCollisionShape = getPartShapes().stream().map(vs -> vs.shape).reduce(Shapes.empty(), Shapes::or);
    }

    static {
        // Note: the centor connector are at connectionType 0.
        SHAPE_CACHE = new VoxelShape[3][6][5];
        for (int slot = 0; slot < 3; slot++) {
            for (Direction direction : Direction.values()) {
                int connectionTypes = slot == 0 ? 2 : slot == 1 ? 4 : 5;
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
                    SHAPE_CACHE[slot][direction.get3DDataValue()][connectionType] = psb.getShape();
                }
            }
        }

        DEFAULT_SHAPE = SHAPE_CACHE[0][0][0];
    }
}
