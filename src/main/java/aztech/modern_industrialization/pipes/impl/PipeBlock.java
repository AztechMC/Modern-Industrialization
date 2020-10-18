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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.tools.IWrenchable;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PipeBlock extends Block implements BlockEntityProvider, IWrenchable {
    public PipeBlock(Settings settings) {
        super(settings.allowsSpawning(MobSpawning.NO_SPAWN).nonOpaque());
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new PipeBlockEntity();
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos blockPos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);
        BlockSoundGroup group = world.getBlockState(blockPos).getSoundGroup();

        Vec3d hitPos = hit.getPos();
        for (PipeVoxelShape partShape : pipeEntity.getPartShapes()) {
            Vec3d posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for (Box box : partShape.shape.getBoundingBoxes()) {
                // move slightly towards box center
                Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
                if (box.contains(posInBlock.add(dir))) {
                    if (ModernIndustrialization.TAG_WRENCH.contains(player.inventory.getMainHandStack().getItem())) {
                        if (player != null && player.isSneaking()) {
                            boolean removeBlock = pipeEntity.connections.size() == 1;
                            if (!world.isClient) {
                                pipeEntity.removePipe(partShape.type);
                            }
                            if (removeBlock) {
                                world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                            }
                            // update adjacent blocks
                            world.updateNeighbors(blockPos, null);
                            // spawn pipe item
                            world.spawnEntity(
                                    new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
                            // play break sound
                            world.playSound(player, blockPos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                                    group.getPitch() * 0.8F);
                            return ActionResult.success(world.isClient);
                        } else {
                            SoundEvent sound = null;
                            if (partShape.direction == null) {
                                if (!world.isClient) {
                                    pipeEntity.addConnection(partShape.type, hit.getSide());
                                } else {
                                    sound = group.getPlaceSound();
                                }
                            } else {
                                if (!world.isClient) {
                                    pipeEntity.removeConnection(partShape.type, partShape.direction);
                                } else {
                                    sound = group.getBreakSound();
                                }
                            }
                            world.updateNeighbors(blockPos, null);
                            if (sound != null) {
                                world.playSound(player, blockPos, sound, SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 4.0F,
                                        group.getPitch() * 0.8F);
                            }
                            return ActionResult.success(world.isClient);
                        }
                    } else if (partShape.opensGui) {
                        if (!world.isClient) {
                            player.openHandledScreen(pipeEntity.getGui(partShape.type, partShape.direction));
                        }
                        return ActionResult.success(world.isClient);
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult onWrenchUse(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);
        BlockSoundGroup group = world.getBlockState(blockPos).getSoundGroup();

        Vec3d hitPos = context.getHitPos();
        for (PipeVoxelShape partShape : pipeEntity.getPartShapes()) {
            Vec3d posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            for (Box box : partShape.shape.getBoundingBoxes()) {
                // move slightly towards box center
                Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
                if (box.contains(posInBlock.add(dir))) {
                    if (player != null && player.isSneaking()) {
                        boolean removeBlock = pipeEntity.connections.size() == 1;
                        if (!world.isClient) {
                            pipeEntity.removePipe(partShape.type);
                        }
                        if (removeBlock) {
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                        }
                        // update adjacent blocks
                        world.updateNeighbors(blockPos, null);
                        // spawn pipe item
                        world.spawnEntity(
                                new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
                        // play break sound
                        if (world.isClient) {
                            world.playSound(player, blockPos, group.getBreakSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                                    group.getPitch() * 0.8F);
                        }
                        return ActionResult.success(world.isClient);
                    } else {
                        SoundEvent sound;
                        if (partShape.direction == null) {
                            if (!world.isClient) {
                                pipeEntity.addConnection(partShape.type, context.getSide());
                            }
                            sound = group.getPlaceSound();
                        } else {
                            if (!world.isClient) {
                                pipeEntity.removeConnection(partShape.type, partShape.direction);
                            }
                            sound = group.getBreakSound();
                        }
                        world.updateNeighbors(blockPos, null);
                        world.playSound(player, blockPos, sound, SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
                        return ActionResult.success(world.isClient);
                    }
                }
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        PipeBlockEntity pipeEntity = (PipeBlockEntity) lootContext.get(LootContextParameters.BLOCK_ENTITY);
        ItemStack tool = lootContext.get(LootContextParameters.TOOL);
        if (tool != null && FabricToolTags.PICKAXES.contains(tool.getItem())) {
            return pipeEntity.connections.keySet().stream().map(t -> new ItemStack(MIPipes.INSTANCE.getPipeItem(t))).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        PipeNetworkType[] itemType = new PipeNetworkType[] { null };
        if (entity != null) {
            double[] smallestDistance = new double[] { 10000 };
            VoxelShape[] closestShape = new VoxelShape[] { null };

            // TODO: don't copy/paste?
            for (PipeVoxelShape pipePartShape : entity.getPartShapes()) {
                VoxelShape partShape = pipePartShape.shape;
                assert (world instanceof ClientWorld);
                float tickDelta = 0; // TODO: fix this
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                Vec3d vec3d = player.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = player.getRotationVec(tickDelta);
                double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = partShape.raycast(vec3d, vec3d3, pos);
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    double dist = hit.getPos().distanceTo(vec3d);
                    if (dist < smallestDistance[0]) {
                        smallestDistance[0] = dist;
                        closestShape[0] = partShape;
                        itemType[0] = pipePartShape.type;
                    }
                }
            }
        }
        return new ItemStack(itemType[0] == null ? Items.AIR : MIPipes.INSTANCE.getPipeItem(itemType[0]));
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
    public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getCollisionShape(state, world, pos, context);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (world instanceof World && ((World) world).isClient) {
            PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                double[] smallestDistance = new double[] { 10000 };
                VoxelShape[] closestShape = new VoxelShape[] { null };

                for (PipeVoxelShape pipePartShape : entity.getPartShapes()) {
                    VoxelShape partShape = pipePartShape.shape;
                    assert (world instanceof ClientWorld);
                    float tickDelta = 0; // TODO: fix this
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    Vec3d vec3d = player.getCameraPosVec(tickDelta);
                    Vec3d vec3d2 = player.getRotationVec(tickDelta);
                    double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                    Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                    BlockHitResult hit = partShape.raycast(vec3d, vec3d3, pos);
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                        double dist = hit.getPos().distanceTo(vec3d);
                        if (dist < smallestDistance[0]) {
                            smallestDistance[0] = dist;
                            closestShape[0] = partShape;
                        }
                    }
                }

                if (closestShape[0] != null) {
                    return closestShape[0];
                }
            }

            return PipeBlockEntity.DEFAULT_SHAPE;
        } else {
            // LBA compat
            return getCollisionShape(state, world, pos, context);
        }
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
        if (!(be instanceof PipeBlockEntity))
            return PipeBlockEntity.DEFAULT_SHAPE; // Because Mojang fucked up
        PipeBlockEntity entity = (PipeBlockEntity) be;
        return entity.currentCollisionShape;
    }
}
