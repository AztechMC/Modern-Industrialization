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
package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.blocks.TickableBlock;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.proxy.CommonProxy;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MachineBlock extends Block implements TickableBlock {

    private final BiFunction<BlockPos, BlockState, MachineBlockEntity> blockEntityConstructor;
    private volatile MachineBlockEntity blockEntityInstance = null; // Used for tooltip, information, BER registration, etc...

    public MachineBlock(BiFunction<BlockPos, BlockState, MachineBlockEntity> blockEntityConstructor, Properties properties) {
        super(properties);
        this.blockEntityConstructor = blockEntityConstructor;
    }

    @Override
    public MachineBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityConstructor.apply(pos, state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MachineBlockEntity machine) {
                InteractionResult beResult = machine.onUse(player, hand, MachineOverlay.findHitSide(hit));
                if (beResult.consumesAction()) {
                    world.blockUpdated(pos, Blocks.AIR);
                    return beResult;
                } else {
                    machine.openMenu((ServerPlayer) player);
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity be = world.getBlockEntity(pos);
        ((MachineBlockEntity) be).onPlaced(placer, itemStack);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            // Drop items
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof MachineBlockEntity machine) {
                List<ItemStack> dropExtra = machine.dropExtra();
                for (ConfigurableItemStack stack : machine.getInventory().getItemStacks()) {
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack.getResource().toStack((int) stack.getAmount()));
                    stack.setAmount(0);
                }

                for (ItemStack extra : dropExtra) {
                    Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), extra);
                }
            }
        }
        super.onRemove(state, world, pos, newState, moved);
    }

    public MachineBlockEntity getBlockEntityInstance() {
        if (blockEntityInstance == null) {
            blockEntityInstance = newBlockEntity(BlockPos.ZERO, this.defaultBlockState());
        }
        return blockEntityInstance;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return getBlockEntityInstance().hasComparatorOutput();
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            return machine.getComparatorOutput();
        }
        return 0;
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            machine.refreshRedstoneStatus();
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.getBlockEntity(pos) instanceof MachineBlockEntity machine) {
            machine.refreshRedstoneStatus();
        }
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState,
            @Nullable BlockPos sourcePos) {
        if (renderView instanceof ServerLevel) {
            // Well... we pull the information from the model, so nothing to do here.
            return state;
        } else {
            return CommonProxy.INSTANCE.getMachineCasingBlockState(state, renderView, pos);
        }
    }
}
