package aztech.modern_industrialization.pipes.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class PipeBlock extends Block implements BlockEntityProvider {
    public PipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return new PipeBlockEntity();
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if(!world.isClient) {
            ((PipeBlockEntity) world.getBlockEntity(pos)).updateConnections();
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        // TODO: no copy paste
        float pipe_width = 2.0f / 16;
        float cl = 0.5f - pipe_width / 2; // center, a bit lower
        float ch = 0.5f + pipe_width / 2; // center, a bit higher
        return VoxelShapes.cuboid(cl, cl, cl, ch, ch, ch);
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return super.getOpacity(state, world, pos);
    }
}
