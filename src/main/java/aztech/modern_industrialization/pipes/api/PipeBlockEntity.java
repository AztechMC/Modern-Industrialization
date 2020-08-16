package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.MIPipes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Tickable;

import java.util.*;

/**
 * The BlockEntity for a pipe.
 */
// TODO: add isClient checks wherever it is necessary
public class PipeBlockEntity extends BlockEntity implements Tickable {
    private static final int MAX_PIPES = 6;
    private SortedSet<PipeNetworkNode> pipes = new TreeSet<>(Comparator.comparing(PipeNetworkNode::getType));

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

    public void updateConnections() {
        loadPipes();
        for(PipeNetworkNode pipe : pipes) {
            pipe.updateConnections(world, pos);
        }
        markDirty();
    }

    /**
     * Add a pipe type.
     * @param type The type to add.
     * @return True if the pipe was placed, false otherwise.
     */
    public boolean addPipe(PipeNetworkType type, PipeNetworkData data) {
        loadPipes();
        if(pipes.size() == MAX_PIPES) return false;
        for(PipeNetworkNode pipe : pipes) {
            if(pipe.getType() == type) return false;
        }

        PipeNetworkNode node = type.getNodeCtor().get();
        MIPipes.PIPE_NETWORKS.get(world).getManager(type).addNode(node, pos, data);
        pipes.add(node);
        node.updateConnections(world, pos);
        markDirty();
        return true;
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
        if(removedPipe != null) {
            throw new IllegalArgumentException("Can't remove type " + type.getIdentifier() + " from BlockEntity at pos " + pos);
        }
        pipes.remove(removedPipe);
        MIPipes.PIPE_NETWORKS.get(world).getManager(type).removeNode(removedPipe, pos);
        markDirty();
    }

    @Override
    public void markRemoved() {
        loadPipes();
        // TODO: drop items when necessary, probably not here
        for(PipeNetworkNode pipe : pipes) {
            MIPipes.PIPE_NETWORKS.get(world).getManager(pipe.getType()).removeNode(pipe, pos);
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
            PipeNetworkType type = PipeNetworkType.getTypes().get(typeId);
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
            pipe.network.tick();
        }
        markDirty();
    }

    public void onChunkUnload() {
        loadPipes();
        for(PipeNetworkNode pipe : pipes) {
            MIPipes.PIPE_NETWORKS.get(world).getManager(pipe.getType()).nodeUnloaded(pipe, pos);
        }
    }
}
