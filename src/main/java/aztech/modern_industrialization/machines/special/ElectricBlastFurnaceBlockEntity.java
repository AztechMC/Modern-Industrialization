package aztech.modern_industrialization.machines.special;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShape;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShapes;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.material.MIMaterials;
import net.minecraft.block.Block;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Direction;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ElectricBlastFurnaceBlockEntity extends MultiblockMachineBlockEntity {
    private static final Block[] COIL_MATERIALS;
    private static final MultiblockShape[] COIL_SHAPE;
    private static final int[] COIL_EU;
    private int coilId = -1;

    public ElectricBlastFurnaceBlockEntity(MachineFactory factory, MachineRecipeType type) {
        super(factory, type, null);
    }

    @Override
    protected void matchShape() {
        Block coilType = world.getBlockState(pos.offset(Direction.UP)).getBlock();
        int coilId = 0;
        for(; coilId < COIL_MATERIALS.length; coilId++) {
            if(coilType == COIL_MATERIALS[coilId]) break;
        }

        if(coilId == COIL_MATERIALS.length) {
            ready = false;
            this.coilId = -1;
            this.errorMessage = new TranslatableText("text.modern_industrialization.shape_error_no_coil", pos.offset(Direction.UP));
        } else {
            ready = COIL_SHAPE[coilId].matchShape(world, pos, facingDirection, linkedHatches, linkedStructureBlocks);
            this.coilId = ready ? coilId : -1;
            this.errorMessage = COIL_SHAPE[coilId].getErrorMessage();
        }
    }

    @Override
    protected Iterable<MachineRecipe> getRecipes() {
        return StreamSupport.stream(super.getRecipes().spliterator(), false).filter(r -> r.eu <= COIL_EU[coilId]).collect(Collectors.toList());
    }

    static {
        COIL_MATERIALS = new Block[] {
                MIMaterials.cupronickel.getBlock("coil"),
        };
        COIL_EU = new int[] {
                128,
        };
        COIL_SHAPE = new MultiblockShape[COIL_MATERIALS.length];

        int i = 0;
        for(Block coilBlock : COIL_MATERIALS) {
            COIL_SHAPE[i] = new MultiblockShape();

            MultiblockShape.Entry optionalHatch = MultiblockShapes.or(MultiblockShapes.block(MIBlock.HEATPROOF_MACHINE_CASING), MultiblockShapes.hatch(31));
            MultiblockShape.Entry coil = MultiblockShapes.block(coilBlock);

            for(int x = -1; x <= 1; x++) {
                for(int z = 0; z < 3; ++z) {
                    if(x != 0 || z != 0) COIL_SHAPE[i].addEntry(x, 0, z, optionalHatch);
                    COIL_SHAPE[i].addEntry(x, 3, z, optionalHatch);
                    if(x != 0 || z != 1) {
                        for (int y = 1; y <= 2; ++y) {
                            COIL_SHAPE[i].addEntry(x, y, z, coil);
                        }
                    }
                }
            }
        }
    }
}
