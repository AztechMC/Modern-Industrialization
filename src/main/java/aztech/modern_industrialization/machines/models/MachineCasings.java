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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class MachineCasings {

    public static final Map<ResourceLocation, MachineCasing> registeredCasings = new HashMap<>();
    public static final Map<ResourceLocation, String> casingNames = new HashMap<>();

    public static final MachineCasing BRICKED_BRONZE = create("bricked_bronze", "Bricked Bronze");
    public static final MachineCasing BRICKED_STEEL = create("bricked_steel", "Bricked Steel");
    public static final MachineCasing BRICKS = create("bricks", "Bricks");
    public static final MachineCasing BRONZE = create("bronze", "Bronze");
    public static final MachineCasing BRONZE_PLATED_BRICKS = create("bronze_plated_bricks", "Bronze Plated Bricks");
    public static final MachineCasing CLEAN_STAINLESS_STEEL = create("clean_stainless_steel_machine_casing", "Clean Stainless Steel");
    public static final MachineCasing CONFIGURABLE_TANK = create("configurable_tank", "Configurable Tank");
    public static final MachineCasing STAINLESS_STEEL_PIPE = create("stainless_steel_machine_casing_pipe", "Stainless Steel Pipe");
    public static final MachineCasing FIREBRICKS = create("firebricks", "Firebricks");
    public static final MachineCasing FROSTPROOF = create("frostproof_machine_casing", "Frostproof");
    public static final MachineCasing HEATPROOF = create("heatproof_machine_casing", "Heatproof");
    public static final MachineCasing STEEL = create("steel", "Steel");
    public static final MachineCasing STEEL_CRATE = create("steel_crate", "Steel Crate");
    public static final MachineCasing TITANIUM = create("titanium", "Titanium");
    public static final MachineCasing TITANIUM_PIPE = create("titanium_machine_casing_pipe", "Titanium Pipe");
    public static final MachineCasing SOLID_TITANIUM = create("solid_titanium_machine_casing", "Solid Titanium");
    public static final MachineCasing NUCLEAR = create("nuclear_casing", "Nuclear");
    public static final MachineCasing PLASMA_HANDLING_IRIDIUM = create("plasma_handling_iridium_machine_casing", "Plasma Handling Iridium");

    static {
        KubeJSProxy.instance.fireRegisterMachineCasingsEvent();
    }

    public static MachineCasing create(ResourceLocation key, String englishName) {
        if (registeredCasings.containsKey(key)) {
            throw new IllegalArgumentException("Duplicate machine casing definition: " + key);
        }

        MachineCasing casing = new MachineCasing(key);
        registeredCasings.put(key, casing);
        casingNames.put(key, englishName);
        return casing;
    }

    public static MachineCasing create(String name, String englishName) {
        return create(MI.id(name), englishName);
    }

    public static MachineCasing get(ResourceLocation key) {
        MachineCasing casing = registeredCasings.get(key);
        if (casing != null) {
            return casing;
        } else {
            throw new IllegalArgumentException("Machine casing model \"" + key + "\" does not exist.");
        }
    }

    public static MachineCasing get(String name) {
        return get(ResourceLocation.isValidPath(name) ? MI.id(name) : ResourceLocation.parse(name));
    }
}
