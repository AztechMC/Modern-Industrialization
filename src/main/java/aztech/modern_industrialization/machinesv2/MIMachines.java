package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machinesv2.blockentities.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class MIMachines {
    public static void init() {
        BlockEntityType<?>[] bet = new BlockEntityType<?>[] { null };
        Supplier<BlockEntity> ctor = () -> new MachineBlockEntity(bet[0], null, buildComponent(1, 4, 0, 0), MachineTier.LV);
        Block block = new MachineBlock("lv_macerator", ctor);
        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("lv_macerator"), BlockEntityType.Builder.create(ctor, block).build(null));
    }
    
    private static MachineInventoryComponent buildComponent(int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount) {
        int bucketCapacity = 16;

        List<ConfigurableItemStack> itemInputStacks = new ArrayList<>();
        for (int i = 0; i < itemInputCount; ++i) {
            itemInputStacks.add(ConfigurableItemStack.standardInputSlot());
        }
        List<ConfigurableItemStack> itemOutputStacks = new ArrayList<>();
        for (int i = 0; i < itemOutputCount; ++i) {
            itemOutputStacks.add(ConfigurableItemStack.standardOutputSlot());
        }
        List<ConfigurableFluidStack> fluidInputStacks = new ArrayList<>();
        for (int i = 0; i < fluidInputCount; ++i) {
            fluidInputStacks.add(ConfigurableFluidStack.standardInputSlot(81000 * bucketCapacity));
        }
        List<ConfigurableFluidStack> fluidOutputStacks = new ArrayList<>();
        for (int i = 0; i < fluidOutputCount; ++i) {
            fluidOutputStacks.add(ConfigurableFluidStack.standardOutputSlot(81000 * bucketCapacity));
        }

        return new MachineInventoryComponent(itemInputStacks, itemOutputStacks, fluidInputStacks, fluidOutputStacks);
    }

    private MIMachines() {
    }
}
