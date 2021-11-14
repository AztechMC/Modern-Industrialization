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
package aztech.modern_industrialization.machines.models;

import aztech.modern_industrialization.api.energy.CableTier;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

public class MachineCasings {

    public static final Map<String, MachineCasing> registeredCasings = new HashMap<>();

    public static final MachineCasing BRICKED_BRONZE = create("bricked_bronze");
    public static final MachineCasing BRICKED_STEEL = create("bricked_steel");
    public static final MachineCasing BRICKS = create("bricks");
    public static final MachineCasing BRONZE = create("bronze");
    public static final MachineCasing BRONZE_PLATED_BRICKS = create("bronze_plated_bricks");
    public static final MachineCasing CLEAN_STAINLESS_STEEL = create("clean_stainless_steel_machine_casing");
    public static final MachineCasing STAINLESS_STEEL_PIPE = create("stainless_steel_machine_casing_pipe");
    public static final MachineCasing FIREBRICKS = create("firebricks");
    public static final MachineCasing FROSTPROOF = create("frostproof_machine_casing");
    public static final MachineCasing HEATPROOF = create("heatproof_machine_casing");
    public static final MachineCasing STEEL = create("steel");
    public static final MachineCasing STEEL_CRATE = create("steel_crate");
    public static final MachineCasing LV = create("lv");
    public static final MachineCasing MV = create("mv");
    public static final MachineCasing HV = create("hv");
    public static final MachineCasing EV = create("ev");
    public static final MachineCasing SUPRACONDUCTOR = create("supraconductor");
    public static final MachineCasing TITANIUM = create("titanium");
    public static final MachineCasing TITANIUM_PIPE = create("titanium_machine_casing_pipe");
    public static final MachineCasing SOLID_TITANIUM = create("solid_titanium_machine_casing");
    public static final MachineCasing NUCLEAR = create("nuclear_casing");
    public static final MachineCasing QUANTUM = create("quantum_casing");
    public static final MachineCasing PLASMA_HANDLING_IRIDIUM = create("plasma_handling_iridium_machine_casing");

    public static MachineCasing casingFromCableTier(CableTier tier) {
        if (tier == CableTier.LV) {
            return LV;
        } else if (tier == CableTier.MV) {
            return MV;
        } else if (tier == CableTier.HV) {
            return HV;
        } else if (tier == CableTier.EV) {
            return EV;
        } else if (tier == CableTier.SUPRACONDUCTOR) {
            return SUPRACONDUCTOR;
        }
        return null;
    }

    private static MachineCasing create(String name) {
        MachineCasing casing = new MachineCasing(name);
        registeredCasings.put(name, casing);
        // Load model on the client only
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            casing.mcm = new MachineCasingModel(name);
        }
        return casing;
    }

    public static MachineCasing get(String name) {
        MachineCasing casing = registeredCasings.get(name);
        if (casing != null) {
            return casing;
        } else {
            throw new IllegalArgumentException("Machine casing model \"" + name + "\" does not exist.");
        }
    }
}
