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
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PipeBlock extends Block implements BlockEntityProvider {
    public PipeBlock(Settings settings) {
        super(settings.allowsSpawning(MobSpawning.NO_SPAWN).nonOpaque().solidBlock((s, p, w) -> false));
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    private static boolean isPartHit(VoxelShape shape, BlockHitResult hit) {
        var pos = hit.getBlockPos();
        Vec3d posInBlock = hit.getPos().subtract(pos.getX(), pos.getY(), pos.getZ());
        for (Box box : shape.getBoundingBoxes()) {
            // move slightly towards box center
            Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
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
    private static PipeVoxelShape getTargetedPart(BlockView blockView, BlockPos pos) {
        if (!(blockView instanceof World world) || !world.isClient()) {
            return null;
        }
        PipeVoxelShape currentBest = null;
        // Not fond of the use of client-side classes...
        if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            double smallestDistance = 10000;

            for (PipeVoxelShape pipePartShape : pipe.getPartShapes()) {
                VoxelShape partShape = pipePartShape.shape;
                float tickDelta = MinecraftClient.getInstance().getTickDelta();
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                Vec3d vec3d = player.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = player.getRotationVec(tickDelta);
                double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = partShape.raycast(vec3d, vec3d3, pos);
                if (hit != null && isPartHit(partShape, hit)) {
                    double dist = hit.getPos().distanceTo(vec3d);

                    if (dist < smallestDistance) {
                        smallestDistance = dist;
                        currentBest = pipePartShape;
                    }
                }
            }
        }
        return currentBest;
    }

    static boolean useWrench(PipeBlockEntity pipe, PlayerEntity player, Hand hand, BlockHitResult hit) {
        PipeVoxelShape partShape = getHitPart(pipe, hit);
        if (partShape == null) {
            return false;
        }

        BlockSoundGroup group = pipe.getCachedState().getSoundGroup();
        var blockPos = pipe.getPos();
        var world = pipe.getWorld();
        Vec3d hitPos = hit.getPos();

        if (player != null && player.isSneaking()) {
            boolean removeBlock = pipe.connections.size() == 1;
            if (!world.isClient) {
                pipe.removePipeAndDropContainedItems(partShape.type);
            }
            if (removeBlock) {
                world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            }
            // update adjacent blocks
            world.updateNeighbors(blockPos, Blocks.AIR);
            // spawn pipe item
            world.spawnEntity(new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
            // play break sound
            world.playSound(player, blockPos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);
        } else {
            SoundEvent sound = null;
            if (partShape.direction == null) {
                if (!world.isClient) {
                    pipe.addConnection(partShape.type, hit.getSide());
                } else {
                    sound = group.getPlaceSound();
                }
            } else {
                if (!world.isClient) {
                    pipe.removeConnection(partShape.type, partShape.direction);
                } else {
                    sound = group.getBreakSound();
                }
            }
            world.updateNeighbors(blockPos, Blocks.AIR);
            if (sound != null) {
                world.playSound(player, blockPos, sound, SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
            }
        }

        return true;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);

        PipeVoxelShape partShape = getHitPart(pipeEntity, hit);
        if (partShape != null && partShape.opensGui) {
            if (!world.isClient) {
                player.openHandledScreen(pipeEntity.getGui(partShape.type, partShape.direction));
            }
            return ActionResult.success(world.isClient);
        }

        return ActionResult.PASS;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        PipeBlockEntity pipeEntity = (PipeBlockEntity) lootContext.get(LootContextParameters.BLOCK_ENTITY);
        List<ItemStack> droppedStacks = new ArrayList<>();
        for (PipeNetworkNode node : pipeEntity.getNodes()) {
            droppedStacks.add(new ItemStack(MIPipes.INSTANCE.getPipeItem(node.getType())));
            node.appendDroppedStacks(droppedStacks);
        }
        return droppedStacks;
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        var targetedPart = getTargetedPart(world, pos);
        return new ItemStack(targetedPart == null ? Items.AIR : MIPipes.INSTANCE.getPipeItem(targetedPart.type));
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            ((PipeBlockEntity) world.getBlockEntity(pos)).updateConnections();
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean hasDynamicBounds() {
        return true;
    }

    @Override
    public VoxelShape getCameraCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var targetedPart = getTargetedPart(world, pos);
        return targetedPart != null ? targetedPart.shape : PipeBlockEntity.DEFAULT_SHAPE;
    }

    @Override
    public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return getCollisionShape(state, world, pos, null);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PipeBlockEntity entity))
            return PipeBlockEntity.DEFAULT_SHAPE; // Because Mojang fucked up
        return entity.currentCollisionShape;
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            if (world.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
                pipe.stateReplaced = true;
            }
            world.removeBlockEntity(pos);
        }
    }
}
