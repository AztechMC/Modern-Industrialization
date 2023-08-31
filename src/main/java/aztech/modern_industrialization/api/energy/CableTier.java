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
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

/**
 * A single tier of cable that can have a varying amount of energy pushed through it per tick.
 */
public final class CableTier implements Comparable<CableTier> {
    // static references
    public static CableTier LV = new CableTier("LV", "lv", 32, MIText.CableTierLV);
    public static CableTier MV = new CableTier("MV", "mv", 32 * 4, MIText.CableTierMV);
    public static CableTier HV = new CableTier("HV", "hv", 32 * 4 * 8, MIText.CableTierHV);
    public static CableTier EV = new CableTier("EV", "ev", 32 * 4 * 8 * 8, MIText.CableTierEV);
    public static CableTier SUPERCONDUCTOR = new CableTier("Superconductor", "superconductor", 128000000, MIText.CableTierSuperconductor);

    // actual fields
    public final String englishName;
    public final String name;
    public final long eu;

    public final String translationKey;
    private final Component englishNameComponent;

    /**
     * The {@link MachineCasing} that uses this cable tier.
     */
    public final MachineCasing casing;

    public CableTier(String englishName, String name, long eu, Component englishNameComponent) {
        this.englishName = englishName;
        this.name = name;
        this.eu = eu;
        this.translationKey = "text.modern_industrialization.cable_tier_" + name;
        this.englishNameComponent = englishNameComponent;
        this.casing = MachineCasings.create(name);
    }

    // package private as nobody will need to pass an MIText externally?
    CableTier(String englishName, String name, long eu, MIText englishNameText) {
        this(englishName, name, eu, englishNameText.text());
    }

    /**
     * @return A copy of the english name text component stored within.
     */
    public MutableComponent getEnglishNameComponent() {
        return englishNameComponent.copy();
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
    public int compareTo(@NotNull CableTier other) {
        return Long.compare(eu, other.eu);
    }

    @Override
    public String toString() {
        return name;
    }

    // "registry" of name -> instance.
    private final static Map<String, CableTier> TIERS = new HashMap<>();

    /**
     * Adds a new cable tier to the internal mapping of tiers. If the tier already existed,
     * then this will error.
     *
     * @param tier The new tier instance to register.
     */
    public static void addTier(CableTier tier) {
        if (TIERS.containsKey(tier.name)) {
            throw new IllegalArgumentException("Tier " + tier + " already exists!");
        }

        TIERS.put(tier.name, tier);
    }

    /**
     * Looks up a cable tier registered by name.
     *
     * @param name The name of the tier, e.g. <code>LV</code>.
     * @return An instance of the cable tier with the provided name.
     */
    @NotNull // Should it be?
    public static CableTier getTier(String name) {
        CableTier existing = TIERS.get(name);

        if (existing == null) {
            throw new NoSuchElementException("No such cable tier: " + name);
        }

        return existing;
    }

    @Deprecated
    public static CableTier getByName(String tier) {
        return getTier(tier);
    }

    /**
     * @return A list of all registered CableTier instances, sorted by their EU.
     */
    public static List<CableTier> allTiers() {
        return TIERS.values().stream().sorted().toList();
    }

    static {
        addTier(LV);
        addTier(MV);
        addTier(HV);
        addTier(EV);
        addTier(SUPERCONDUCTOR);
    }
}
