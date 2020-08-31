package aztech.modern_industrialization.machines.impl.multiblock;

import net.minecraft.block.Block;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

public class MultiblockShapes {
    public static MultiblockShape.Entry block(Block block) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return world.getBlockState(pos).isOf(block);
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_block", new TranslatableText(block.getTranslationKey()));
            }
        };
    }

    public static MultiblockShape.Entry blockId(Identifier id) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return Registry.BLOCK.getId(world.getBlockState(pos).getBlock()).equals(id);
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_block", id);
            }
        };
    }

    public static final int HATCH_FLAG_ITEM_INPUT = 1;
    public static final int HATCH_FLAG_ITEM_OUTPUT = 1 << 1;
    public static final int HATCH_FLAG_FLUID_INPUT = 1 << 2;
    public static final int HATCH_FLAG_FLUID_OUTPUT = 1 << 3;
    public static MultiblockShape.Entry hatch(int hatchesFlag) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                if(world.getBlockEntity(pos) instanceof HatchBlockEntity) {
                    HatchBlockEntity entity = (HatchBlockEntity) world.getBlockEntity(pos);
                    return entity.controllerPos == null && (hatchesFlag & (1 << entity.type.getId())) > 0;
                }
                return false;
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_hatch", Integer.toBinaryString(hatchesFlag));
            }
        };
    }

    public static MultiblockShape.Entry or(MultiblockShape.Entry entry1, MultiblockShape.Entry entry2) {
        return new MultiblockShape.Entry() {
            @Override
            public boolean matches(BlockView world, BlockPos pos) {
                return entry1.matches(world, pos) || entry2.matches(world, pos);
            }

            @Override
            public Text getErrorMessage() {
                return new TranslatableText("text.modern_industrialization.shape_error_or", entry1.getErrorMessage(), entry2.getErrorMessage());
            }
        };
    }
}
