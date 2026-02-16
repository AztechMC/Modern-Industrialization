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
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PipeBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CAMOUFLAGED = BooleanProperty.create("camouflaged");

    public PipeBlock(Properties settings) {
        super(settings
                .isValidSpawn(MobSpawning.NO_SPAWN)
                .isRedstoneConductor((state, level, pos) -> state.getValue(CAMOUFLAGED))
                // Disable occlusion like this to bypass the occlusion cache,
                // which cannot capture a dependency on the global transparent rendering setting.
                // We still implement an occlusion check in hidesNeighborFace.
                .noOcclusion());
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(CAMOUFLAGED, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(WATERLOGGED, CAMOUFLAGED));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(pos);
        return this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
    }

    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity user, BlockGetter level, BlockPos pos, BlockState state, Fluid type) {
        return !state.getValue(CAMOUFLAGED) && SimpleWaterloggedBlock.super.canPlaceLiquid(user, level, pos, state, type);
    }

    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
        return !state.getValue(CAMOUFLAGED) && SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
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
    public static PipeVoxelShape getHitPart(Level level, BlockPos pos, BlockHitResult hit) {
        return level.getBlockEntity(pos) instanceof PipeBlockEntity pipe ? getHitPart(pipe, hit) : null;
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

    static boolean useWrench(PipeBlockEntity pipe, Player player, InteractionHand hand, BlockHitResult hit) {
        if (pipe.hasCamouflage()) {
            if (player.isShiftKeyDown()) {
                return pipe.tryRemoveCamouflage(player, hand);
            } else {
                return false;
            }
        }

        PipeVoxelShape partShape = getHitPart(pipe, hit);
        if (partShape == null) {
            return false;
        }

        SoundType group = pipe.getBlockState().getSoundType();
        var blockPos = pipe.getBlockPos();
        var world = pipe.getLevel();
        Vec3 hitPos = hit.getLocation();

        if (player.isShiftKeyDown()) {
            boolean removeBlock = pipe.connections.size() == 1;
            if (!world.isClientSide()) {
                pipe.removePipeAndDropContainedItems(partShape.type);
            }
            if (removeBlock) {
                world.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
            }
            // update adjacent blocks
            world.updateNeighborsAt(blockPos, Blocks.AIR);
            // spawn pipe item
            world.addFreshEntity(new ItemEntity(world, hitPos.x, hitPos.y, hitPos.z, new ItemStack(MIPipes.INSTANCE.getPipeItem(partShape.type))));
            // play break sound
            world.playSound(player, blockPos, group.getBreakSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);
        } else {
            SoundEvent sound = null;
            if (partShape.direction == null) {
                if (!world.isClientSide()) {
                    pipe.addConnection(player, partShape.type, hit.getDirection());
                } else {
                    sound = group.getPlaceSound();
                }
            } else {
                if (!world.isClientSide()) {
                    pipe.removeConnection(partShape.type, partShape.direction);
                } else {
                    sound = group.getBreakSound();
                }
            }
            world.updateNeighborsAt(blockPos, Blocks.AIR);
            if (sound != null) {
                world.playSound(player, blockPos, sound, SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
            }
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos blockPos, Player player, InteractionHand hand,
                                       BlockHitResult hit) {
        PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(blockPos);

        if (pipeEntity.tryApplyCamouflage(player, hand)) {
            return InteractionResult.SUCCESS;
        }

        PipeVoxelShape partShape = getHitPart(pipeEntity, hit);
        if (partShape == null || !partShape.opensGui || pipeEntity.hasCamouflage()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!world.isClientSide()) {
            if (!pipeEntity.customUse(partShape, player, hand) && !player.isShiftKeyDown()) {
                var menuOpener = pipeEntity.getGui(partShape.type, partShape.direction);
                player.openMenu(menuOpener, menuOpener::writeAdditionalData);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        PipeBlockEntity pipeEntity = (PipeBlockEntity) builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> droppedStacks = new ArrayList<>();
        for (PipeNetworkNode node : pipeEntity.getNodes()) {
            droppedStacks.add(new ItemStack(MIPipes.INSTANCE.getPipeItem(node.getType())));
            node.appendDroppedStacks(droppedStacks);
        }
        if (pipeEntity.hasCamouflage()) {
            droppedStacks.add(pipeEntity.getCamouflageStack());
        }
        return droppedStacks;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClientSide()) {
            ((PipeBlockEntity) level.getBlockEntity(pos)).updateConnections();
        }
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected int getLightBlock(BlockState state) {
        return state.getValue(CAMOUFLAGED) ? Level.MAX_BRIGHTNESS : 0;
    }

    @Override
    public boolean hasDynamicShape() {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PipeBlockEntity entity))
            return PipeBlockEntity.DEFAULT_SHAPE; // Because Mojang fucked up
        return entity.currentCollisionShape;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return state.getValue(CAMOUFLAGED) ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    // TODO 26.1: audit this
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            pipe.stateReplaced = true;
        }
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState,
            @Nullable BlockPos sourcePos) {
        if (renderView instanceof ServerLevel) {
            if (renderView.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
                return Objects.requireNonNullElse(pipe.camouflage, state);
            }
        } else {
            var data = renderView.getModelData(pos).get(PipeBlockEntity.RenderAttachment.KEY);
            if (data != null) {
                return Objects.requireNonNullElse(data.camouflage(), state);
            }
        }
        return state;
    }

    // TODO 26.1
//    @Override
//    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
//        if (target instanceof BlockHitResult bhr) {
//            if (player.level().getBlockEntity(bhr.getBlockPos()) instanceof PipeBlockEntity pipe) {
//                if (pipe.hasCamouflage()) {
//                    return pipe.getCamouflageStack();
//                }
//
//                var targetedPart = PipeBlock.getHitPart(player.level(), bhr.getBlockPos(), bhr);
//                return new ItemStack(targetedPart == null ? Items.AIR : MIPipes.INSTANCE.getPipeItem(targetedPart.type));
//            }
//        }
//
//        return ItemStack.EMPTY;
//    }

    @Override
    public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
        // If we are a full block, we should always be able to occlude...
        return !MIPipes.transparentCamouflage && state.getValue(CAMOUFLAGED);
    }
}
