package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.mixin_impl.WorldRendererGetter;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkManager;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.util.NbtHelper;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.*;

/**
 * The BlockEntity for a pipe.
 */
// TODO: add isClient checks wherever it is necessary
public class PipeBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable, RenderAttachmentBlockEntity {
    private static final int MAX_PIPES = 6;
    private SortedSet<PipeNetworkNode> pipes = new TreeSet<>(Comparator.comparing(PipeNetworkNode::getType));
    SortedMap<PipeNetworkType, Byte> renderedConnections = new TreeMap<>();

    // because we can't access the PipeNetworksComponent in fromTag, we defer the node loading
    private List<Pair<PipeNetworkType, PipeNetworkNode>> unloadedPipes = new ArrayList<>();
    private void loadPipes() {
        for(Pair<PipeNetworkType, PipeNetworkNode> unloaded : unloadedPipes) {
            MIPipes.PIPE_NETWORKS.get(world).getManager(unloaded.getLeft()).nodeLoaded(unloaded.getRight(), pos);
            pipes.add(unloaded.getRight());
        }
        unloadedPipes.clear();
    }

    public PipeBlockEntity() {
        super(MIPipes.BLOCK_ENTITY_TYPE_PIPE);
    }

    void updateConnections() {
        loadPipes();
        for(PipeNetworkNode pipe : pipes) {
            pipe.updateConnections(world, pos);
        }
        markDirty();
        sync();
    }

    /**
     * Check if it's possible to add a pipe.
     * @param type The type to add.
     * @return True if the pipe can be added, false otherwise.
     */
     boolean canAddPipe(PipeNetworkType type) {
        loadPipes();
        if(world.isClient) {
            return pipes.size() < MAX_PIPES && !renderedConnections.containsKey(type);
        } else {
            if (pipes.size() == MAX_PIPES) return false;
            for (PipeNetworkNode pipe : pipes) {
                if (pipe.getType() == type) return false;
            }
            return true;
        }
    }

    /**
     * Add a pipe type. Will not do anything if the pipe couldn't be added.
     * @param type The type to add.
     */
    void addPipe(PipeNetworkType type, PipeNetworkData data) {
        if(!canAddPipe(type)) return;

        PipeNetworkNode node = type.getNodeCtor().get();
        PipeNetworkManager manager = MIPipes.PIPE_NETWORKS.get(world).getManager(type);
        manager.addNode(node, pos, data);
        for(Direction direction : Direction.values()) {
            manager.addLink(pos, direction);
        }
        pipes.add(node);
        node.updateConnections(world, pos);
        markDirty();
        sync();
    }

    /**
     * Remove a pipe type.
     * @param type The type to remove.
     */
    public void removePipe(PipeNetworkType type) {
        loadPipes();
        PipeNetworkNode removedPipe = null;
        for(PipeNetworkNode pipe : pipes) {
            if(pipe.getType() == type) {
                removedPipe = pipe;
                break;
            }
        }
        if(removedPipe == null) {
            throw new IllegalArgumentException("Can't remove type " + type.getIdentifier() + " from BlockEntity at pos " + pos);
        }
        pipes.remove(removedPipe);
        removedPipe.getManager().removeNode(pos);
        markDirty();
        sync();
    }

    @Override
    public void markRemoved() {
        loadPipes();
        // TODO: drop items when necessary, probably not here
        for(PipeNetworkNode pipe : pipes) {
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
        for(PipeNetworkNode pipe : pipes) {
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
        while(tag.contains("pipe_type_" + i)) {
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
        for(PipeNetworkNode pipe : pipes) {
            pipe.tick();
        }
        markDirty();
    }

    public void onChunkUnload() {
        loadPipes();
        for(PipeNetworkNode pipe : pipes) {
            pipe.getManager().nodeUnloaded(pipe, pos);
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        renderedConnections.clear();
        CompoundTag pipesTag = tag.getCompound("pipes");
        for(String key : pipesTag.getKeys()) {
            renderedConnections.put(PipeNetworkType.get(new Identifier(key)), pipesTag.getByte(key));
        }

        ClientWorld clientWorld = (ClientWorld)world;
        WorldRendererGetter wrg = (WorldRendererGetter)clientWorld;
        wrg.modern_industrialization_getWorldRenderer().updateBlock(null, this.pos, null, null, 0);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        loadPipes();
        CompoundTag pipesTag = new CompoundTag();
        for(PipeNetworkNode pipe : pipes) {
            pipesTag.putByte(pipe.getType().getIdentifier().toString(), NbtHelper.encodeDirections(pipe.getRenderedConnections(pos)));
        }
        tag.put("pipes", pipesTag);
        return tag;
    }

    @Override
    public Object getRenderAttachmentData() {
        return new RenderAttachment(new TreeMap<>(this.renderedConnections));
    }

    static class RenderAttachment {
        byte[] renderedConnections;
        PipeNetworkType[] types;

        private RenderAttachment(SortedMap<PipeNetworkType, Byte> renderedConnections) {
            this.renderedConnections = new byte[renderedConnections.size()];
            this.types = new PipeNetworkType[renderedConnections.size()];
            int i = 0;
            for(Map.Entry<PipeNetworkType, Byte> entry : renderedConnections.entrySet()) {
                this.renderedConnections[i] = entry.getValue();
                this.types[i] = entry.getKey();
                i++;
            }
        }
    }
}
