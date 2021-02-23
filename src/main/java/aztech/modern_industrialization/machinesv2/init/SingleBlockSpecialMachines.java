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

import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.ElectricWaterPumpBlockEntity;
import aztech.modern_industrialization.machinesv2.blockentities.SteamWaterPumpBlockEntity;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class SingleBlockSpecialMachines {
    public static void init() {
        // TODO: register water pumps in REI?
        MachineRegistrationHelper.registerMachine("bronze_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, true),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("steel_water_pump", bet -> new SteamWaterPumpBlockEntity(bet, false),
                MachineBlockEntity::registerFluidApi);
        MachineRegistrationHelper.registerMachine("lv_water_pump", ElectricWaterPumpBlockEntity::new, bet -> {
            MachineBlockEntity.registerFluidApi(bet);
            ElectricWaterPumpBlockEntity.registerEnergyApi(bet);
        });

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            MachineModels.addTieredMachineTiers("water_pump", true, true, true, "bronze", "steel", "lv");
        }
    }
}
