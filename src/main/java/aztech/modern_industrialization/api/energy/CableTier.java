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
package aztech.modern_industrialization.api.energy;

public enum CableTier {
    LV("lv", 32),
    MV("mv", 32 * 4),
    HV("hv", 32 * 4 * 8),
    EV("ev", 32 * 4 * 8 * 8),
    SUPERCONDUCTOR("superconductor", 128000000);

    public final String name;
    public final long eu;

    public final String translationKey;

    CableTier(String name, long eu) {
        this.name = name;
        this.eu = eu;
        this.translationKey = "text.modern_industrialization.cable_tier_" + name;

    }

    /**
     * @return The total EU/t transferred by this tier of network. The same number
     *         is also the internal storage of every node.
     */
    public long getMaxTransfer() {
        return eu * 8;
    }

    public long getEu() {
        return eu;
    }

    @Override
    public String toString() {
        return name;
    }

    public static final CableTier getTier(String name) {
        for (CableTier tier : CableTier.values()) {
            if (tier.name.equals(name)) {
                return tier;
            }
        }
        return null;
    }
}
