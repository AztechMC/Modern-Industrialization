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

package aztech.modern_industrialization.machines.init;

public enum MachineTier {
    BRONZE("bronze", true, 2, 2),
    STEEL("steel", true, 4, 4),
    LV("lv", false, 32, 8),
    MULTIBLOCK("multiblock", false, 128, 8),
    UNLIMITED("unlimited", false, Integer.MAX_VALUE, 8);

    private final String name;
    private final boolean steam;
    private final int maxEu;
    private final int baseEu;

    MachineTier(String name, boolean steam, int maxEu, int baseEu) {
        this.name = name;
        this.steam = steam;
        this.maxEu = maxEu;
        this.baseEu = baseEu;
    }

    public boolean isSteam() {
        return steam;
    }

    public boolean isElectric() {
        return !steam;
    }

    public int getBaseEu() {
        return baseEu;
    }

    public int getMaxEu() {
        return maxEu;
    }

    @Override
    public String toString() {
        return name;
    }

    public int getMaxStoredEu() {
        return isSteam() ? 0 : getMaxEu() * 100;
    }
}
