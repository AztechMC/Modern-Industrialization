package aztech.modern_industrialization.pipes.impl;

import static net.minecraft.util.math.Direction.NORTH;

import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.*;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * The BlockEntity for a pipe.
 */
// TODO: add isClient checks wherever it is necessary
public class PipeBlockEntity extends BlockEntity
        implements Tickable, BlockEntityClientSerializable, RenderAttachmentBlockEntity, ChunkUnloadBlockEntity {
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
    private SortedSet<PipeNetworkNode> pipes = new TreeSet<>(Comparator.comparing(PipeNetworkNode::getType));
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
    private List<Pair<PipeNetworkType, PipeNetworkNode>> unloadedPipes = new ArrayList<>();

    private void loadPipes() {
        boolean changed = false;
        for (Pair<PipeNetworkType, PipeNetworkNode> unloaded : unloadedPipes) {
            MIPipes.PIPE_NETWORKS.get(world).getManager(unloaded.getLeft()).nodeLoaded(unloaded.getRight(), pos);
            pipes.add(unloaded.getRight());
            unloaded.getRight().updateConnections(world, pos);
            changed = true;
        }
        unloadedPipes.clear();
        if (changed) {
            onConnectionsChanged();
        }
    }

    public PipeBlockEntity() {
        super(MIPipes.BLOCK_ENTITY_TYPE_PIPE);
    }

    void updateConnections() {
        loadPipes();
        for (PipeNetworkNode pipe : pipes) {
            pipe.updateConnections(world, pos);
        }
        onConnectionsChanged();
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
            return pipes.size() < MAX_PIPES && !connections.containsKey(type);
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
        PipeNetworkManager manager = MIPipes.PIPE_NETWORKS.get(world).getManager(type);
        manager.addNode(node, pos, data);
        for (Direction direction : Direction.values()) {
            manager.addLink(pos, direction, false);
        }
        pipes.add(node);
        node.updateConnections(world, pos);
        onConnectionsChanged();
    }

    /**
     * Remove a pipe type.
     * 
     * @param type The type to remove.
     */
    public void removePipe(PipeNetworkType type) {
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
                return pipe.getConnectionGui(direction, this::markDirty, this::sync);
            }
        }
        return null;
    }

    @Override
    public void markRemoved() {
        loadPipes();
        // TODO: drop items when necessary, probably not here
        for (PipeNetworkNode pipe : pipes) {
            pipe.getManager().removeNode(pos);
        }
        pipes.clear();

        super.markRemoved();
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        loadPipes();
        super.toTag(tag);
        int i = 0;
        for (PipeNetworkNode pipe : pipes) {
            tag.putString("pipe_type_" + i, pipe.getType().getIdentifier().toString());
            tag.put("pipe_data_" + i, pipe.toTag(new CompoundTag()));
            i++;
        }
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
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
    public void tick() {
        loadPipes();
        for (PipeNetworkNode pipe : pipes) {
            pipe.tick(world, pos);
            if (pipe.shouldSync()) {
                sync();
            }
        }
        markDirty();
    }

    @Override
    public void onChunkUnload() {
        loadPipes();
        for (PipeNetworkNode pipe : pipes) {
            pipe.getManager().nodeUnloaded(pipe, pos);
        }
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
    public void fromClientTag(CompoundTag tag) {
        connections.clear();
        customData.clear();
        CompoundTag pipesTag = tag.getCompound("pipes");
        for (String key : pipesTag.getKeys()) {
            CompoundTag nodeTag = pipesTag.getCompound(key);
            PipeNetworkType type = PipeNetworkType.get(new Identifier(key));
            connections.put(type, NbtHelper.decodeConnections(nodeTag.getByteArray("connections")));
            customData.put(type, nodeTag.getCompound("custom").copy());
        }
        rebuildCollisionShape();

        ClientWorld clientWorld = (ClientWorld) world;
        WorldRendererGetter wrg = (WorldRendererGetter) clientWorld;
        wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        loadPipes();
        CompoundTag pipesTag = new CompoundTag();
        for (PipeNetworkNode pipe : pipes) {
            CompoundTag nodeTag = new CompoundTag();
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
    Collection<PipeVoxelShape> getPartShapes() {
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
