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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIAdvancementTriggers;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.datagen.translation.TranslationProvider;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public record MIAdvancementsProvider(TranslationProvider translations) implements AdvancementProvider.AdvancementGenerator {

    private static Criterion<?> createSimpleCriterion(Supplier<PlayerTrigger> trigger) {
        return trigger.get().createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
    }

    private Component translateTitle(String path, String englishName) {
        var titleKey = "advancements.modern_industrialization." + path;
        translations.addTranslation(titleKey, englishName);
        return Component.translatable(titleKey);
    }

    private Component translateDescription(String path, String englishDescription) {
        var descKey = "advancements.modern_industrialization." + path + ".description";
        translations.addTranslation(descKey, englishDescription);
        return Component.translatable(descKey);
    }

    private class Builder extends Advancement.Builder {
        private final String path;

        private Builder(String path) {
            this.path = path;
        }

        @Override
        public Builder sendsTelemetryEvent() {
            return (Builder) super.sendsTelemetryEvent();
        }

        @Override
        public Builder display(ItemLike pIcon, Component pTitle, Component pDescription, @Nullable ResourceLocation pBackground,
                AdvancementType pType, boolean pShowToast, boolean pAnnounceChat, boolean pHidden) {
            return (Builder) super.display(pIcon, pTitle, pDescription, pBackground, pType, pShowToast, pAnnounceChat, pHidden);
        }

        public Builder display(
                ItemLike icon,
                String titleEnglishName,
                String englishDescription,
                @Nullable ResourceLocation pBackground,
                AdvancementType pType,
                boolean pShowToast,
                boolean pAnnounceChat,
                boolean pHidden) {
            return this.display(
                    icon,
                    translateTitle(path, titleEnglishName),
                    translateDescription(path, englishDescription),
                    pBackground,
                    pType,
                    pShowToast,
                    pAnnounceChat,
                    pHidden);
        }

        @Override
        public Builder parent(AdvancementHolder pParent) {
            return (Builder) super.parent(pParent);
        }

        @Override
        public Builder addCriterion(String pKey, Criterion<?> pCriterion) {
            return (Builder) super.addCriterion(pKey, pCriterion);
        }

        public AdvancementHolder save(Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
            return save(consumer, MI.id(path), existingFileHelper);
        }
    }

    private Builder newBuilder(String path) {
        return new Builder(path).sendsTelemetryEvent();
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper) {
        var guidebook = newBuilder("guidebook")
                .display(MIItem.GUIDE_BOOK,
                        MIText.ModernIndustrialization.text(),
                        translateDescription("guidebook", "Welcome to Modern Industrialization! Make sure to check out the guidebook."),
                        MI.id("textures/block/fire_clay_bricks.png"),
                        AdvancementType.TASK,
                        false,
                        false,
                        false)
                .addCriterion("logged_in", createSimpleCriterion(MIAdvancementTriggers.PLAYER_LOGGED_IN))
                .save(consumer, existingFileHelper);

        // @formatter:off
        var forgeHammer = createBasic(consumer, "forge_hammer", guidebook, AdvancementType.GOAL, "Is This A Forge Mod?", "Craft a Forge Hammer and start exploring the mod.", existingFileHelper);
        var steamMiningDrill = createBasic(consumer, "steam_mining_drill", forgeHammer, AdvancementType.GOAL, "Getting That 3x3 Going", "Craft a Steam Mining Drill", existingFileHelper);
        var fireClayBricks = createBasic(consumer, "fire_clay_bricks", forgeHammer, "Almost Steam?", "Craft Fire Clay Bricks", existingFileHelper);
        var bronzeFurnace = createBasic(consumer, "bronze_furnace", fireClayBricks, "Ten Times More Fuel Efficient", "Craft a Bronze Furnace", existingFileHelper);
        var bronzeCompressor = createBasic(consumer, "bronze_compressor", forgeHammer, "An Automatic Forge Mod", "Craft a Bronze Compressor", existingFileHelper);
        var bronzeMixer = createBasic(consumer, "bronze_mixer", forgeHammer, "Mixing Without Mixins", "Craft a Bronze Mixer", existingFileHelper);
        var bronzeMacerator = createBasic(consumer, "bronze_macerator", forgeHammer, "Ore Hemiquadrupling", "Craft a Bronze Macerator", existingFileHelper);
        var cokeOven = createBasic(consumer, "coke_oven", forgeHammer, AdvancementType.GOAL, "Coke-A Cola", "Craft a Coke Oven to start Steel Production", existingFileHelper);
        var steamBlastFurnace = createBasic(consumer, "steam_blast_furnace", fireClayBricks, "Almost Steel!", "Craft a Steam Blast Furnace", existingFileHelper);
        var steelMachineCasing = createBasic(consumer, "steel_machine_casing", steamBlastFurnace, AdvancementType.GOAL, "Cooked Uncooked Steel!", "Use a Steam Blast Furnace to produce Steel and craft a Steel Machine Casing", existingFileHelper);
        // @formatter:on

        var usedSteelUpgrade = newBuilder("used_steel_upgrade")
                .display(MIItem.STEEL_UPGRADE,
                        "Upgrade Like a Boss",
                        "Right-click a Steel Upgrade on a bronze machine.",
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false)
                .parent(steelMachineCasing)
                .addCriterion("used_steel_upgrade", createSimpleCriterion(MIAdvancementTriggers.USED_STEEL_UPGRADE))
                .save(consumer, existingFileHelper);

        // @formatter:off
        var steelWiremill = createBasic(consumer, "steel_wiremill", steelMachineCasing, "Neither a Wire nor a Mill", "Craft a Steel Wiremill", existingFileHelper);
        var steamQuarry = createBasic(consumer, "steam_quarry", steelMachineCasing, AdvancementType.GOAL, "From Minecraft to Craft", "Craft a Steam Quarry and say goodbye to mining", existingFileHelper);
        var steelPacket = createBasic(consumer, "steel_packer", steelMachineCasing, "To Pack Or Not To Pack", "Craft a Steel Packer", existingFileHelper);
        var inductor = createBasic(consumer, "inductor", steelWiremill, "The L in RLC", "Craft an Inductor", existingFileHelper);
        var resistor = createBasic(consumer, "resistor", steelWiremill, "The R in RLC", "Craft a Resistor", existingFileHelper);
        var capacitor = createBasic(consumer, "capacitor", steelWiremill, "The C in RLC", "Craft a Capacitor", existingFileHelper);
        var analogCircuit = createBasic(consumer, "analog_circuit", resistor, AdvancementType.GOAL, "RLC Circuits", "Craft an Analog Circuit and start the Electric Age", existingFileHelper);
        var lvSteamTurbine = createBasic(consumer, "lv_steam_turbine", analogCircuit, "Better Than Solar Panels", "Craft a Steam Turbine", existingFileHelper);
        var polarizer = createBasic(consumer, "polarizer", lvSteamTurbine, "One Recipe (+2) To Rule Them All", "Craft a Polarizer", existingFileHelper);
        var largeSteamBoiler = createBasic(consumer, "large_steam_boiler", analogCircuit, "Kiss Your Fuel Goodbye!", "Craft a Large Steam Boiler", existingFileHelper);
        var assembler = createBasic(consumer, "assembler", analogCircuit, AdvancementType.GOAL, "Avengers, Assemble!", "Craft an Assembler", existingFileHelper);
        var mvLvTransformer = createBasic(consumer, "mv_lv_transformer", analogCircuit, "Optimus Prime!", "Craft an MV to LV Transformer", existingFileHelper);
        var electricBlastFurnace = createBasic(consumer, "electric_blast_furnace", lvSteamTurbine, AdvancementType.GOAL, "Electric Best Friend", "Craft an Electric Blast Furnace to start producing Aluminum", existingFileHelper);
        var electronicCircuit = createBasic(consumer, "electronic_circuit", electricBlastFurnace, "The Power of Silicon", "Craft a Electronic Circuit", existingFileHelper);
        var centrifuge = createBasic(consumer, "centrifuge", electronicCircuit, "Actually It's The Centripetal Force", "Craft a Centrifuge", existingFileHelper);
        var electrolyzer = createBasic(consumer, "electrolyzer", electronicCircuit, "It's Got What Plants Crave", "Craft an Electrolyzer", existingFileHelper);
        var chemicalReactor = createBasic(consumer, "chemical_reactor", electronicCircuit, "Walter White Approves", "Craft a Chemical Reactor", existingFileHelper);
        var distillery = createBasic(consumer, "distillery", electronicCircuit, "Al Capone Approves", "Craft a Distillery", existingFileHelper);
        var electricQuarry = createBasic(consumer, "electric_quarry", electronicCircuit, AdvancementType.CHALLENGE, "Resource Goes BRRRRRR!!!", "Craft an Electric Quarry", existingFileHelper);
        var oilDrillingRig = createBasic(consumer, "oil_drilling_rig", electronicCircuit, AdvancementType.GOAL, "Bringing Freedom To Your Country", "Craft an Oil Drilling Rig", existingFileHelper);
        var vacuumFreezer = createBasic(consumer, "vacuum_freezer", electronicCircuit, AdvancementType.GOAL, "Enslaved Winter", "Craft a Vacuum Freezer", existingFileHelper);
        var mvSteamTurbine = createBasic(consumer, "mv_steam_turbine", electronicCircuit, AdvancementType.GOAL, "Better Than Wind Mills", "Craft an Advanced Steam Turbine", existingFileHelper);
        var dieselGenerator = createBasic(consumer, "mv_diesel_generator", distillery, "Fast and Furious", "Craft an MV Diesel Generator", existingFileHelper);
        var dieselJetpack = createBasic(consumer, "diesel_jetpack", distillery, AdvancementType.CHALLENGE, "Ely... We Meant Jetpack!", "Craft a Diesel Jetpack", existingFileHelper);
        var dieselChainsaw = createBasic(consumer, "diesel_chainsaw", distillery, AdvancementType.CHALLENGE, "The Texas Chain Saw Massacre", "Craft a Diesel Chainsaw", existingFileHelper);
        var dieselMiningDrill = createBasic(consumer, "diesel_mining_drill", distillery, AdvancementType.CHALLENGE, "Through The Walls Of Ba Sing Se", "Craft a Diesel Mining Drill", existingFileHelper);
        var digitalCircuit = createBasic(consumer, "digital_circuit", distillery, "No need for Sodium anymore", "Craft a Digital Circuit", existingFileHelper);
        var turboDieselGenerator = createBasic(consumer, "hv_diesel_generator", dieselGenerator, "Fast and Furious 2 : Revenge", "Craft an HV Diesel Generator", existingFileHelper);
        var largeDieselGenerator = createBasic(consumer, "large_diesel_generator", turboDieselGenerator, "Fast and Furious 42 : Armageddon", "Craft a Large Diesel Generator", existingFileHelper);
        var hvSteamTurbine = createBasic(consumer, "hv_steam_turbine", mvSteamTurbine, "Better than Water Wheel", "Craft an HV Steam Turbine", existingFileHelper);
        var largeSteamTurbine = createBasic(consumer, "large_steam_turbine", hvSteamTurbine, "Enslaved Hurricane", "Craft a Large Steam Turbine", existingFileHelper);
        var distillationTower = createBasic(consumer, "distillation_tower", digitalCircuit, AdvancementType.GOAL, "TOTAL™ Distillation", "Craft a Distillation Tower to unlock to full potential of oil processing", existingFileHelper);
        var heatExchanger = createBasic(consumer, "heat_exchanger", digitalCircuit, "Lava Power but Balanced",  "Craft a Heat Exchanger to avoid losing high pressure (and cheese easy energy from lava production)", existingFileHelper);
        var stainlessSteel = createBasic(consumer, "stainless_steel_ingot", vacuumFreezer, AdvancementType.GOAL, "Invar with Slot Locking", "Craft a Stainless Steel Ingot", existingFileHelper);
        var kanthalCoil = createBasic(consumer, "kanthal_coil", stainlessSteel, "Electric Better Furnace", "Craft a Kanthal Coil to unlock new EBF recipes", existingFileHelper);
        var processingUnit = createBasic(consumer, "processing_unit", kanthalCoil, "Bitcoin Miner", "Craft a Processing Unit", existingFileHelper);
        var titaniumIngot = createBasic(consumer, "titanium_ingot", kanthalCoil, "Steel but Pink", "Craft a Titanium Ingot", existingFileHelper);
        var blastProofAlloyPlate = createBasic(consumer, "blastproof_alloy_plate", titaniumIngot, "Creeper Nightmare", "Craft a Blastproof Alloy Plate in the compressor", existingFileHelper);
        var implosionCompressor = createBasic(consumer, "implosion_compressor", blastProofAlloyPlate, AdvancementType.GOAL, "Automated Creeper", "Craft an Implosion Compressor", existingFileHelper);
        var pressurizer = createBasic(consumer, "pressurizer", titaniumIngot, "Under Pressure", "Craft a Pressurizer to unlock for efficient Steam Process", existingFileHelper);
        var rawIridium = createBasic(consumer, "raw_iridium", titaniumIngot, "Diamond 2.0 : Electric Boogaloo", "Obtain a Raw Piece of Iridium", existingFileHelper);
        var superconductorCable = createBasic(consumer, "superconductor_cable", rawIridium, AdvancementType.GOAL, "Unlimited Power (Transfer)", "Craft a Superconductor Cable to transfer unlimited amount of energy", existingFileHelper);
        var gravichestplate = createBasic(consumer, "gravichestplate", superconductorCable, AdvancementType.CHALLENGE, "Gravichestplate™", "Craft a Gravichestplate to unlock creative flight", existingFileHelper);
        var nuclearReactor = createBasic(consumer, "nuclear_reactor", implosionCompressor, AdvancementType.CHALLENGE, "3.6 Roentgen", "Craft a Nuclear Reactor and discover its overengineered mechanisms", existingFileHelper);
        var nuke = createBasic(consumer, "nuke", nuclearReactor, "I've become Death, Destroyer of Worlds", "Craft a Nuke", existingFileHelper);
        var singularity = createBasic(consumer, "singularity", nuke, "Pocket Black Hole", "Craft a Singularity", existingFileHelper);
        var mixedIngotIridium = createBasic(consumer, "mixed_ingot_iridium", implosionCompressor, "Oreo Ingot", "Craft a Mixed Ingot Iridium to craft Iridium Plates", existingFileHelper);
        var quantumCircuit = createBasic(consumer, "quantum_circuit", mixedIngotIridium, AdvancementType.GOAL, "Mobius Strip Eigenvalues", "Craft a Quantum Circuit", existingFileHelper);
        var fusionReactor = createBasic(consumer, "fusion_reactor", quantumCircuit, AdvancementType.CHALLENGE, "Enslaved Star Core", "Craft a Fusion Reactor to produce insane amount of energy", existingFileHelper);
        var plasmaTurbine = createBasic(consumer, "plasma_turbine", fusionReactor, "Better than Nuclear Fission", "Craft a Plasma Turbine to transform Helium Plasma into energy", existingFileHelper);
        var basicUpgrade = createBasic(consumer, "basic_upgrade", chemicalReactor, AdvancementType.GOAL, "Machine Speedup", "Craft a Basic Upgrade to increase the maximum speed of an electric recipe", existingFileHelper);
        var advancedUpgrade = createBasic(consumer, "advanced_upgrade", electronicCircuit, "Machine Speedup-Speedup", "Craft a Advanced Upgrade", existingFileHelper);
        var turboUpgrade = createBasic(consumer, "turbo_upgrade", digitalCircuit, "Gotta go Fast", "Craft a Turbo Upgrade", existingFileHelper);
        var highlyAdvancedUpgrade = createBasic(consumer, "highly_advanced_upgrade", processingUnit, "I'm speed", "Craft an Highly Advanced Upgrade", existingFileHelper);
        var quantumUpgrade = createBasic(consumer, "quantum_upgrade", quantumCircuit, AdvancementType.GOAL, "Time Dilation", "Craft a Quantum Upgrade to unlock unlimited recipe speed", existingFileHelper);
        var replicator = createBasic(consumer, "replicator", quantumUpgrade, AdvancementType.CHALLENGE, "Legal Duping", "Craft a Replicator and replicate any item you want using UU Matter", existingFileHelper);
        var uuMatter = createBasic(consumer, "uu_matter_bucket", singularity, AdvancementType.GOAL, "Liquid Creative Mode", "Produce a bucket of UU Matter to start duplication in the replicator", existingFileHelper);
        var quantumSword = createBasic(consumer, "quantum_sword", quantumUpgrade, AdvancementType.CHALLENGE, "Annihilation Operator", "Craft a Quantum Sword and disintegrate your foes (or the Wandering Trader llama's)", existingFileHelper);
        var quantumChestplate = createBasic(consumer, "quantum_chestplate", quantumUpgrade, AdvancementType.CHALLENGE, "Quantum Immortality™", "Craft a Quantum Chestplate to reduce the probability of taking any damage by 25% for each piece of the Quantum Armor Set", existingFileHelper);
        // @formatter:on
    }

    private AdvancementHolder createBasic(Consumer<AdvancementHolder> consumer, String item, AdvancementHolder parent,
            String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        return createBasic(consumer, item, parent, AdvancementType.TASK, titleEnglishName, englishDescription, existingFileHelper);
    }

    private AdvancementHolder createBasic(Consumer<AdvancementHolder> consumer, String itemId, AdvancementHolder parent,
            AdvancementType frame, String titleEnglishName, String englishDescription, ExistingFileHelper existingFileHelper) {
        var item = BuiltInRegistries.ITEM.get(new MIIdentifier(itemId));

        var advancementTask = newBuilder(itemId);

        advancementTask.parent(parent);
        advancementTask.display(
                item,
                titleEnglishName,
                englishDescription,
                null,
                frame,
                true,
                true,
                false);
        advancementTask.addCriterion("checkInv", InventoryChangeTrigger.TriggerInstance.hasItems(item));

        return advancementTask.save(consumer, existingFileHelper);
    }
}
