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
package aztech.modern_industrialization.machines.init;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.*;
import aztech.modern_industrialization.machines.components.FluidItemConsumerComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SingleBlockSpecialMachines {

    public static void init() {
        MachineRegistrationHelper.registerMachine("Bronze Boiler", "bronze_boiler", bet -> new BoilerMachineBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);
        MachineRegistrationHelper.registerMachine("Steel Boiler", "steel_boiler", bet -> new BoilerMachineBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);

        // TODO: register water pumps in REI?
        MachineRegistrationHelper.registerMachine("Bronze Water Pump", "bronze_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("Steel Water Pump", "steel_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("Electric Water Pump", "electric_water_pump", ElectricWaterPumpBlockEntity::new,
                MachineBlockEntity::registerFluidApi,
                ElectricWaterPumpBlockEntity::registerEnergyApi);

        registerTransformers();
        registerEUStorage();

        registerSteamTurbine(CableTier.LV, 32, 16000);
        registerSteamTurbine(CableTier.MV, 128, 32000);
        registerSteamTurbine(CableTier.HV, 512, 64000);

        MachineRegistrationHelper.registerMachine("LV Diesel Generator", "lv_diesel_generator",
                bet -> new GeneratorMachineBlockEntity(bet, "lv_diesel_generator",
                        CableTier.LV, 4000, 16000,
                        FluidItemConsumerComponent.ofFluidFuels(64)),
                MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.registerMachine("MV Diesel Generator", "mv_diesel_generator",
                bet -> new GeneratorMachineBlockEntity(bet, "mv_diesel_generator", CableTier.MV, 12000, 32000,
                        FluidItemConsumerComponent.ofFluidFuels(256)),
                MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.registerMachine("HV Diesel Generator", "hv_diesel_generator",
                bet -> new GeneratorMachineBlockEntity(bet, "hv_diesel_generator", CableTier.HV, 60000, 64000,
                        FluidItemConsumerComponent.ofFluidFuels(1024)),
                MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.registerMachine("Configurable Chest", "configurable_chest", ConfigurableChestMachineBlockEntity::new,
                MachineBlockEntity::registerItemApi);

        MachineRegistrationHelper.registerMachine("Configurable Tank", "configurable_tank", ConfigurableTankMachineBlockEntity::new,
                MachineBlockEntity::registerFluidApi);

        MachineRegistrationHelper.registerMachine("Replicator", "replicator", ReplicatorMachineBlockEntity::new, MachineBlockEntity::registerFluidApi,
                MachineBlockEntity::registerItemApi);

        MachineRegistrationHelper.addModelsForTiers("water_pump", true, true, true, "bronze", "steel", "electric");
        MachineRegistrationHelper.addMachineModel("bronze_boiler", "boiler", MachineCasings.BRICKED_BRONZE, true, false, false);
        MachineRegistrationHelper.addMachineModel("steel_boiler", "boiler", MachineCasings.BRICKED_STEEL, true, false, false);
        MachineRegistrationHelper.addMachineModel("lv_diesel_generator", "diesel_generator", CableTier.LV.casing, true, true, true);
        MachineRegistrationHelper.addMachineModel("mv_diesel_generator", "diesel_generator", CableTier.MV.casing, true, true, true);
        MachineRegistrationHelper.addMachineModel("hv_diesel_generator", "diesel_generator", CableTier.HV.casing, true, true, true);
        MachineRegistrationHelper.addMachineModel("configurable_chest", "", MachineCasings.STEEL_CRATE, false, false, false, false);
        MachineRegistrationHelper.addMachineModel("configurable_tank", "", MachineCasings.CONFIGURABLE_TANK, false, false, false, false);
        MachineRegistrationHelper.addMachineModel("replicator", "replicator", CableTier.EV.casing, true, false, true, true);
    }

    private static void registerTransformer(CableTier low, CableTier up) {
        String lowToUp = TransformerMachineBlockEntity.getTransformerName(low, up);
        String lowToUpName = TransformerMachineBlockEntity.getTransformerEnglishName(low, up);
        MachineRegistrationHelper.registerMachine(lowToUpName, lowToUp, bet -> new TransformerMachineBlockEntity(bet, low, up),
                AbstractStorageMachineBlockEntity::registerEnergyApi);

        String upToLow = TransformerMachineBlockEntity.getTransformerName(up, low);
        String upToLowName = TransformerMachineBlockEntity.getTransformerEnglishName(up, low);
        MachineRegistrationHelper.registerMachine(upToLowName, upToLow, bet -> new TransformerMachineBlockEntity(bet, up, low),
                AbstractStorageMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.addMachineModel(lowToUp, "transformer", getTransformerCasingFromTier(low, up), true, true, true, false);
        MachineRegistrationHelper.addMachineModel(upToLow, "transformer", getTransformerCasingFromTier(up, low), true, true, true, false);
    }

    private static void registerTransformers() {
        record TierPair(CableTier low, CableTier high) {
        }

        Set<TierPair> registeredPairs = new HashSet<>();

        // Register N <-> N+1 transformers
        List<CableTier> tiers = CableTier.allTiers();
        for (int i = 0; i < tiers.size() - 1; i++) {
            var pair = new TierPair(tiers.get(i), tiers.get(i + 1));
            registerTransformer(pair.low, pair.high);
            registeredPairs.add(pair);
        }

        // Register transformers for builtin MI cable tiers, even if added tiers are placed in between.
        // This ensures that our recipes, guidebook pages, etc... won't error if tiers are added.
        List<CableTier> builtinTiers = CableTier.allTiers().stream().filter(x -> x.builtin).toList();
        for (int i = 0; i < builtinTiers.size() - 1; i++) {
            var pair = new TierPair(builtinTiers.get(i), builtinTiers.get(i + 1));
            if (registeredPairs.add(pair)) {
                registerTransformer(pair.low, pair.high);
            }
        }
    }

    public static MachineCasing getTransformerCasingFromTier(CableTier from, CableTier to) {
        if (from.eu > to.eu) {
            return from.casing;
        } else {
            return to.casing;
        }
    }

    private static void registerSteamTurbine(CableTier tier, int eu, int fluidCapacity) {
        String id = tier.name + "_steam_turbine";
        String englishName = tier.shortEnglishName + " Steam Turbine";
        MachineRegistrationHelper.registerMachine(englishName, id,
                bet -> new GeneratorMachineBlockEntity(bet, id, tier, eu * 100L, fluidCapacity, eu,
                        MIFluids.STEAM, 1),
                MachineBlockEntity::registerFluidApi, GeneratorMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.addMachineModel(id, "steam_turbine", tier.casing, true, false, false);
    }

    private static void registerEUStorage() {
        for (CableTier tier : CableTier.allTiers()) {
            String id = tier.name + "_storage_unit";
            String englishName = tier.shortEnglishName + " Storage Unit";
            MachineRegistrationHelper.registerMachine(englishName, id, bet -> new StorageMachineBlockEntity(bet, tier, id, 60 * 5 * 20 * tier.eu),
                    AbstractStorageMachineBlockEntity::registerEnergyApi);

            MachineRegistrationHelper.addMachineModel(id, "electric_storage", tier.casing, true, false, true, false);
        }
    }

}
