package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.machinesv2.multiblocks.world.ChunkEventListener;
import aztech.modern_industrialization.machinesv2.multiblocks.world.ChunkEventListeners;
import com.google.common.base.Preconditions;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.minecraft.util.math.Direction.*;

/**
 * Status of a multiblock shape bound to some position and direction.
 */
public class ShapeMatcher implements ChunkEventListener {
    public ShapeMatcher(World world, BlockPos controllerPos, Direction controllerDirection, ShapeTemplate template) {
        this.controllerPos = controllerPos;
        this.template = template;
        this.simpleMembers = toWorldPos(controllerPos, controllerDirection, template.simpleMembers);
        this.hatchFlags = toWorldPos(controllerPos, controllerDirection, template.hatchFlags);
    }

    private final BlockPos controllerPos;
    private final ShapeTemplate template;
    private final Map<BlockPos, SimpleMember> simpleMembers;
    private final Map<BlockPos, HatchFlags> hatchFlags;

    private boolean needsRematch = true;
    private boolean matchSuccessful = false;
    private final List<HatchBlockEntity> matchedHatches = new ArrayList<>();

    /**
     * Convert a relative position in the shape template to the real position in the world.
     */
    private static BlockPos toWorldPos(BlockPos controllerPos, Direction controllerDirection, BlockPos templatePos) {
        BlockPos rotatedPos;
        if (controllerDirection == NORTH)
            rotatedPos = templatePos;
        else if (controllerDirection == SOUTH)
            rotatedPos = new BlockPos(-templatePos.getX(), templatePos.getY(), -templatePos.getZ());
        else if (controllerDirection == EAST)
            rotatedPos = new BlockPos(-templatePos.getZ(), templatePos.getY(), templatePos.getX());
        else
            rotatedPos = new BlockPos(templatePos.getZ(), templatePos.getY(), -templatePos.getX());
        return rotatedPos.add(controllerPos);
    }

    private static <V> Map<BlockPos, V> toWorldPos(BlockPos controllerPos, Direction controllerDirection, Map<BlockPos, V> templateMap) {
        Map<BlockPos, V> result = new HashMap<>();
        for (Map.Entry<BlockPos, V> entry : templateMap.entrySet()) {
            result.put(toWorldPos(controllerPos, controllerDirection, entry.getKey()), entry.getValue());
        }
        return result;
    }

    public Set<BlockPos> getPositions() {
        return new HashSet<>(simpleMembers.keySet());
    }

    public SimpleMember getSimpleMember(BlockPos pos) {
        return Objects.requireNonNull(simpleMembers.get(pos));
    }

    @Nullable
    public HatchFlags getHatchFlags(BlockPos pos) {
        return hatchFlags.get(pos);
    }

    public List<HatchBlockEntity> getMatchedHatches() {
        return Collections.unmodifiableList(matchedHatches);
    }

    public void unlinkHatches() {
        for (HatchBlockEntity hatch : matchedHatches) {
            hatch.unlink();
        }

        matchedHatches.clear();
        matchSuccessful = false;
        needsRematch = true;
    }

    /**
     * Return true if there was a match, and append matched hatches to the list if it's not null.
     */
    public boolean matches(BlockPos pos, World world, @Nullable List<HatchBlockEntity> hatches) {
        SimpleMember simpleMember = simpleMembers.get(pos);
        if (simpleMember == null) return false;

        BlockState state = world.getBlockState(pos);
        if (simpleMember.matchesState(state)) return true;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof HatchBlockEntity) {
            HatchBlockEntity hatch = (HatchBlockEntity) be;
            HatchFlags flags = hatchFlags.get(pos);
            if (flags != null && flags.allows(hatch.getHatchType()) && !hatch.isMatched()) {
                if (matchedHatches != null) {
                    matchedHatches.add(hatch);
                }
                return true;
            }
        }

        return false;
    }

    public boolean needsRematch() {
        return needsRematch;
    }

    public boolean isMatchSuccessful() {
        return matchSuccessful && !needsRematch;
    }

    public void rematch(World world) {
        Preconditions.checkArgument(needsRematch);
        unlinkHatches();
        matchSuccessful = true;

        for (BlockPos pos : simpleMembers.keySet()) {
            // TODO: check if the chunk is loaded

            if (!matches(pos, world, matchedHatches)) {
                matchSuccessful = false;
            }
        }

        if (!matchSuccessful) {
            matchedHatches.clear();
        } else {
            for (HatchBlockEntity hatch : matchedHatches) {
                hatch.link(template.hatchCasing);
            }
        }

        needsRematch = false;
    }

    public Set<ChunkPos> getSpannedChunks() {
        Set<ChunkPos> spannedChunks = new HashSet<>();
        for (BlockPos pos : simpleMembers.keySet()) {
            spannedChunks.add(new ChunkPos(pos));
        }
        return spannedChunks;
    }

    public void registerListeners(World world) {
        for (ChunkPos chunkPos : getSpannedChunks()) {
            ChunkEventListeners.listeners.add(world, chunkPos, this);
        }
    }

    public void unregisterListeners(World world) {
        for (ChunkPos chunkPos : getSpannedChunks()) {
            ChunkEventListeners.listeners.remove(world, chunkPos, this);
        }
    }

    @Override
    public void onBlockUpdate(BlockPos pos) {
        if (simpleMembers.containsKey(pos)) {
            needsRematch = true;
        }
    }

    @Override
    public void onUnload() {
        needsRematch = true;
    }

    @Override
    public void onLoad() {
        needsRematch = true;
    }
}
