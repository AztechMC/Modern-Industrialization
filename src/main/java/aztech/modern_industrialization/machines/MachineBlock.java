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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.api.TickableBlock;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MachineBlock extends MIBlock implements TickableBlock {
    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityConstructor;

    /**
     * Used by the model loading code to identify machine models.
     * TODO: refactor
     */
    public static final Map<String, MachineCasing> REGISTERED_MACHINES = new HashMap<>();

    public MachineBlock(String machineId, BiFunction<BlockPos, BlockState, BlockEntity> blockEntityConstructor) {
        super(machineId, FabricBlockSettings.of(METAL_MATERIAL).destroyTime(4.0f).requiresCorrectToolForDrops()
                .isValidSpawn(MobSpawning.NO_SPAWN), MIBlock.FLAG_BLOCK_LOOT);
        setPickaxeMineable();
        this.blockEntityConstructor = blockEntityConstructor;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityConstructor.apply(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MachineBlockEntity) {
                InteractionResult beResult = ((MachineBlockEntity) be).onUse(player, hand, MachineOverlay.findHitSide(hit));
                if (beResult.consumesAction()) {
                    world.blockUpdated(pos, Blocks.AIR);
                    return beResult;
                } else {
                    player.openMenu((MachineBlockEntity) be);
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
}
