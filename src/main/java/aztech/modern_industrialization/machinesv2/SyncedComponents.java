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
package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.components.sync.*;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.Identifier;

public final class SyncedComponents {
    public static final Identifier AUTO_EXTRACT = new MIIdentifier("auto_extract");
    public static final Identifier ENERGY_BAR = new MIIdentifier("energy_bar");
    public static final Identifier PROGRESS_BAR = new MIIdentifier("progress_bar");
    public static final Identifier RECIPE_EFFICIENCY_BAR = new MIIdentifier("recipe_efficiency_bar");
    public static final Identifier TEMPERATURE_BAR = new MIIdentifier("temperature_bar");
    public static final Identifier CRAFTING_MULTIBLOCK_GUI = new MIIdentifier("crafting_multiblock_gui");
    public static final Identifier REI_SLOT_LOCKING = new MIIdentifier("rei_slot_locking");

    public static final class Client {
        private static final Map<Identifier, SyncedComponent.ClientFactory> components = new HashMap<>();

        public static SyncedComponent.ClientFactory get(Identifier identifier) {
            return components.get(identifier);
        }

        public static void register(Identifier id, SyncedComponent.ClientFactory clientFactory) {
            if (components.put(id, clientFactory) != null) {
                throw new RuntimeException("Duplicate registration of component identifier.");
            }
        }

        static {
            register(AUTO_EXTRACT, AutoExtract.Client::new);
            register(ENERGY_BAR, EnergyBar.Client::new);
            register(PROGRESS_BAR, ProgressBar.Client::new);
            register(RECIPE_EFFICIENCY_BAR, RecipeEfficiencyBar.Client::new);
            register(TEMPERATURE_BAR, TemperatureBar.Client::new);
            register(CRAFTING_MULTIBLOCK_GUI, CraftingMultiblockGui.Client::new);
            register(REI_SLOT_LOCKING, ReiSlotLocking.Client::new);
        }
    }
}
