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
package aztech.modern_industrialization.compat.kubejs.material;

import aztech.modern_industrialization.api.energy.CableTier;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.network.chat.Component;

/**
 * Event class for registering new cable tiers.
 */
public final class AddCableTiersEventJS extends EventJS {
    // useful primarily for setting EU based off of a previous tier's value.
    @Info("""
            Gets a previously registered cable tier by name.
            """)
    public CableTier get(String name) {
        return CableTier.getTier(name);
    }

    @SuppressWarnings("unused") // shh, intellij
    @Info("""
            Adds a new tier to the list of registered cable tiers.
            """)
    public CableTier addTier(String englishName, String name, long eu) {
        Component key = Component.translatable("cable_tier.modern_industrialization." + name);
        CableTier tier = new CableTier(englishName, name, eu, key);
        CableTier.addTier(tier);
        return tier;
    }
}
