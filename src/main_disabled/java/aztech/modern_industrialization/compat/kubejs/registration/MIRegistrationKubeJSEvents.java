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
package aztech.modern_industrialization.compat.kubejs.registration;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface MIRegistrationKubeJSEvents {
    EventGroup EVENT_GROUP = EventGroup.of("MIRegistrationEvents");

    EventHandler REGISTER_CABLE_TIERS = EVENT_GROUP.startup("registerCableTiers", () -> RegisterCableTiersEventJS.class);
    EventHandler REGISTER_FLUIDS = EVENT_GROUP.startup("registerFluids", () -> RegisterFluidsEventJS.class);
    EventHandler REGISTER_FLUID_FUELS = EVENT_GROUP.startup("registerFluidFuels", () -> RegisterFluidFuelsEventJS.class);
    EventHandler REGISTER_FLUID_NEUTRON_INTERACTIONS = EVENT_GROUP.startup("registerFluidNeutronInteractions",
            () -> RegisterFluidNeutronInteractionsEventJS.class);
}
