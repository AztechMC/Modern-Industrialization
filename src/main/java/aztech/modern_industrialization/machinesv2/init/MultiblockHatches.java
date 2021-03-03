package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.blockentities.hatches.ItemHatch;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasingModel;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MultiblockHatches {
    public static void init() {
        registerItemHatches("bronze", MachineCasings.BRONZE, 1, 1, 80, 40);
        registerItemHatches("steel", MachineCasings.STEEL, 2, 1, 80, 30);
        registerItemHatches("advanced", MachineCasings.MV, 2, 2, 80, 21);
        registerItemHatches("turbo", MachineCasings.HV, 3, 3, 71, 21);
    }

    private static void registerItemHatches(String prefix, MachineCasingModel casing, int rows, int columns, int xStart, int yStart) {
        for (int iter = 0; iter < 2; ++iter) {
            boolean input = iter == 0;
            String machine = prefix + "_item_" + (input ? "input" : "output") + "_hatch";
            List<ConfigurableItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < rows*columns; ++i) {
                if (input) {
                    itemStacks.add(ConfigurableItemStack.standardInputSlot());
                } else {
                    itemStacks.add(ConfigurableItemStack.standardOutputSlot());
                }
            }
            MIInventory inventory = new MIInventory(itemStacks, Collections.emptyList(),
                    new SlotPositions.Builder().addSlots(xStart, yStart, rows, columns).build(), SlotPositions.empty());
            MachineRegistrationHelper.registerMachine(machine, bet -> new ItemHatch(bet, new MachineGuiParameters.Builder(machine, true).build(), casing, input, inventory));
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(machine, "", casing, false, false, false);
            }
        }
    }
}
