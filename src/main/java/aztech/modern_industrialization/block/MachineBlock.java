package aztech.modern_industrialization.block;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blockentity.AbstractMachineBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.tools.IWrenchable;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * A generic machine block.
 */
public class MachineBlock extends Block implements BlockEntityProvider {
    private final Supplier<AbstractMachineBlockEntity> blockEntityFactory;

    public MachineBlock(Supplier<AbstractMachineBlockEntity> blockEntityFactory) {
        super(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView blockView) {
        return blockEntityFactory.get();
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if(!state.isOf(newState.getBlock())) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof AbstractMachineBlockEntity) {
                AbstractMachineBlockEntity machineBlockEntity = (AbstractMachineBlockEntity) entity;
                double x = pos.getX(), y = pos.getY(), z = pos.getZ();
                for(int i = 0; i < machineBlockEntity.size(); ++i) {
                    ItemStack stack = machineBlockEntity.getStack(i);
                    if(stack.getItem() != ModernIndustrialization.ITEM_FLUID_SLOT) {
                        ItemScatterer.spawn(world, x, y, z, stack);
                    }
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof AbstractMachineBlockEntity) {
                player.openHandledScreen((AbstractMachineBlockEntity)blockEntity);
            }
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        AbstractMachineBlockEntity entity = (AbstractMachineBlockEntity) world.getBlockEntity(pos);
        entity.setFacingDirection(placer.getHorizontalFacing().getOpposite());
    }
}
