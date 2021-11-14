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
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.*;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.fluid.Fluid;

public class SingleBlockSpecialMachines {

    public static void init() {

        MachineRegistrationHelper.registerMachine("bronze_boiler", bet -> new BoilerMachineBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);
        MachineRegistrationHelper.registerMachine("steel_boiler", bet -> new BoilerMachineBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi, MachineBlockEntity::registerItemApi);

        // TODO: register water pumps in REI?
        MachineRegistrationHelper.registerMachine("bronze_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("steel_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("electric_water_pump", ElectricWaterPumpBlockEntity::new, MachineBlockEntity::registerFluidApi,
                ElectricWaterPumpBlockEntity::registerEnergyApi);

        registerTransformers();
        registerSteamTurbines(32, 128, 512);
        registerEUStorage();

        MachineRegistrationHelper.registerMachine("diesel_generator",
                bet -> new EnergyFromFluidMachineBlockEntity(bet, "diesel_generator", CableTier.MV, 12000, 32000, 256,
                        (Fluid f) -> (FluidFuelRegistry.getEu(f) != 0), FluidFuelRegistry::getEu),
                MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.registerMachine("turbo_diesel_generator",
                bet -> new EnergyFromFluidMachineBlockEntity(bet, "turbo_diesel_generator", CableTier.HV, 60000, 64000, 1024,
                        (Fluid f) -> (FluidFuelRegistry.getEu(f) != 0), FluidFuelRegistry::getEu),
                MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

        MachineRegistrationHelper.registerMachine("configurable_chest", ConfigurableChestMachineBlockEntity::new,
                MachineBlockEntity::registerItemApi);

        MachineRegistrationHelper.registerMachine("replicator", ReplicatorMachineBlockEntity::new, MachineBlockEntity::registerFluidApi,
                MachineBlockEntity::registerItemApi);

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MachineModels.addTieredMachineTiers("water_pump", true, true, true, "bronze", "steel", "electric");
            MachineModels.addTieredMachine("bronze_boiler", "boiler", MachineCasings.BRICKED_BRONZE, true, false, false);
            MachineModels.addTieredMachine("steel_boiler", "boiler", MachineCasings.BRICKED_STEEL, true, false, false);
            MachineModels.addTieredMachine("diesel_generator", "diesel_generator", MachineCasings.MV, true, true, true);
            MachineModels.addTieredMachine("turbo_diesel_generator", "diesel_generator", MachineCasings.HV, true, true, true);
            MachineModels.addTieredMachine("configurable_chest", "", MachineCasings.STEEL_CRATE, false, false, false, false);
            MachineModels.addTieredMachine("replicator", "replicator", MachineCasings.SUPRACONDUCTOR, true, false, true, true);

        }
    }

    private static void registerTransformers() {
        CableTier[] tiers = CableTier.values();
        for (int i = 0; i < tiers.length - 1; i++) {
            final CableTier low = tiers[i];
            final CableTier up = tiers[i + 1];

            String lowToUp = TransformerMachineBlockEntity.getTransformerName(low, up);
            MachineRegistrationHelper.registerMachine(lowToUp, bet -> new TransformerMachineBlockEntity(bet, low, up),
                    AbstractStorageMachineBlockEntity::registerEnergyApi);

            String upToLow = TransformerMachineBlockEntity.getTransformerName(up, low);
            MachineRegistrationHelper.registerMachine(upToLow, bet -> new TransformerMachineBlockEntity(bet, up, low),
                    AbstractStorageMachineBlockEntity::registerEnergyApi);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(lowToUp, "transformer", getTransformerCasingFromTier(low, up), true, true, true, false);
                MachineModels.addTieredMachine(upToLow, "transformer", getTransformerCasingFromTier(up, low), true, true, true, false);
            }
        }
    }

    public static MachineCasing getTransformerCasingFromTier(CableTier from, CableTier to) {
        return MachineCasings.casingFromCableTier(from.eu > to.eu ? to : from);
    }

    private static void registerSteamTurbines(int... maxConsumption) {
        for (int i = 0; i < maxConsumption.length; i++) {
            CableTier tier = CableTier.values()[i];
            String id = tier.name + "_steam_turbine";
            final int eu = maxConsumption[i];
            final int fluidCapacity = 16000 * (1 << i);
            MachineRegistrationHelper.registerMachine(id,
                    bet -> new EnergyFromFluidMachineBlockEntity(bet, id, tier, eu * 100L, fluidCapacity, eu, MIFluids.STEAM, 1),
                    MachineBlockEntity::registerFluidApi, EnergyFromFluidMachineBlockEntity::registerEnergyApi);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(id, "steam_turbine", MachineCasings.casingFromCableTier(tier), true, false, false);
            }
        }
    }

    private static void registerEUStorage() {
        for (CableTier tier : CableTier.values()) {
            String id = tier.name + "_storage_unit";
            MachineRegistrationHelper.registerMachine(id, bet -> new StorageMachineBlockEntity(bet, tier, id, 60 * 5 * 20 * tier.eu),
                    AbstractStorageMachineBlockEntity::registerEnergyApi);

            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                MachineModels.addTieredMachine(id, "electric_storage", MachineCasings.casingFromCableTier(tier), true, false, true, false);
            }
        }
    }

}
