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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeItem extends Item {
    final PipeNetworkType type;
    public final PipeNetworkData defaultData;

    public PipeItem(Settings settings, PipeNetworkType type, PipeNetworkData defaultData) {
        super(settings);
        this.type = type;
        this.defaultData = defaultData;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // TODO: Check BlockItem code and implement all checks.
        // TODO: Check advancement criteria.

        BlockPos placingPos = tryPlace(context);
        if (placingPos != null) {
            World world = context.getWorld();
            PlayerEntity player = context.getPlayer();

            // update adjacent pipes
            world.updateNeighbors(placingPos, Blocks.AIR);
            // remove one from stack
            ItemStack placementStack = context.getStack();
            if (player != null && !player.abilities.creativeMode) {
                placementStack.decrement(1);
            }
            // play placing sound
            BlockState newState = world.getBlockState(placingPos);
            BlockSoundGroup group = newState.getSoundGroup();
            world.playSound(player, placingPos, group.getPlaceSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                    group.getPitch() * 0.8F);

            return ActionResult.success(world.isClient);
        } else {
            // if we couldn't place a pipe, we try to add a connection instead
            placingPos = context.getBlockPos().offset(context.getSide());
            World world = context.getWorld();
            BlockEntity entity = world.getBlockEntity(placingPos);
            if (entity instanceof PipeBlockEntity) {
                PipeBlockEntity pipeEntity = (PipeBlockEntity) entity;
                if (pipeEntity.connections.containsKey(type)) {
                    if (!world.isClient) {
                        pipeEntity.addConnection(type, context.getSide().getOpposite());
                    }
                    // update adjacent pipes
                    world.updateNeighbors(placingPos, Blocks.AIR);
                    // play placing sound
                    BlockState newState = world.getBlockState(placingPos);
                    BlockSoundGroup group = newState.getSoundGroup();
                    world.playSound(context.getPlayer(), placingPos, group.getPlaceSound(), SoundCategory.BLOCKS, (group.getVolume() + 1.0F) / 2.0F,
                            group.getPitch() * 0.8F);
                    return ActionResult.success(world.isClient);
                }
            }
        }
        return super.useOnBlock(context);
    }

    // Try placing the pipe and registering the new pipe to the entity, returns null
    // if it failed
    private BlockPos tryPlace(ItemUsageContext context) {
        BlockPos hitPos = context.getBlockPos();
        BlockPos adjacentPos = hitPos.offset(context.getSide());
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
    private boolean tryPlaceAt(ItemUsageContext context, BlockPos pos) {
        World world = context.getWorld();
        // If there is a block entity we try to add the pipe.
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof PipeBlockEntity) {
            PipeBlockEntity pipeBe = (PipeBlockEntity) be;
            if (pipeBe.canAddPipe(type)) {
                if (!world.isClient()) {
                    pipeBe.addPipe(type, defaultData.clone());
                }
                return true;
            }
        }
        // Otherwise we try replacing the target block.
        if (canPlace(context, pos)) {
            world.setBlockState(pos, MIPipes.BLOCK_PIPE.getDefaultState(), 3); // neighbor update is handled later
            if (!world.isClient()) {
                PipeBlockEntity pipeBe = (PipeBlockEntity) world.getBlockEntity(pos);
                pipeBe.addPipe(type, defaultData.clone());
            }
            return true;
        }
        return false;
    }

    private static boolean canPlace(ItemUsageContext ctx, BlockPos pos) {
        BlockState state = MIPipes.BLOCK_PIPE.getDefaultState();
        ShapeContext shapeContext = ctx.getPlayer() == null ? ShapeContext.absent() : ShapeContext.of(ctx.getPlayer());
        return ctx.getWorld().getBlockState(pos).canReplace(new ItemPlacementContext(ctx)) && state.canPlaceAt(ctx.getWorld(), pos)
                && ctx.getWorld().canPlace(state, pos, shapeContext);
    }
}
