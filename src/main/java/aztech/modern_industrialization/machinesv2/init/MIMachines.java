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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machinesv2.MachineBlock;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.ElectricMachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.EnergyBar;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.components.sync.RecipeEfficiencyBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class MIMachines {
    public static void init() {
        BlockEntityType<?>[] bet = new BlockEntityType<?>[] { null };
        SlotPositions itemPositions = new SlotPositions.Builder().addSlot(56, 35).addSlots(102, 27, 2, 2).build();
        SlotPositions fluidPositions = SlotPositions.empty();
        MachineGuiParameters guiParams = new MachineGuiParameters.Builder(new LiteralText("FIXME"),
                new MIIdentifier("textures/gui/container/default.png"), true).build();
        EnergyBar.Parameters energyBarParams = new EnergyBar.Parameters(18, 34);
        ProgressBar.Parameters progressBarParams = new ProgressBar.Parameters(78, 35, "macerator");
        RecipeEfficiencyBar.Parameters efficiencyBarParams = new RecipeEfficiencyBar.Parameters(38, 66);
        Supplier<BlockEntity> ctor = () -> new ElectricMachineBlockEntity(bet[0], MIMachineRecipeTypes.MACERATOR,
                buildComponent(1, 4, 0, 0, itemPositions, fluidPositions), guiParams, energyBarParams, progressBarParams, efficiencyBarParams,
                MachineTier.LV, 3200);
        Block block = new MachineBlock("lv_macerator", ctor);
        bet[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("lv_macerator"),
                BlockEntityType.Builder.create(ctor, block).build(null));
        ElectricMachineBlockEntity.registerEnergyApi(bet[0]);
        MachineBlockEntity.registerItemApi(bet[0]);
        ModernIndustrialization.RESOURCE_PACK.addTag(new Identifier("fabric:wrenchables"), JTag.tag().add(new MIIdentifier("lv_macerator")));
    }

    private static MachineInventoryComponent buildComponent(int itemInputCount, int itemOutputCount, int fluidInputCount, int fluidOutputCount,
            SlotPositions itemPositions, SlotPositions fluidPositions) {
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

        return new MachineInventoryComponent(itemInputStacks, itemOutputStacks, fluidInputStacks, fluidOutputStacks, itemPositions, fluidPositions);
    }

    private MIMachines() {
    }
}
