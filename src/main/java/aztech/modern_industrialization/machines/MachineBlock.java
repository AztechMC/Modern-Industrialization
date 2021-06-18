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
import aztech.modern_industrialization.util.MobSpawning;
import java.util.List;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MachineBlock extends MIBlock implements TickableBlock {
    private final BiFunction<BlockPos, BlockState, BlockEntity> blockEntityConstructor;

    public MachineBlock(String machineId, BiFunction<BlockPos, BlockState, BlockEntity> blockEntityConstructor) {
        super(machineId, FabricBlockSettings.of(METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool()
                .allowsSpawning(MobSpawning.NO_SPAWN), false);
        this.blockEntityConstructor = blockEntityConstructor;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return blockEntityConstructor.apply(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MachineBlockEntity) {
                ActionResult beResult = ((MachineBlockEntity) be).onUse(player, hand, MachineOverlay.findHitSide(hit));
                if (beResult.isAccepted()) {
                    world.updateNeighbors(pos, null);
                    return beResult;
                } else {
                    player.openHandledScreen((MachineBlockEntity) be);
                }
            }
            return ActionResult.CONSUME;
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity be = world.getBlockEntity(pos);
        ((MachineBlockEntity) be).onPlaced(placer, itemStack);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.isOf(newState.getBlock())) {
            // Drop items
            BlockEntity be = world.getBlockEntity(pos);

            if (be instanceof MachineBlockEntity) {
                MachineBlockEntity machine = (MachineBlockEntity) be;
                List<ItemStack> dropExtra = machine.dropExtra();
                for (ConfigurableItemStack stack : machine.getInventory().getItemStacks()) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack.getItemKey().toStack(stack.getCount()));
                    stack.setCount(0);
                }

                for (ItemStack extra : dropExtra) {
                    ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), extra);
                }
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
