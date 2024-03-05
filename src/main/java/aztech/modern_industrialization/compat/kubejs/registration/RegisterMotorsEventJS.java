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

import aztech.modern_industrialization.api.pipe.item.SpeedUpgrade;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;

public class RegisterMotorsEventJS extends EventJS {
    public void register(ResourceLocation id, long upgrade) {
        if (SpeedUpgrade.UPGRADES.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registration of motor " + id);
        }
        if (upgrade <= 0) {
            throw new IllegalArgumentException("Motor " + id + " must have positive speed, given: " + upgrade);
        }
        SpeedUpgrade.UPGRADES.put(id, upgrade);
    }

    public void modify(ResourceLocation id, long upgrade) {
        if (!SpeedUpgrade.UPGRADES.containsKey(id)) {
            throw new IllegalArgumentException("No such motor: " + id);
        }
        if (upgrade <= 0) {
            throw new IllegalArgumentException("Motor " + id + " must have positive speed, given: " + upgrade);
        }
        SpeedUpgrade.UPGRADES.put(id, upgrade);
    }

    public void remove(ResourceLocation id) {
        if (!SpeedUpgrade.UPGRADES.containsKey(id)) {
            throw new IllegalArgumentException("No such motor: " + id);
        }
        SpeedUpgrade.UPGRADES.remove(id);
    }
}
