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
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkData;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkData;
import aztech.modern_industrialization.pipes.item.ItemNetworkData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;

public class PipeItem extends Item {
    public final PipeNetworkType type;
    public final PipeNetworkData defaultData;

    public PipeItem(Properties settings, PipeNetworkType type, PipeNetworkData defaultData) {
        super(settings);
        this.type = type;
        this.defaultData = defaultData;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // TODO: Check BlockItem code and implement all checks.
        // TODO: Check advancement criteria.

        BlockPos placingPos = tryPlace(context);
        if (placingPos != null) {
            Level world = context.getLevel();
            Player player = context.getPlayer();

            // update adjacent pipes
            world.blockUpdated(placingPos, Blocks.AIR);
            // remove one from stack
            ItemStack placementStack = context.getItemInHand();
            if (player != null && !player.getAbilities().instabuild) {
                placementStack.shrink(1);
            }
            // play placing sound
            BlockState newState = world.getBlockState(placingPos);
            SoundType group = newState.getSoundType();
            world.playSound(player, placingPos, group.getPlaceSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);

            return InteractionResult.sidedSuccess(world.isClientSide);
        } else {
            // if we couldn't place a pipe, we try to add a connection instead
            placingPos = context.getClickedPos().relative(context.getClickedFace());
            Level world = context.getLevel();
            BlockEntity entity = world.getBlockEntity(placingPos);
            if (entity instanceof PipeBlockEntity pipeEntity) {
                if (pipeEntity.connections.containsKey(type)) {
                    if (!world.isClientSide) {
                        pipeEntity.addConnection(context.getPlayer(), type, context.getClickedFace().getOpposite());
                    }
                    // update adjacent pipes
                    world.blockUpdated(placingPos, Blocks.AIR);
                    // play placing sound
                    BlockState newState = world.getBlockState(placingPos);
                    SoundType group = newState.getSoundType();
                    world.playSound(context.getPlayer(), placingPos, group.getPlaceSound(), SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                            group.getPitch() * 0.8F);
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
        }
        return super.useOn(context);
    }

    // Try placing the pipe and registering the new pipe to the entity, returns null
    // if it failed
    private BlockPos tryPlace(UseOnContext context) {
        BlockPos hitPos = context.getClickedPos();
        BlockPos adjacentPos = hitPos.relative(context.getClickedFace());
        if (tryPlaceAt(context, hitPos)) {
            return hitPos;
        } else if (tryPlaceAt(context, adjacentPos)) {
            return adjacentPos;
        } else {
            return null;
        }
    }

    /**
     * Try adding the pipe to an existing block entity, or replacing the current
     * state if that was not possible.
     *
     * @return True if succeeded, false otherwise.
     */
    private boolean tryPlaceAt(UseOnContext context, BlockPos pos) {
        Level world = context.getLevel();
        // If there is a block entity we try to add the pipe.
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PipeBlockEntity pipeBe) {
            if (pipeBe.canAddPipe(type)) {
                if (!world.isClientSide()) {
                    pipeBe.addPipe(type, defaultData.clone());
                }
                return true;
            }
        }
        // Otherwise we try replacing the target block.
        if (canPlace(context, pos)) {
            boolean waterLog = context.getLevel().getFluidState(pos).getType() == Fluids.WATER;

            // neighbor update is handled later
            world.setBlock(pos, MIPipes.BLOCK_PIPE.get().defaultBlockState().setValue(PipeBlock.WATERLOGGED, waterLog), 3);
            if (!world.isClientSide()) {
                PipeBlockEntity pipeBe = (PipeBlockEntity) world.getBlockEntity(pos);
                pipeBe.addPipe(type, defaultData.clone());
            }
            return true;
        }
        return false;
    }

    private static boolean canPlace(UseOnContext ctx, BlockPos pos) {
        BlockState state = MIPipes.BLOCK_PIPE.get().defaultBlockState();
        CollisionContext shapeContext = ctx.getPlayer() == null ? CollisionContext.empty() : CollisionContext.of(ctx.getPlayer());
        return ctx.getLevel().getBlockState(pos).canBeReplaced(new BlockPlaceContext(ctx)) && state.canSurvive(ctx.getLevel(), pos)
                && ctx.getLevel().isUnobstructed(state, pos, shapeContext);
    }

    public boolean isItemPipe() {
        return this.defaultData instanceof ItemNetworkData;
    }

    public boolean isFluidPipe() {
        return this.defaultData instanceof FluidNetworkData;
    }

    public boolean isCable() {
        return this.defaultData instanceof ElectricityNetworkData;
    }
}
