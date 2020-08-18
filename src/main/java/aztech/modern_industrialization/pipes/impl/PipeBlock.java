package aztech.modern_industrialization.pipes.impl;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

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
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        if (entity != null) {
            double[] smallestDistance = new double[]{10000};
            VoxelShape[] closestShape = new VoxelShape[]{null};

            for (VoxelShape partShape : entity.getPartShapes()) {
                assert (world instanceof ClientWorld);
                float tickDelta = 0; // TODO: fix this
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                Vec3d vec3d = player.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = player.getRotationVec(tickDelta);
                double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = partShape.rayTrace(vec3d, vec3d3, pos);
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
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, BlockView world, BlockPos pos) {
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
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        return entity == null ? PipeBlockEntity.DEFAULT_SHAPE : entity.currentCollisionShape;
    }
}
