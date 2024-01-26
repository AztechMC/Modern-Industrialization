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
package aztech.modern_industrialization.machines.multiblocks;

import static net.minecraft.core.Direction.*;

import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListener;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Status of a multiblock shape bound to some position and direction.
 */
public class ShapeMatcher implements ChunkEventListener {
    public ShapeMatcher(Level world, BlockPos controllerPos, Direction controllerDirection, ShapeTemplate template) {
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
     * Convert a relative position in the shape template to the real position in the
     * world.
     */
    public static BlockPos toWorldPos(BlockPos controllerPos, Direction controllerDirection, BlockPos templatePos) {
        BlockPos rotatedPos;
        if (controllerDirection == NORTH)
            rotatedPos = templatePos;
        else if (controllerDirection == SOUTH)
            rotatedPos = new BlockPos(-templatePos.getX(), templatePos.getY(), -templatePos.getZ());
        else if (controllerDirection == EAST)
            rotatedPos = new BlockPos(-templatePos.getZ(), templatePos.getY(), templatePos.getX());
        else
            rotatedPos = new BlockPos(templatePos.getZ(), templatePos.getY(), -templatePos.getX());
        return rotatedPos.offset(controllerPos);
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
     * Return true if there was a match, and append matched hatches to the list if
     * it's not null.
     */
    public boolean matches(BlockPos pos, Level world, @Nullable List<HatchBlockEntity> hatches) {
        SimpleMember simpleMember = simpleMembers.get(pos);
        if (simpleMember == null)
            return false;

        BlockState state = world.getBlockState(pos);
        if (simpleMember.matchesState(state))
            return true;

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof HatchBlockEntity hatch) {
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

    public void rematch(Level world) {
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

    public void registerListeners(Level world) {
        for (ChunkPos chunkPos : getSpannedChunks()) {
            ChunkEventListeners.listeners.add(world, chunkPos, this);
        }
    }

    public void unregisterListeners(Level world) {
        for (ChunkPos chunkPos : getSpannedChunks()) {
            ChunkEventListeners.listeners.remove(world, chunkPos, this);
        }
    }

    public int buildMultiblock(Level level) {
        int setBlocks = 0;

        for (var entry : simpleMembers.entrySet()) {
            var current = level.getBlockState(entry.getKey());
            if (!entry.getValue().matchesState(current)) {
                level.setBlockAndUpdate(entry.getKey(), entry.getValue().getPreviewState());
                ++setBlocks;
            }
        }

        return setBlocks;
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
