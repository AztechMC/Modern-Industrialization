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

import static aztech.modern_industrialization.machines.init.MachineRegistrationHelper.*;

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.*;
import aztech.modern_industrialization.machines.components.FluidConsumerComponent;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;

public class SingleBlockSpecialMachines {

    public static void init() {

        registerMachine("Bronze Boiler", "bronze_boiler", bet -> new BoilerMachineBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);
        registerMachine("Steel Boiler", "steel_boiler", bet -> new BoilerMachineBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);

        // TODO: register water pumps in REI?
        registerMachine("Bronze Water Pump", "bronze_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi);
        registerMachine("Steel Water Pump", "steel_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi);
        registerMachine("Electric Water Pump", "electric_water_pump", ElectricWaterPumpBlockEntity::new,
                MachineBlockEntity::registerFluidApi,
                ElectricWaterPumpBlockEntity::registerEnergyApi);

        registerTransformer(CableTier.LV, CableTier.MV);
        registerTransformer(CableTier.MV, CableTier.HV);
        registerTransformer(CableTier.HV, CableTier.EV);
        registerTransformer(CableTier.EV, CableTier.SUPERCONDUCTOR);

        registerSteamTurbine(CableTier.LV, 32, 1600);
        registerSteamTurbine(CableTier.MV, 128, 3200);
        registerSteamTurbine(CableTier.HV, 512, 6400);

        registerEnergyStorage(CableTier.LV);
        registerEnergyStorage(CableTier.MV);
        registerEnergyStorage(CableTier.HV);
        registerEnergyStorage(CableTier.EV);
        registerEnergyStorage(CableTier.SUPERCONDUCTOR);

        registerMachine("Starter Diesel Generator", "starter_diesel_generator",
                bet -> new EnergyFromFluidMachineBlockEntity(bet, "starter_diesel_generator", CableTier.LV, 4000, 16000,
                        FluidConsumerComponent.ofFluidFuels(64)),
                MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        registerMachine("Diesel Generator", "diesel_generator",
                bet -> new EnergyFromFluidMachineBlockEntity(bet, "diesel_generator", CableTier.MV, 12000, 32000,
                        FluidConsumerComponent.ofFluidFuels(256)),
                MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        registerMachine("Turbo Diesel Generator", "turbo_diesel_generator",
                bet -> new EnergyFromFluidMachineBlockEntity(bet, "turbo_diesel_generator", CableTier.HV, 60000, 64000,
                        FluidConsumerComponent.ofFluidFuels(1024)),
                MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        registerMachine("Configurable Chest", "configurable_chest", ConfigurableChestMachineBlockEntity::new,
                MachineBlockEntity::registerItemApi);

        registerMachine("Configurable Tank", "configurable_tank", ConfigurableTankMachineBlockEntity::new,
                MachineBlockEntity::registerFluidApi);

        registerMachine("Replicator", "replicator", ReplicatorMachineBlockEntity::new, MachineBlockEntity::registerFluidApi,
                MachineBlockEntity::registerItemApi);

        addModelsForTiers("water_pump", true, true, true, "bronze", "steel", "electric");
        addMachineModel("bronze_boiler", "boiler", MachineCasings.BRICKED_BRONZE, true, false, false);
        addMachineModel("steel_boiler", "boiler", MachineCasings.BRICKED_STEEL, true, false, false);
        addMachineModel("starter_diesel_generator", "diesel_generator", MachineCasings.LV, true, true, true);
        addMachineModel("diesel_generator", "diesel_generator", MachineCasings.MV, true, true, true);
        addMachineModel("turbo_diesel_generator", "diesel_generator", MachineCasings.HV, true, true, true);
        addMachineModel("configurable_chest", "", MachineCasings.STEEL_CRATE, false, false, false, false);
        addMachineModel("configurable_tank", "", MachineCasings.CONFIGURABLE_TANK, false, false, false, false);
        addMachineModel("replicator", "replicator", MachineCasings.SUPERCONDUCTOR, true, false, true, true);
    }

    public static MachineCasing getTransformerCasingFromTier(CableTier from, CableTier to) {
        return MachineCasings.casingFromCableTier(from.eu() > to.eu() ? to : from);
    }

    /**
     * Registers a new transformer block for the provided tiers.
     *
     * @param base The base tier to transform from.
     * @param up   The tier to transform upto, or down from.
     */
    private static void registerTransformer(CableTier base, CableTier up) {
        String baseToHigherId = TransformerMachineBlockEntity.getTransformerName(base, up);
        String higherToBaseId = TransformerMachineBlockEntity.getTransformerName(up, base);

        registerMachine(
                TransformerMachineBlockEntity.getTransformerEnglishName(base, up),
                baseToHigherId,
                bep -> new TransformerMachineBlockEntity(bep, base, up),
                AbstractStorageMachineBlockEntity::registerEnergyApi);

        registerMachine(
                TransformerMachineBlockEntity.getTransformerEnglishName(up, base),
                higherToBaseId,
                bep -> new TransformerMachineBlockEntity(bep, up, base),
                AbstractStorageMachineBlockEntity::registerEnergyApi);

        addMachineModel(
                baseToHigherId,
                "transformer",
                getTransformerCasingFromTier(base, up),
                true, true, true, false);

        addMachineModel(
                higherToBaseId,
                "transformer",
                getTransformerCasingFromTier(base, up),
                true, true, true, false);
    }

    // TODO: Should maxConsumption and fluidCapacity be based automatically off of tier?
    /**
     * Registers a new steam turbine block for the provided tier.
     *
     * @param tier           The CableTier this turbine uses.
     * @param maxConsumption The maximum EU this turbine can output.
     * @param fluidCapacity  The maximum fluid capacity of this turbine, in droplets.
     */
    private static void registerSteamTurbine(
            CableTier tier,
            int maxConsumption,
            int fluidCapacity) {
        var machineId = tier.name() + "_steam_turbine";
        var machineEnglishName = tier.englishName() + " Steam Turbine";

        registerMachine(
                machineEnglishName,
                machineId,
                bep -> new EnergyFromFluidMachineBlockEntity(
                        bep,
                        machineId,
                        tier,
                        maxConsumption * 100L,
                        fluidCapacity,
                        maxConsumption,
                        MIFluids.STEAM.asFluid(),
                        1),
                MachineBlockEntity::registerFluidApi,
                EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        addMachineModel(
                machineId,
                "steam_turbine",
                MachineCasings.casingFromCableTier(tier),
                true, false, false);
    }

    /**
     * Registers an EU energy storage machine for the specified tier.
     */
    private static void registerEnergyStorage(CableTier tier) {
        var id = tier.name() + "_storage_unit";
        var englishName = tier.englishName() + " Storage Unit";

        registerMachine(
                englishName,
                id,
                bep -> new StorageMachineBlockEntity(bep, tier, id, 60 * 5 * 20 * tier.eu()),
                AbstractStorageMachineBlockEntity::registerEnergyApi);

        addMachineModel(
                id,
                "electric_storage",
                MachineCasings.casingFromCableTier(tier),
                true, false, true, false);
    }
}
