package aztech.modern_industrialization.machines.impl;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.impl.multiblock.HatchBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.material.MIMaterialSetup;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.tools.IWrenchable;
import aztech.modern_industrialization.tools.MachineOverlayItem;
import aztech.modern_industrialization.tools.WrenchItem;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.List;
import java.util.function.Supplier;

/**
 * A generic machine_recipe block.
 */
public class MachineBlock extends Block implements BlockEntityProvider, IWrenchable {
    private final Supplier<MachineBlockEntity> blockEntityFactory;

    public MachineBlock(Supplier<MachineBlockEntity> blockEntityFactory) {
        super(FabricBlockSettings.of(MIMaterialSetup.METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool());
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
            if (entity instanceof MachineBlockEntity) {
                MachineBlockEntity machineBlockEntity = (MachineBlockEntity) entity;
                double x = pos.getX(), y = pos.getY(), z = pos.getZ();
                for(int i = 0; i < machineBlockEntity.size(); ++i) {
                    ItemStack stack = machineBlockEntity.getStack(i);
                    ItemScatterer.spawn(world, x, y, z, stack);
                }
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        // Allow wrench to process useOnBlock
        if(player.inventory.getMainHandStack().getItem() == ModernIndustrialization.ITEM_WRENCH) {
            return ActionResult.PASS;
        }
        // Otherwise open inventory
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if(blockEntity instanceof MachineBlockEntity) {
                player.openHandledScreen((MachineBlockEntity)blockEntity);
            }
            return ActionResult.CONSUME;
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        MachineBlockEntity entity = (MachineBlockEntity) world.getBlockEntity(pos);
        entity.setFacingDirection(placer.getHorizontalFacing().getOpposite());
        if(entity.hasOutput()) {
            if(entity instanceof HatchBlockEntity) {
                entity.setOutputDirection(placer.getHorizontalFacing().getOpposite());
            } else {
                entity.setOutputDirection(placer.getHorizontalFacing());
            }
        }
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if(!(world instanceof ClientWorld)) return getCollisionShape(state, world, pos, context);
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        double[] smallestDistance = new double[]{10000};
        VoxelShape[] closestShape = new VoxelShape[]{VoxelShapes.cuboid(0, 0, 0, 1, 1, 1) };

        if(player.inventory.getMainHandStack().getItem() instanceof MachineOverlayItem) {
            for (VoxelShape shape : MachineOverlay.OVERLAY_SHAPES) {
                float tickDelta = 0; // TODO: fix this
                Vec3d vec3d = player.getCameraPosVec(tickDelta);
                Vec3d vec3d2 = player.getRotationVec(tickDelta);
                double maxDistance = MinecraftClient.getInstance().interactionManager.getReachDistance();
                Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
                BlockHitResult hit = shape.rayTrace(vec3d, vec3d3, pos);
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    double dist = hit.getPos().distanceTo(vec3d);
                    if (dist < smallestDistance[0]) {
                        smallestDistance[0] = dist;
                        closestShape[0] = shape;
                    }
                }
            }
        }
        return closestShape[0];
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
    }

    @Override
    public VoxelShape getRayTraceShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.cuboid(0, 0, 0, 1, 1, 1);
    }

    @Override
    public ActionResult onWrenchUse(ItemUsageContext context) {
        World world = context.getWorld();
        PlayerEntity player = context.getPlayer();
        BlockPos blockPos = context.getBlockPos();
        BlockSoundGroup group = world.getBlockState(blockPos).getSoundGroup();

        Vec3d hitPos = context.getHitPos();
        Vec3d posInBlock = hitPos.subtract(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        for(int i = 0; i < MachineOverlay.OVERLAY_SHAPES.size(); i++) {
            Box box = MachineOverlay.OVERLAY_SHAPES.get(i).getBoundingBox();
            // move slightly towards box center
            Vec3d dir = box.getCenter().subtract(posInBlock).normalize().multiply(1e-4);
            if(box.contains(posInBlock.add(dir))) {
                Direction newDirection = null;
                List<Direction> shapeDirections = MachineOverlay.TOUCHING_DIRECTIONS.get(i);
                if(shapeDirections.size() == 1) newDirection = context.getSide();
                else if(shapeDirections.size() == 3) newDirection = context.getSide().getOpposite();
                else {
                    for(Direction direction : shapeDirections) {
                        if(direction != context.getSide()) {
                            newDirection = direction;
                        }
                    }
                }
                MachineBlockEntity entity = (MachineBlockEntity) context.getWorld().getBlockEntity(blockPos);
                if(player != null && !player.isSneaking()) {
                    if(entity.facingDirection != newDirection && newDirection.getAxis().isHorizontal()) {
                        entity.setFacingDirection(newDirection);
                        world.updateNeighbors(blockPos, null);
                        // TODO play sound
                        return ActionResult.success(world.isClient);
                    }
                    if(entity instanceof MultiblockMachineBlockEntity) {
                        if(!world.isClient) {
                            MultiblockMachineBlockEntity multiblock = (MultiblockMachineBlockEntity) entity;
                            multiblock.rebuildShape();
                            if (multiblock.getErrorMessage() != null) {
                                player.sendMessage(multiblock.getErrorMessage(), true);
                            }
                        }
                        return ActionResult.success(world.isClient);
                    }
                } else if(entity.hasOutput()) {
                    entity.setOutputDirection(newDirection);
                    // TODO play sound
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }
}
