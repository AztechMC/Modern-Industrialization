package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchFlags;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machinesv2.multiblocks.SimpleMember;
import net.minecraft.block.Blocks;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;

public class MultiblockMachines {
    public static void init() {
        SimpleMember bricks = SimpleMember.forBlock(Blocks.BRICKS);
        HatchFlags steamCraftingHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).build();
        ShapeTemplate cokeOvenShape = new ShapeTemplate.Builder()
                .add3by3(0, bricks, false, steamCraftingHatches)
                .add3by3(1, bricks, true, null)
                .add3by3(2, bricks, true, null)
                .build();
        MachineRegistrationHelper.registerMachine("coke_oven", bet -> new SteamCraftingMultiblockBlockEntity(bet, "coke_oven", cokeOvenShape), bet -> {});
    }
}
