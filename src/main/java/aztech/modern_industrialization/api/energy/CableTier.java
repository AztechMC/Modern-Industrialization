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

import aztech.modern_industrialization.MIText;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.minecraft.network.chat.MutableComponent;

/**
 * A single tier of cables.
 */
public interface CableTier extends Comparable<CableTier> {
    SortedSet<CableTier> TIERS = new TreeSet<>();

    /**
     * Gets a table tier by internal name.
     *
     * @param name The internal name of the tier, e.g. 'LV'.
     * @return If it exists, the added cable tier; otherwise, <code>null</code>.
     */
    static @Nullable CableTier getByName(String name) {
        for (var tier : TIERS) {
            if (Objects.equals(tier.name(), name))
                return tier;
        }

        return null;
    }

    static CableTier create(String name, String englishName, long eu, MutableComponent text) {
        return new CableTierImpl(name, englishName, eu, eu * 8, text);
    }

    static CableTier create(String name, String englishName, long eu, long maxTransfer, MutableComponent text) {
        return new CableTierImpl(name, englishName, eu, maxTransfer, text);
    }

    CableTier LV = create("lv", "LV", 32, MIText.CableTierLV.text());
    CableTier MV = create("mv", "MV", 32 * 4, MIText.CableTierMV.text());
    CableTier HV = create("hv", "HV", 32 * 4 * 8, MIText.CableTierHV.text());
    CableTier EV = create("ev", "EV", 32 * 4 * 8 * 8, MIText.CableTierEV.text());
    CableTier SUPERCONDUCTOR = create("superconductor", "SUPERCONDUCTOR", 128_000_000, MIText.CableTierSuperconductor.text());

    /** The internal name for this tier. */
    String name();

    /** The English localised name for this tier. */
    String englishName();

    // TODO: kill this brutally
    /** The English text component for this tier. */
    MutableComponent englishTextComponent();

    long eu();

    /**
     * @return The total EU/t transferred by this tier of network. The same number
     *         is also the internal storage of every node.
     */
    default long maxTransfer() {
        return eu() * 8;
    }
}
