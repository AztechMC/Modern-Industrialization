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

package aztech.modern_industrialization.materials.set;

import org.jspecify.annotations.Nullable;

public enum MaterialOreSet {
    IRON("iron"),
    GOLD("gold"),
    DIAMOND("diamond"),
    OLD("old"),
    LAPIS("lapis"),
    REDSTONE("redstone"),
    COPPER("copper"),
    EMERALD("emerald"),
    COAL("coal"),
    QUARTZ("quartz"),
    NETHER_GOLD("nether_gold");

    MaterialOreSet(String name) {
        this.name = name;
    }

    public final String name;

    @Nullable
    public static MaterialOreSet getByName(String ore_set) {
        for (MaterialOreSet set : values()) {
            if (set.name.equals(ore_set)) {
                return set;
            }
        }
        return null;
    }
}
