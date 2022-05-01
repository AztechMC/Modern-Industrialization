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
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MIAdvancementsProvider extends FabricAdvancementProvider {
    public MIAdvancementsProvider(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @SuppressWarnings("unused")
    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        var forgeHammer = createBasic(consumer, "forge_hammer", null, FrameType.GOAL);
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, FrameType.GOAL);
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer);
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks);
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer);
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer);
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer);
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, FrameType.GOAL);
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks);
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, FrameType.GOAL);
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing);
        var steamQuarry = createBasic(consumer, "steam_quarry", steelMachineCasing);
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing);
        var inductor = createBasic(consumer, "inductor", steelWiremill);
        var resistor = createBasic(consumer, "resistor", steelWiremill);
        var capacitor = createBasic(consumer, "capacitor", steelWiremill);
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, FrameType.GOAL);
        var lvSteamTurbine = createBasic(consumer, "lv_steam_turbine", analogCircuit);
        var polarizer = createBasic(consumer, "polarizer", lvSteamTurbine);
        var largeSteamBoiler = createBasic(consumer, "large_steam_boiler", analogCircuit);
        var assembler = createBasic(consumer, "assembler", analogCircuit, FrameType.GOAL);
        var mvLvTransformer = createBasic(consumer, "mv_lv_transformer", analogCircuit);
        var electricBlastFurnace = createBasic(consumer, "electric_blast_furnace", lvSteamTurbine, FrameType.GOAL);
        var electronicCircuit = createBasic(consumer, "electronic_circuit", electricBlastFurnace);
        var centrifuge = createBasic(consumer, "centrifuge", electronicCircuit);
        var electrolyzer = createBasic(consumer, "electrolyzer", electronicCircuit);
        var chemicalReactor = createBasic(consumer, "chemical_reactor", electronicCircuit);
        var distillery = createBasic(consumer, "distillery", electronicCircuit);
        var electricQuarry = createBasic(consumer, "electric_quarry", electronicCircuit, FrameType.CHALLENGE);
        var oilDrillingRig = createBasic(consumer, "oil_drilling_rig", electronicCircuit, FrameType.GOAL);
        var vacuumFreezer = createBasic(consumer, "vacuum_freezer", electronicCircuit, FrameType.GOAL);
        var mvSteamTurbine = createBasic(consumer, "mv_steam_turbine", electronicCircuit, FrameType.GOAL);
        var dieselGenerator = createBasic(consumer, "diesel_generator", distillery);
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", distillery, FrameType.CHALLENGE);
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", distillery, FrameType.CHALLENGE);
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", distillery, FrameType.CHALLENGE);

        var digitalCircuit = createBasic(consumer, "digital_circuit", distillery);

        var turboDieselGenerator = createBasic(consumer, "turbo_diesel_generator", dieselGenerator);
        var largeDieselGenerator = createBasic(consumer, "large_diesel_generator", turboDieselGenerator);
        var hvSteamTurbine = createBasic(consumer, "hv_steam_turbine", mvSteamTurbine);
        var largeSteamTurbine = createBasic(consumer, "large_steam_turbine", hvSteamTurbine);

        var distillationTower = createBasic(consumer, "distillation_tower", digitalCircuit, FrameType.GOAL);

        var crowbar = createBasic(consumer, "crowbar", steelMachineCasing);

        var heatExchanger = createBasic(consumer, "heat_exchanger", digitalCircuit);

        var stainlessSteel = createBasic(consumer, "stainless_steel_ingot", vacuumFreezer, FrameType.GOAL);
        var screwdriver = createBasic(consumer, "screwdriver", stainlessSteel);
        var kanthalCoil = createBasic(consumer, "kanthal_coil", screwdriver);
        var processingUnit = createBasic(consumer, "processing_unit", kanthalCoil);

        var titaniumIngot = createBasic(consumer, "titanium_ingot", kanthalCoil);
        var blastProofAlloyPlate = createBasic(consumer, "blastproof_alloy_plate", titaniumIngot);
        var implosionCompressor = createBasic(consumer, "implosion_compressor", blastProofAlloyPlate, FrameType.GOAL);
        var pressurizer = createBasic(consumer, "pressurizer", titaniumIngot);
        var rawIridium = createBasic(consumer, "raw_iridium", titaniumIngot);

        var superconductorCable = createBasic(consumer, "superconductor_cable", rawIridium, FrameType.GOAL);
        var gravichestplate = createBasic(consumer, "gravichestplate", superconductorCable, FrameType.CHALLENGE);

        var nuclearReactor = createBasic(consumer, "nuclear_reactor", implosionCompressor, FrameType.CHALLENGE);

        var nuke = createBasic(consumer, "nuke", nuclearReactor);
        var singularity = createBasic(consumer, "singularity", nuke);

        var mixedIngotIridium = createBasic(consumer, "mixed_ingot_iridium", implosionCompressor);
        var quantumCircuit = createBasic(consumer, "quantum_circuit", mixedIngotIridium, FrameType.GOAL);
        var fusionReactor = createBasic(consumer, "fusion_reactor", quantumCircuit, FrameType.CHALLENGE);
        var plasmaTurbine = createBasic(consumer, "plasma_turbine", fusionReactor);

        var basicUpgrade = createBasic(consumer, "basic_upgrade", analogCircuit, FrameType.GOAL);
        var advancedUpgrade = createBasic(consumer, "advanced_upgrade", electronicCircuit);
        var turboUpgrade = createBasic(consumer, "turbo_upgrade", digitalCircuit);
        var highlyAdvancedUpgrade = createBasic(consumer, "highly_advanced_upgrade", processingUnit);

        var quantumUpgrade = createBasic(consumer, "quantum_upgrade", quantumCircuit, FrameType.GOAL);

        var replicator = createBasic(consumer, "replicator", quantumUpgrade, FrameType.CHALLENGE);

        var uuMatter = createBasic(consumer, "bucket_uu_matter", singularity, FrameType.GOAL);

        var quantumSword = createBasic(consumer, "quantum_sword", quantumUpgrade, FrameType.CHALLENGE);
        var quantumChestplate = createBasic(consumer, "quantum_chestplate", quantumUpgrade, FrameType.CHALLENGE);

    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String item, @Nullable Advancement parent) {
        return createBasic(consumer, item, parent, FrameType.TASK);
    }

    private static Advancement createBasic(Consumer<Advancement> consumer, String itemId, @Nullable Advancement parent, FrameType frame) {
        var item = Registry.ITEM.get(new MIIdentifier(itemId));
        var titleKey = "advancements.modern_industrialization." + itemId;
        var descKey = "advancements.modern_industrialization." + itemId + ".description";

        var advancementTask = Advancement.Builder.advancement();
        ResourceLocation background = null;

        if (parent != null) {
            advancementTask.parent(parent);
        } else {
            background = new MIIdentifier("textures/blocks/fire_clay_bricks.png");
        }

        advancementTask.display(
                item,
                new TranslatableComponent(titleKey),
                new TranslatableComponent(descKey),
                background,
                frame,
                true,
                true,
                false);
        advancementTask.addCriterion("checkInv", InventoryChangeTrigger.TriggerInstance.hasItems(item));

        var advancement = advancementTask.build(new MIIdentifier(itemId));
        consumer.accept(advancement);
        return advancement;
    }
}
