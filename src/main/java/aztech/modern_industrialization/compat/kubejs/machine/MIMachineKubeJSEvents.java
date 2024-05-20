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
package aztech.modern_industrialization.compat.kubejs.machine;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public interface MIMachineKubeJSEvents {
    EventGroup EVENT_GROUP = EventGroup.of("MIMachineEvents");

    EventHandler REGISTER_RECIPE_TYPES = EVENT_GROUP.startup("registerRecipeTypes", () -> RegisterRecipeTypesEventJS.class);
    EventHandler REGISTER_CASINGS = EVENT_GROUP.startup("registerCasings", () -> RegisterCasingsEventJS.class);
    EventHandler REGISTER_MACHINES = EVENT_GROUP.startup("registerMachines", () -> RegisterMachinesEventJS.class);
    EventHandler REGISTER_UPGRADES = EVENT_GROUP.startup("registerUpgrades", () -> RegisterUpgradesEventJS.class);
    EventHandler ADD_MULTIBLOCK_SLOTS = EVENT_GROUP.startup("addMultiblockSlots", () -> AddMultiblockSlotsEventJS.class).extra(Extra.REQUIRES_STRING);
    EventHandler ADD_EBF_TIERS = EVENT_GROUP.startup("addEbfTiers", () -> AddEbfTiersEventJS.class);
}
