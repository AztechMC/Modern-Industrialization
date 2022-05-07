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

import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block implements EntityBlock {
    public PipeBlock(Properties settings) {
        super(settings.isValidSpawn(MobSpawning.NO_SPAWN).noOcclusion().isRedstoneConductor((s, p, w) -> false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    private static boolean isPartHit(VoxelShape shape, BlockHitResult hit) {
        var pos = hit.getBlockPos();
        Vec3 posInBlock = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        for (AABB box : shape.toAabbs()) {
            // move slightly towards box center
            Vec3 dir = box.getCenter().subtract(posInBlock).normalize().scale(1e-4);
            if (box.contains(posInBlock.add(dir))) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static PipeVoxelShape getHitPart(PipeBlockEntity pipe, BlockHitResult hit) {
        for (PipeVoxelShape partShape : pipe.getPartShapes()) {
            if (isPartHit(partShape.shape, hit)) {
                return partShape;
            }
        }
        return null;
    }

    @Nullable
    private static PipeVoxelShape getTargetedPart(BlockGetter blockView, BlockPos pos) {
        if (!(blockView instanceof Level world) || !world.isClientSide()) {
            return null;
        }
        PipeVoxelShape currentBest = null;
        // Not fond of the use of client-side classes...
        if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            double smallestDistance = 10000;

            for (PipeVoxelShape pipePartShape : pipe.getPartShapes()) {
                VoxelShape partShape = pipePartShape.shape;
                float tickDelta = Minecraft.getInstance().getFrameTime();
                LocalPlayer player = Minecraft.getInstance().player;
                Vec3 vec3d = player.getEyePosition(tickDelta);
                Vec3 vec3d2 = player.getViewVector(tickDelta);
                double maxDistance = Minecraft.getInstance().gameMode.getPickRange();
                Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = partShape.clip(vec3d, vec3d3, pos);
                if (hit != null && isPartHit(partShape, hit)) {
                    double dist = hit.getLocation().distanceTo(vec3d);

                    if (dist < smallestDistance) {
                        smallestDistance = dist;
                        currentBest = pipePartShape;
                    }
                }
            }
        }
        return currentBest;
    }

    static boolean useWrench(PipeBlockEntity pipe, Player player, InteractionHand hand, BlockHitResult hit) {
        PipeVoxelShape partShape = getHitPart(pipe, hit);
        if (partShape == null) {
            return false;
        }

        SoundType group = pipe.getBlockState().getSoundType();
        var blockPos = pipe.getBlockPos();
        var world = pipe.getLevel();
        Vec3 hitPos = hit.getLocation();

        if (player != null && player.isShiftKeyDown()) {
            boolean removeBlock = pipe.connections.size() == 1;
            if (!world.isClientSide) {
                pipe.removePipeAndDropContainedItems(partShape.type);
            }
            if (removeBlock) {
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
            // update adjacent blocks
            world.blockUpdated(blockPos, Blocks.AIR);
            // spawn pipe item
            world.addFreshEntity(new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
            // play break sound
            world.playSound(player, blockPos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);
        } else {
            SoundEvent sound = null;
            if (partShape.direction == null) {
                if (!world.isClientSide) {
                    pipe.addConnection(partShape.type, hit.getDirection());
                } else {
                    sound = group.getPlaceSound();
                }
            } else {
                if (!world.isClientSide) {
                    pipe.removeConnection(partShape.type, partShape.direction);
                } else {
                    sound = group.getBreakSound();
                }
            }
            world.blockUpdated(blockPos, Blocks.AIR);
            if (sound != null) {
                world.playSound(player, blockPos, sound, SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);

        PipeVoxelShape partShape = getHitPart(pipeEntity, hit);
        if (partShape != null && partShape.opensGui) {
            if (!world.isClientSide) {
                player.openMenu(pipeEntity.getGui(partShape.type, partShape.direction));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
        PipeBlockEntity pipeEntity = (PipeBlockEntity) lootContext.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> droppedStacks = new ArrayList<>();
        for (PipeNetworkNode node : pipeEntity.getNodes()) {
            droppedStacks.add(new ItemStack(MIPipes.INSTANCE.getPipeItem(node.getType())));
            node.appendDroppedStacks(droppedStacks);
        }
        return droppedStacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        var targetedPart = getTargetedPart(world, pos);
        return new ItemStack(targetedPart == null ? Items.AIR : MIPipes.INSTANCE.getPipeItem(targetedPart.type));
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClientSide) {
            ((PipeBlockEntity) world.getBlockEntity(pos)).updateConnections();
        }
        super.neighborChanged(state, world, pos, block, fromPos, notify);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getLightBlock(BlockState state, BlockGetter world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return getCollisionShape(state, world, pos, context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        var targetedPart = getTargetedPart(world, pos);
        return targetedPart != null ? targetedPart.shape : PipeBlockEntity.DEFAULT_SHAPE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getBlockSupportShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PipeBlockEntity entity))
            return PipeBlockEntity.DEFAULT_SHAPE; // Because Mojang fucked up
        return entity.currentCollisionShape;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isPathfindable(BlockState state, BlockGetter world, BlockPos pos, PathComputationType type) {
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
                pipe.stateReplaced = true;
            }
            world.removeBlockEntity(pos);
        }
    }
}
