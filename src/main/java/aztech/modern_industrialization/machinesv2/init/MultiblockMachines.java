package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.machinesv2.blockentities.multiblocks.SteamCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchFlags;
import aztech.modern_industrialization.machinesv2.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machinesv2.multiblocks.SimpleMember;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.*;

@SuppressWarnings("rawtypes")
public class MultiblockMachines {
    public static BlockEntityType COKE_OVEN;

    public static void init() {
        SimpleMember bricks = SimpleMember.forBlock(Blocks.BRICKS);
        HatchFlags steamCraftingHatches = new HatchFlags.Builder().with(ITEM_INPUT).with(ITEM_OUTPUT).with(FLUID_INPUT).build();
        ShapeTemplate cokeOvenShape = new ShapeTemplate.Builder(MachineCasings.BRICKS)
                .add3by3(-1, bricks, false, steamCraftingHatches)
                .add3by3(0, bricks, true, null)
                .add3by3(1, bricks, true, null)
                .build();
        COKE_OVEN = MachineRegistrationHelper.registerMachine("coke_oven", bet -> new SteamCraftingMultiblockBlockEntity(bet, "coke_oven", cokeOvenShape));
    }

    @SuppressWarnings("unchecked")
    public static void clientInit() {
        MachineModels.addTieredMachine("coke_oven", "coke_oven", MachineCasings.BRICKS, true, false, false);
        BlockEntityRendererRegistry.INSTANCE.register(COKE_OVEN, MultiblockMachineBER::new);
    }
}
