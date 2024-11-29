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

import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.machines.components.ShapeValidComponent;
import aztech.modern_industrialization.machines.multiblocks.structure.member.LiteralStructureMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.StructureMember;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListener;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Status of a multiblock shape bound to some position and direction.
 */
public class ShapeMatcher implements ChunkEventListener {
    public ShapeMatcher(Level world, BlockPos controllerPos, Direction controllerDirection, ShapeTemplate template,
            @Nullable ShapeValidComponent shapeValid) {
        this.controllerPos = controllerPos;
        this.controllerDirection = controllerDirection;
        this.template = template;
        this.shapeValid = shapeValid;
        this.simpleMembers = toWorldPos(controllerPos, controllerDirection, template.simpleMembers);
        this.hatchFlags = toWorldPos(controllerPos, controllerDirection, template.hatchFlags);
    }

    protected final BlockPos controllerPos;
    protected final Direction controllerDirection;
    protected final ShapeTemplate template;
    @Nullable
    protected final ShapeValidComponent shapeValid;
    protected final Map<BlockPos, SimpleMember> simpleMembers;
    protected final Map<BlockPos, HatchFlags> hatchFlags;

    protected boolean needsRematch = true;
    protected boolean matchSuccessful = false;
    protected final List<HatchBlockEntity> matchedHatches = new ArrayList<>();

    /**
     * Convert a real position in the world to a relative position in the shape template.
     */
    public static BlockPos toTemplatePos(BlockPos controllerPos, Direction controllerDirection, BlockPos worldPos) {
        BlockPos relativePos = worldPos.subtract(controllerPos);
        if (controllerDirection == Direction.NORTH)
            return new BlockPos(-relativePos.getX(), relativePos.getY(), relativePos.getZ());
        else if (controllerDirection == Direction.SOUTH)
            return new BlockPos(relativePos.getX(), relativePos.getY(), -relativePos.getZ());
        else if (controllerDirection == Direction.EAST)
            return new BlockPos(-relativePos.getZ(), relativePos.getY(), -relativePos.getX());
        else
            return new BlockPos(relativePos.getZ(), relativePos.getY(), relativePos.getX());
    }

    /**
     * Convert a relative position in the shape template to the real position in the
     * world.
     */
    public static BlockPos toWorldPos(BlockPos controllerPos, Direction controllerDirection, BlockPos templatePos) {
        BlockPos rotatedPos;
        if (controllerDirection == Direction.NORTH)
            rotatedPos = new BlockPos(-templatePos.getX(), templatePos.getY(), templatePos.getZ());
        else if (controllerDirection == Direction.SOUTH)
            rotatedPos = new BlockPos(templatePos.getX(), templatePos.getY(), -templatePos.getZ());
        else if (controllerDirection == Direction.EAST)
            rotatedPos = new BlockPos(-templatePos.getZ(), templatePos.getY(), -templatePos.getX());
        else
            rotatedPos = new BlockPos(templatePos.getZ(), templatePos.getY(), templatePos.getX());
        return rotatedPos.offset(controllerPos);
    }

    protected static <V> Map<BlockPos, V> toWorldPos(BlockPos controllerPos, Direction controllerDirection, Map<BlockPos, V> templateMap) {
        Map<BlockPos, V> result = new HashMap<>();
        for (Map.Entry<BlockPos, V> entry : templateMap.entrySet()) {
            result.put(toWorldPos(controllerPos, controllerDirection, entry.getKey()), entry.getValue());
        }
        return result;
    }

    private static Rotation templateRotation(Direction controllerDirection) {
        return switch (controllerDirection) {
        case SOUTH -> Rotation.NONE;
        case NORTH -> Rotation.CLOCKWISE_180;
        case EAST -> Rotation.CLOCKWISE_90;
        case WEST -> Rotation.COUNTERCLOCKWISE_90;
        default -> throw new IllegalStateException("Unexpected value: " + controllerDirection);
        };
    }

