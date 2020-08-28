package aztech.modern_industrialization.blocks.forgeHammer;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ForgeHammerBlock extends Block {

    private VoxelShape shape;
    private int part_height[] = {4, 1, 5, 5};
    private int part_width[] = {14, 10, 8, 14};

    public ForgeHammerBlock() {
        super(FabricBlockSettings.of(Material.METAL).hardness(6.0f).resistance(1200).sounds(BlockSoundGroup.ANVIL));
        VoxelShape[] parts = new VoxelShape[part_height.length];
        float currentY = 0;
        for(int i = 0; i < part_height.length; i++) {
            float o = (16 - part_width[i])/32.0f;
            float e = o + part_width[i]/16.0f;
            parts[i] = VoxelShapes.cuboid(o, currentY, o, e, currentY+part_height[i]/16.0f, e);
            currentY += part_height[i]/16.0f;
        }
        shape = parts[0];
        for(int i = 1; i < part_height.length; i++) {
            shape = VoxelShapes.union(shape, parts[i]);
        }


    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            //player.openHandledScreen();
            return ActionResult.CONSUME;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        return shape;
    }


}
