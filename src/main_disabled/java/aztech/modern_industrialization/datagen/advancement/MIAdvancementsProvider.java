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
import aztech.modern_industrialization.datagen.translation.TranslationProvider;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MIAdvancementsProvider extends FabricAdvancementProvider {
    private final TranslationProvider translations;

    public MIAdvancementsProvider(FabricDataOutput packOutput, TranslationProvider translations) {
        super(packOutput);
        this.translations = translations;
    }

    @SuppressWarnings("unused")
    @Override
    // @formatter:off
    public void generateAdvancement(Consumer<Advancement> consumer) {
        var guidebook = createBasic(consumer, "guidebook", null, "Modern Industrialization", "Obtain the Modern Industrialization guidebook.");
        var forgeHammer = createBasic(consumer, "forge_hammer", guidebook, FrameType.GOAL, "Is This A Forge Mod?", "Craft a Forge Hammer and start exploring the mod.");
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, FrameType.GOAL, "Getting That 3x3 Going", "Craft a Steam Mining Drill");
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer, "Almost Steam?", "Craft Fire Clay Bricks");
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks, "Ten Times More Fuel Efficient", "Craft a Bronze Furnace");
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer, "An Automatic Forge Mod", "Craft a Bronze Compressor");
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer, "Mixing Without Mixins", "Craft a Bronze Mixer");
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer, "Ore Hemiquadrupling", "Craft a Bronze Macerator");
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, FrameType.GOAL, "Coke-A Cola", "Craft a Coke Oven to start Steel Production");
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks, "Almost Steel!", "Craft a Steam Blast Furnace");
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, FrameType.GOAL, "Cooked Uncooked Steel!", "Use a Steam Blast Furnace to produce Steel and craft a Steel Machine Casing");
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing, "Neither a Wire nor a Mill", "Craft a Steel Wiremill");
        var steamQuarry = createBasic(consumer, "steam_quarry", steelMachineCasing, "From Minecraft to Craft", "Craft a Steam Quarry and say goodbye to mining");
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing, "To Pack Or Not To Pack", "Craft a Steel Packer");
        var inductor = createBasic(consumer, "inductor", steelWiremill, "The L in RLC", "Craft an Inductor");
        var resistor = createBasic(consumer, "resistor", steelWiremill, "The R in RLC", "Craft a Resistor");
        var capacitor = createBasic(consumer, "capacitor", steelWiremill, "The C in RLC", "Craft a Capacitor");
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, FrameType.GOAL, "RLC Circuits", "Craft an Analog Circuit and start the Electric Age");
        var lvSteamTurbine = createBasic(consumer, "lv_steam_turbine", analogCircuit, "Better Than Solar Panels", "Craft a Steam Turbine");
        var polarizer = createBasic(consumer, "polarizer", lvSteamTurbine, "One Recipe (+2) To Rule Them All", "Craft a Polarizer");
        var largeSteamBoiler = createBasic(consumer, "large_steam_boiler", analogCircuit, "Kiss Your Fuel Goodbye!", "Craft a Large Steam Boiler");
        var assembler = createBasic(consumer, "assembler", analogCircuit, FrameType.GOAL, "Avengers, Assemble!", "Craft an Assembler");
        var mvLvTransformer = createBasic(consumer, "mv_lv_transformer", analogCircuit, "Optimus Prime!", "Craft an MV to LV Transformer");
        var electricBlastFurnace = createBasic(consumer, "electric_blast_furnace", lvSteamTurbine, FrameType.GOAL, "Electric Best Friend", "Craft an Electric Blast Furnace to start producing Aluminum");
        var electronicCircuit = createBasic(consumer, "electronic_circuit", electricBlastFurnace, "The Power of Silicon", "Craft a Electronic Circuit");
        var centrifuge = createBasic(consumer, "centrifuge", electronicCircuit, "Actually It's The Centripetal Force", "Craft a Centrifuge");
        var electrolyzer = createBasic(consumer, "electrolyzer", electronicCircuit, "It's Got What Plants Crave", "Craft an Electrolyzer");
        var chemicalReactor = createBasic(consumer, "chemical_reactor", electronicCircuit, "Walter White Approves", "Craft a Chemical Reactor");
        var distillery = createBasic(consumer, "distillery", electronicCircuit, "Al Capone Approves", "Craft a Distillery");
        var electricQuarry = createBasic(consumer, "electric_quarry", electronicCircuit, FrameType.CHALLENGE, "Resource Goes BRRRRRR!!!", "Craft an Electric Quarry");
        var oilDrillingRig = createBasic(consumer, "oil_drilling_rig", electronicCircuit, FrameType.GOAL, "Bringing Freedom To Your Country", "Craft an Oil Drilling Rig");
        var vacuumFreezer = createBasic(consumer, "vacuum_freezer", electronicCircuit, FrameType.GOAL, "Enslaved Winter", "Craft a Vacuum Freezer");
        var mvSteamTurbine = createBasic(consumer, "mv_steam_turbine", electronicCircuit, FrameType.GOAL, "Better Than Wind Mills", "Craft an Advanced Steam Turbine");
        var dieselGenerator = createBasic(consumer, "mv_diesel_generator", distillery, "Fast and Furious", "Craft an MV Diesel Generator");
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", distillery, FrameType.CHALLENGE, "Ely... We Meant Jetpack!", "Craft a Diesel Jetpack");
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", distillery, FrameType.CHALLENGE, "The Texas Chain Saw Massacre", "Craft a Diesel Chainsaw");
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", distillery, FrameType.CHALLENGE, "Through The Walls Of Ba Sing Se", "Craft a Diesel Mining Drill");
        var digitalCircuit = createBasic(consumer, "digital_circuit", distillery, "No need for Sodium anymore", "Craft a Digital Circuit");
        var turboDieselGenerator = createBasic(consumer, "hv_diesel_generator", dieselGenerator, "Fast and Furious 2 : Revenge", "Craft an HV Diesel Generator");
        var largeDieselGenerator = createBasic(consumer, "large_diesel_generator", turboDieselGenerator, "Fast and Furious 42 : Armageddon", "Craft a Large Diesel Generator");
        var hvSteamTurbine = createBasic(consumer, "hv_steam_turbine", mvSteamTurbine, "Better than Water Wheel", "Craft an HV Steam Turbine");
        var largeSteamTurbine = createBasic(consumer, "large_steam_turbine", hvSteamTurbine, "Enslaved Hurricane", "Craft a Large Steam Turbine");
        var distillationTower = createBasic(consumer, "distillation_tower", digitalCircuit, FrameType.GOAL, "TOTAL™ Distillation", "Craft a Distillation Tower to unlock to full potential of oil processing");
        var heatExchanger = createBasic(consumer, "heat_exchanger", digitalCircuit, "Lava Power but Balanced",  "Craft a Heat Exchanger to avoid losing high pressure (and cheese easy energy from lava production)");
        var stainlessSteel = createBasic(consumer, "stainless_steel_ingot", vacuumFreezer, FrameType.GOAL, "Invar with Slot Locking", "Craft a Stainless Steel Ingot");
        var kanthalCoil = createBasic(consumer, "kanthal_coil", stainlessSteel, "Electric Better Furnace", "Craft a Kanthal Coil to unlock new EBF recipes");
        var processingUnit = createBasic(consumer, "processing_unit", kanthalCoil, "Bitcoin Miner", "Craft a Processing Unit");
        var titaniumIngot = createBasic(consumer, "titanium_ingot", kanthalCoil, "Steel but Pink", "Craft a Titanium Ingot");
        var blastProofAlloyPlate = createBasic(consumer, "blastproof_alloy_plate", titaniumIngot, "Creeper Nightmare", "Craft a Blastproof Alloy Plate in the compressor");
        var implosionCompressor = createBasic(consumer, "implosion_compressor", blastProofAlloyPlate, FrameType.GOAL, "Automated Creeper", "Craft an Implosion Compressor");
        var pressurizer = createBasic(consumer, "pressurizer", titaniumIngot, "Under Pressure", "Craft a Pressurizer to unlock for efficient Steam Process");
        var rawIridium = createBasic(consumer, "raw_iridium", titaniumIngot, "Diamond 2.0 : Electric Boogaloo", "Obtain a Raw Piece of Iridium");
        var superconductorCable = createBasic(consumer, "superconductor_cable", rawIridium, FrameType.GOAL, "Unlimited Power (Transfer)", "Craft a Superconductor Cable to transfer unlimited amount of energy");
        var gravichestplate = createBasic(consumer, "gravichestplate", superconductorCable, FrameType.CHALLENGE, "Gravichestplate™", "Craft a Gravichestplate to unlock creative flight");
        var nuclearReactor = createBasic(consumer, "nuclear_reactor", implosionCompressor, FrameType.CHALLENGE, "3.6 Roentgen", "Craft a Nuclear Reactor and discover its overengineered mechanisms");
        var nuke = createBasic(consumer, "nuke", nuclearReactor, "I've become Death, Destroyer of Worlds", "Craft a Nuke");
        var singularity = createBasic(consumer, "singularity", nuke, "Pocket Black Hole", "Craft a Singularity");
        var mixedIngotIridium = createBasic(consumer, "mixed_ingot_iridium", implosionCompressor, "Oreo Ingot", "Craft a Mixed Ingot Iridium to craft Iridium Plates");
        var quantumCircuit = createBasic(consumer, "quantum_circuit", mixedIngotIridium, FrameType.GOAL, "Mobius Strip Eigenvalues", "Craft a Quantum Circuit");
        var fusionReactor = createBasic(consumer, "fusion_reactor", quantumCircuit, FrameType.CHALLENGE, "Enslaved Star Core", "Craft a Fusion Reactor to produce insane amount of energy");
        var plasmaTurbine = createBasic(consumer, "plasma_turbine", fusionReactor, "Better than Nuclear Fission", "Craft a Plasma Turbine to transform Helium Plasma into energy");
        var basicUpgrade = createBasic(consumer, "basic_upgrade", chemicalReactor, FrameType.GOAL, "Machine Speedup", "Craft a Basic Upgrade to increase the maximum speed of an electric recipe");
        var advancedUpgrade = createBasic(consumer, "advanced_upgrade", electronicCircuit, "Machine Speedup-Speedup", "Craft a Advanced Upgrade");
        var turboUpgrade = createBasic(consumer, "turbo_upgrade", digitalCircuit, "Gotta go Fast", "Craft a Turbo Upgrade");
        var highlyAdvancedUpgrade = createBasic(consumer, "highly_advanced_upgrade", processingUnit, "I'm speed", "Craft an Highly Advanced Upgrade");
        var quantumUpgrade = createBasic(consumer, "quantum_upgrade", quantumCircuit, FrameType.GOAL, "Time Dilation", "Craft a Quantum Upgrade to unlock unlimited recipe speed");
        var replicator = createBasic(consumer, "replicator", quantumUpgrade, FrameType.CHALLENGE, "Legal Duping", "Craft a Replicator and replicate any item you want using UU Matter");
        var uuMatter = createBasic(consumer, "uu_matter_bucket", singularity, FrameType.GOAL, "Liquid Creative Mode", "Produce a bucket of UU Matter to start duplication in the replicator");
        var quantumSword = createBasic(consumer, "quantum_sword", quantumUpgrade, FrameType.CHALLENGE, "Annihilation Operator", "Craft a Quantum Sword and disintegrate your foes (or the Wandering Trader llama's)");
        var quantumChestplate = createBasic(consumer, "quantum_chestplate", quantumUpgrade, FrameType.CHALLENGE, "Quantum Immortality™", "Craft a Quantum Chestplate to reduce the probability of taking any damage by 25% for each piece of the Quantum Armor Set");
    }

    private Advancement createBasic(Consumer<Advancement> consumer, String item, @Nullable Advancement parent, String titleEnglishName, String englishDescription) {
        return createBasic(consumer, item, parent, FrameType.TASK, titleEnglishName, englishDescription);
    }

    private Advancement createBasic(Consumer<Advancement> consumer, String itemId, @Nullable Advancement parent, FrameType frame,
                                           String titleEnglishName, String englishDescription) {
        var item = BuiltInRegistries.ITEM.get(new MIIdentifier(itemId));
        var titleKey = "advancements.modern_industrialization." + itemId;
        var descKey = "advancements.modern_industrialization." + itemId + ".description";

        var advancementTask = Advancement.Builder.advancement();
        ResourceLocation background = null;

        if (parent != null) {
            advancementTask.parent(parent);
        } else {
            background = new MIIdentifier("textures/block/fire_clay_bricks.png");
        }

        advancementTask.display(
                item,
                Component.translatable(titleKey),
                Component.translatable(descKey),
                background,
                frame,
                true,
                true,
                false);
        advancementTask.addCriterion("checkInv", InventoryChangeTrigger.TriggerInstance.hasItems(item));

        var advancement = advancementTask.build(new MIIdentifier(itemId));
        consumer.accept(advancement);

        translations.addTranslation(titleKey, titleEnglishName);
        translations.addTranslation(descKey, englishDescription);

        return advancement;
    }
}