    private static Rotation worldRotation(Direction controllerDirection) {
        return switch (controllerDirection) {
        case SOUTH -> Rotation.NONE;
        case NORTH -> Rotation.CLOCKWISE_180;
        case EAST -> Rotation.COUNTERCLOCKWISE_90;
        case WEST -> Rotation.CLOCKWISE_90;
        default -> throw new IllegalStateException("Unexpected value: " + controllerDirection);
        };
    }

    /**
     * Convert a in-world block state to a rotated block state as it should be saved for a shape template.
     */
    public static BlockState toTemplateState(Level level, BlockPos pos, BlockState state, Direction controllerDirection) {
        return state.rotate(level, pos, templateRotation(controllerDirection));
    }

    /**
     * Convert a template block state to a rotated block state as it should be placed in world.
     */
    public static BlockState toWorldState(Level level, BlockPos pos, BlockState state, Direction controllerDirection) {
        return state.rotate(level, pos, worldRotation(controllerDirection));
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
        shapeValid.clearMismatchingBlockEntities();
        matchSuccessful = false;
        needsRematch = true;
    }

    /**
     * Return true if there was a match, and append matched hatches and mismatching block entities to the internal lists.
     */
    public boolean matches(BlockPos pos, Level world) {
        SimpleMember simpleMember = simpleMembers.get(pos);
        if (simpleMember == null)
            return false;

        BlockState state = toTemplateState(world, pos, world.getBlockState(pos), controllerDirection);
        BlockEntity be = world.getBlockEntity(pos);

        if (be instanceof HatchBlockEntity hatch) {
            HatchFlags flags = hatchFlags.get(pos);
            if (flags != null && flags.allows(hatch.getHatchType()) && !hatch.isMatched()) {
                matchedHatches.add(hatch);
                return true;
            }
        }

        boolean matches = simpleMember.matchesState(state, be);
        if (be != null && shapeValid != null) {
            boolean client = world.isClientSide();
            if (client) {
                return shapeValid.isBlockEntityMatchingAt(pos);
            } else if (!matches) {
                shapeValid.addMismatchingBlockEntity(pos);
            }
        }
        return matches;
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

            if (!matches(pos, world)) {
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

    public int buildMultiblock(Level level, boolean structureBlocks) {
        int setBlocks = 0;

        for (var entry : simpleMembers.entrySet()) {
            BlockPos pos = entry.getKey();
            CompoundTag nbt = null;
            if (entry.getValue() instanceof StructureMember member) {
                var optionalStructureBlock = member.asStructureBlock(pos, structureBlocks);
                if (optionalStructureBlock.isPresent()) {
                    BlockState state = optionalStructureBlock.get().getFirst();
                    FastBlockEntity be = optionalStructureBlock.get().getSecond();
                    level.setBlockAndUpdate(pos, state);
                    level.setBlockEntity(be);
                    be.setChanged();
                    be.sync();
                    setBlocks++;
                    continue;
                } else if (member instanceof LiteralStructureMember literalMember) {
                    nbt = literalMember.nbt();
                }
            }
            var currentState = level.getBlockState(pos);
            var currentBlockEntity = level.getBlockEntity(pos);
            if (!entry.getValue().matchesState(currentState, currentBlockEntity)) {
                BlockState state = toWorldState(level, pos, entry.getValue().getPreviewState(), controllerDirection);
                BlockEntity blockEntity = null;
                if (state.getBlock() instanceof EntityBlock entityBlock) {
                    blockEntity = entityBlock.newBlockEntity(pos, state);
                    if (blockEntity != null && nbt != null) {
                        blockEntity.loadCustomOnly(nbt, level.registryAccess());
                    }
                }
                level.setBlockAndUpdate(pos, state);
                if (blockEntity != null) {
                    level.setBlockEntity(blockEntity);
                    blockEntity.setChanged();
                }
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
