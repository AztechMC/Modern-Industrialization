/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.MachineBlock;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.ElectricMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.SteamMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.RecipeEfficiencyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import aztech.modern_industrialization.machinesv2.models.MachineModels;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class MIMachines {
    public static void init() {
        MachineGuiParameters guiParams = new MachineGuiParameters.Builder(new LiteralText("FIXME"), "default", true).build();
        ProgressBar.Parameters progressBarParams = new ProgressBar.Parameters(78, 35, "macerator");
        RecipeEfficiencyBar.Parameters efficiencyBarParams = new RecipeEfficiencyBar.Parameters(38, 66);
        registerMachineTiers("macerator", guiParams, MIMachineRecipeTypes.MACERATOR, 1, 4, 0, 0, progressBarParams, efficiencyBarParams,
                items -> items.addSlot(56, 35).addSlots(102, 27, 2, 2),
                fluids -> {},
                true, true, false,
                TIER_BRONZE | TIER_STEEL | TIER_ELECTRIC);
    }

    private static final EnergyBar.Parameters ENERGY_BAR_PARAMS = new EnergyBar.Parameters(18, 34);

    public static void registerMachineTiers(String machine, MachineGuiParameters guiParams, MachineRecipeType type,
                                            int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount,
                                            ProgressBar.Parameters progressBarParams, RecipeEfficiencyBar.Parameters efficiencyBarParams,
                                            Consumer<SlotPositions.Builder> itemPositions, Consumer<SlotPositions.Builder> fluidPositions,
                                            boolean frontOverlay, boolean topOverlay, boolean sideOverlay,
                                            int tiers) {
        for (int i = 0; i < 2; ++i) {
            if (i == 0 && (tiers & TIER_BRONZE) == 0) {
                continue;
            }
            if (i == 1 && (tiers & TIER_STEEL) == 0) {
                continue;
            }

            SlotPositions.Builder itemPositionsBuilder = new SlotPositions.Builder();
            itemPositions.accept(itemPositionsBuilder);
            SlotPositions.Builder fluidPositionsBuilder = new SlotPositions.Builder();
            fluidPositionsBuilder.addSlot(12, 35);
            fluidPositions.accept(fluidPositionsBuilder);
            MachineTier tier = i == 0 ? MachineTier.BRONZE : MachineTier.STEEL;
            String prefix = i == 0 ? "bronze" : "steel";
            int steamBuckets = i == 0 ? 2 : 4;
            registerMachine(prefix + "_" + machine,
                    bet -> new SteamMachineBlockEntity(bet, type, buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, itemPositionsBuilder.build(), fluidPositionsBuilder.build(), steamBuckets), guiParams, progressBarParams, tier),
                    bet -> {
                        if (itemInputCount + itemOutputCount > 0) {
                            MachineBlockEntity.registerItemApi(bet);
                        }
                        MachineBlockEntity.registerFluidApi(bet);
                    });
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addMachine(prefix, machine, frontOverlay, topOverlay, sideOverlay);
            }
        }
        if ((tiers & TIER_ELECTRIC) > 0) {
            SlotPositions.Builder itemPositionsBuilder = new SlotPositions.Builder();
            itemPositions.accept(itemPositionsBuilder);
            SlotPositions.Builder fluidPositionsBuilder = new SlotPositions.Builder();
            fluidPositions.accept(fluidPositionsBuilder);
            registerMachine("lv_" + machine,
                    bet -> new ElectricMachineBlockEntity(bet, type, buildComponent(itemInputCount, itemOutputCount, fluidInputCount, fluidOutputCount, itemPositionsBuilder.build(), fluidPositionsBuilder.build(), 0), guiParams, ENERGY_BAR_PARAMS, progressBarParams, efficiencyBarParams, MachineTier.LV, 3200),
                    bet -> {
                        ElectricMachineBlockEntity.registerEnergyApi(bet);
                        if (itemInputCount + itemOutputCount > 0) {
                            MachineBlockEntity.registerItemApi(bet);
                        }
                        if (fluidInputCount + fluidOutputCount > 0) {
                            MachineBlockEntity.registerFluidApi(bet);
                        }
                    });
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addMachine("lv", machine, frontOverlay, topOverlay, sideOverlay);
            }
        }
    }

    private static final int TIER_BRONZE = 1, TIER_STEEL = 2, TIER_ELECTRIC = 4;

    public static void registerMachine(String id, Function<BlockEntityType<?>, BlockEntity> factory, Consumer<BlockEntityType<?>> extraRegistrator) {
        BlockEntityType<?>[] bet = new BlockEntityType[1];
        Supplier<BlockEntity> ctor = () -> factory.apply(bet[0]);
        Block block = new MachineBlock(id, ctor);
        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier(id), BlockEntityType.Builder.create(ctor, block).build(null));
        ModernIndustrialization.RESOURCE_PACK.addTag(new Identifier("fabric:blocks/wrenchable"), JTag.tag().add(new MIIdentifier(id)));
        extraRegistrator.accept(bet[0]);
    }

    /**
     * @param steamBuckets Number of steam buckets in the steam input slot, or 0 for no steam input slot
     */
    private static MachineInventoryComponent buildComponent(int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount,
            SlotPositions itemPositions, SlotPositions fluidPositions, int steamBuckets) {
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
        if (steamBuckets > 0) {
            fluidInputStacks.add(ConfigurableFluidStack.lockedInputSlot(81000 * steamBuckets, MIFluids.STEAM));
        }
        for (int i = 0; i < fluidInputCount; ++i) {
            fluidInputStacks.add(ConfigurableFluidStack.standardInputSlot(81000 * bucketCapacity));
        }
        List<ConfigurableFluidStack> fluidOutputStacks = new ArrayList<>();
        for (int i = 0; i < fluidOutputCount; ++i) {
            fluidOutputStacks.add(ConfigurableFluidStack.standardOutputSlot(81000 * bucketCapacity));
        }

        return new MachineInventoryComponent(itemInputStacks, itemOutputStacks, fluidInputStacks, fluidOutputStacks, itemPositions, fluidPositions);
    }

    private MIMachines() {
    }
}
