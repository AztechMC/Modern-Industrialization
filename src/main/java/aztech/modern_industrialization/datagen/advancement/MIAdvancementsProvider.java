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
package aztech.modern_industrialization.datagen.advancement;

import aztech.modern_industrialization.MIIdentifier;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementsProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class MIAdvancementsProvider extends FabricAdvancementsProvider {
    public MIAdvancementsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @SuppressWarnings("unused")
    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        var forgeHammer = createBasic(consumer, "forge_hammer", null, AdvancementFrame.GOAL);
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, AdvancementFrame.GOAL);
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer);
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks);
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer);
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer);
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer);
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, AdvancementFrame.GOAL);
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks);
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, AdvancementFrame.GOAL);
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing);
        var steamQuarry = createBasic(consumer, "steam_quarry", steelMachineCasing);
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing);
        var inductor = createBasic(consumer, "inductor", steelWiremill);
        var resistor = createBasic(consumer, "resistor", steelWiremill);
        var capacitor = createBasic(consumer, "capacitor", steelWiremill);
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, AdvancementFrame.GOAL);
        var lvSteamTurbine = createBasic(consumer, "lv_steam_turbine", analogCircuit);
        var polarizer = createBasic(consumer, "polarizer", lvSteamTurbine);
        var largeSteamBoiler = createBasic(consumer, "large_steam_boiler", analogCircuit);
        var assembler = createBasic(consumer, "assembler", analogCircuit, AdvancementFrame.GOAL);
        var mvLvTransformer = createBasic(consumer, "mv_lv_transformer", analogCircuit);
        var electricBlastFurnace = createBasic(consumer, "electric_blast_furnace", lvSteamTurbine, AdvancementFrame.GOAL);
        var electronicCircuit = createBasic(consumer, "electronic_circuit", electricBlastFurnace);
        var centrifuge = createBasic(consumer, "centrifuge", electronicCircuit);
        var electrolyzer = createBasic(consumer, "electrolyzer", electronicCircuit);
        var chemicalReactor = createBasic(consumer, "chemical_reactor", electronicCircuit);
        var distillery = createBasic(consumer, "distillery", electronicCircuit);
        var electricQuarry = createBasic(consumer, "electric_quarry", electronicCircuit, AdvancementFrame.CHALLENGE);
        var oilDrillingRig = createBasic(consumer, "oil_drilling_rig", electronicCircuit, AdvancementFrame.GOAL);
        var vacuumFreezer = createBasic(consumer, "vacuum_freezer", electronicCircuit, AdvancementFrame.GOAL);
        var mvSteamTurbine = createBasic(consumer, "mv_steam_turbine", electronicCircuit, AdvancementFrame.GOAL);
        var dieselGenerator = createBasic(consumer, "diesel_generator", distillery);
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", distillery, AdvancementFrame.CHALLENGE);
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", distillery, AdvancementFrame.CHALLENGE);
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", distillery, AdvancementFrame.CHALLENGE);

        var digitalCircuit = createBasic(consumer, "digital_circuit", distillery);

        var turboDieselGenerator = createBasic(consumer, "turbo_diesel_generator", dieselGenerator);
        var largeDieselGenerator = createBasic(consumer, "large_diesel_generator", turboDieselGenerator);
        var hvSteamTurbine = createBasic(consumer, "hv_steam_turbine", mvSteamTurbine);
        var largeSteamTurbine = createBasic(consumer, "large_steam_turbine", hvSteamTurbine);

        var distillationTower = createBasic(consumer, "distillation_tower", digitalCircuit, AdvancementFrame.GOAL);

        var crowbar = createBasic(consumer, "crowbar", steelMachineCasing);

        var heatExchanger = createBasic(consumer, "heat_exchanger", digitalCircuit);

        var stainlessSteel = createBasic(consumer, "stainless_steel_ingot", vacuumFreezer, AdvancementFrame.GOAL);
        var screwdriver = createBasic(consumer, "screwdriver", stainlessSteel);
        var kanthalCoil = createBasic(consumer, "kanthal_coil", screwdriver);
        var processingUnit = createBasic(consumer, "processing_unit", kanthalCoil);

        var titaniumIngot = createBasic(consumer, "titanium_ingot", kanthalCoil);
        var blastProofAlloyPlate = createBasic(consumer, "blastproof_alloy_plate", titaniumIngot);
        var implosionCompressor = createBasic(consumer, "implosion_compressor", blastProofAlloyPlate, AdvancementFrame.GOAL);
        var pressurizer = createBasic(consumer, "pressurizer", titaniumIngot);
        var rawIridium = createBasic(consumer, "raw_iridium", titaniumIngot);

        var superconductorCable = createBasic(consumer, "superconductor_cable", rawIridium, AdvancementFrame.GOAL);
        var gravichestplate = createBasic(consumer, "gravichestplate", superconductorCable, AdvancementFrame.CHALLENGE);

        var nuclearReactor = createBasic(consumer, "nuclear_reactor", implosionCompressor, AdvancementFrame.CHALLENGE);

        var nuke = createBasic(consumer, "nuke", nuclearReactor);
        var singularity = createBasic(consumer, "singularity", nuke);

        var mixedIngotIridium = createBasic(consumer, "mixed_ingot_iridium", implosionCompressor);
        var quantumCircuit = createBasic(consumer, "quantum_circuit", mixedIngotIridium, AdvancementFrame.GOAL);
        var fusionReactor = createBasic(consumer, "fusion_reactor", quantumCircuit, AdvancementFrame.CHALLENGE);
        var plasmaTurbine = createBasic(consumer, "plasma_turbine", fusionReactor);

        var basicUpgrade = createBasic(consumer, "basic_upgrade", analogCircuit, AdvancementFrame.GOAL);
        var advancedUpgrade = createBasic(consumer, "advanced_upgrade", electronicCircuit);
        var turboUpgrade = createBasic(consumer, "turbo_upgrade", digitalCircuit);
        var highlyAdvancedUpgrade = createBasic(consumer, "highly_advanced_upgrade", processingUnit);

        var quantumUpgrade = createBasic(consumer, "quantum_upgrade", quantumCircuit, AdvancementFrame.GOAL);

        var replicator = createBasic(consumer, "replicator", quantumUpgrade, AdvancementFrame.CHALLENGE);

        var uuMatter = createBasic(consumer, "bucket_uu_matter", singularity, AdvancementFrame.GOAL);

        var quantumSword = createBasic(consumer, "quantum_sword", quantumUpgrade, AdvancementFrame.CHALLENGE);
        var quantumChestplate = createBasic(consumer, "quantum_chestplate", quantumUpgrade, AdvancementFrame.CHALLENGE);

    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String item, @Nullable Advancement parent) {
        return createBasic(consumer, item, parent, AdvancementFrame.TASK);
    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String itemId, @Nullable Advancement parent, AdvancementFrame frame) {
        var item = Registry.ITEM.get(new MIIdentifier(itemId));
        var titleKey = "advancements.modern_industrialization." + itemId;
        var descKey = "advancements.modern_industrialization." + itemId + ".description";

        var advancementTask = Advancement.Task.create();
        Identifier background = null;

        if (parent != null) {
            advancementTask.parent(parent);
        } else {
            background = new MIIdentifier("textures/blocks/fire_clay_bricks.png");
        }

        advancementTask.display(
                item,
                new TranslatableText(titleKey),
                new TranslatableText(descKey),
                background,
                frame,
                true,
                true,
                false);
        advancementTask.criterion("checkInv", InventoryChangedCriterion.Conditions.items(item));

        var advancement = advancementTask.build(new MIIdentifier(itemId));
        consumer.accept(advancement);
        return advancement;
    }
}
