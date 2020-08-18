package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.Map;
import java.util.function.Consumer;

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
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        if(entity != null) {
            double[] smallestDistance = new double[] { 10000 };
            VoxelShape[] closestShape = new VoxelShape[] { null };

            Consumer<VoxelShape> rayTrace = shape -> {
                if(shape.isEmpty()) return;
                if (world instanceof ClientWorld) {
                    float tickDelta = 0; // TODO: fix this
                    ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    Vec3d vec3d = player.getCameraPosVec(tickDelta);
                    Vec3d vec3d2 = player.getRotationVec(tickDelta);
                    double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                    Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                    BlockHitResult hit = shape.rayTrace(vec3d, vec3d3, pos);
                    if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                        double dist = hit.getPos().distanceTo(vec3d);
                        if(dist < smallestDistance[0]) {
                            smallestDistance[0] = dist;
                            closestShape[0] = shape;
                        }
                    }
                }
            };
            byte[] renderedConnections = new byte[entity.renderedConnections.size()];
            int slot = 0;
            for (Map.Entry<PipeNetworkType, Byte> connections : entity.renderedConnections.entrySet()) {
                renderedConnections[slot++] = connections.getValue();
            }
            for(slot = 0; slot < renderedConnections.length; ++slot) {
                // Center connector
                PipeShapeBuilder centerPsb = new PipeShapeBuilder(PipeModel.getSlotPos(slot), Direction.NORTH);
                centerPsb.centerConnector();
                rayTrace.accept(centerPsb.getShape());

                // Side connectors
                for (Direction direction : Direction.values()) {
                    PipeShapeBuilder psb = new PipeShapeBuilder(PipeModel.getSlotPos(slot), direction);
                    if ((renderedConnections[slot] & (1 << direction.getId())) == 0) {
                        // todo: clean up
                    } else {
                        int connSlot = 0;
                        for (int i = 0; i < slot; i++) {
                            if ((renderedConnections[i] & (1 << direction.getId())) != 0) {
                                connSlot++;
                            }
                        }
                        if (connSlot == slot) {
                            psb.straightLine();
                        } else if (connSlot == 0) {
                            psb.shortBend();
                        } else {
                            psb.longBend();
                        }
                        VoxelShape shape = psb.getShape();
                        rayTrace.accept(shape);
                    }
                }
            }

            if(closestShape[0] != null) {
                return closestShape[0];
            }
        }

        float pipe_width = 2.0f / 16;
        float cl = 0.5f - pipe_width / 2; // center, a bit lower
        float ch = 0.5f + pipe_width / 2; // center, a bit higher*
        return VoxelShapes.cuboid(cl, cl, cl, ch, ch, ch);
    }

    @Override
    public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
        return super.getOpacity(state, world, pos);
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return super.getVisualShape(state, world, pos, context);
    }
}
