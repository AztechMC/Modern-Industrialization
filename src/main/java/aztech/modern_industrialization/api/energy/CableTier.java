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

import aztech.modern_industrialization.MIBlockKeys;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A single tier of cable that can have a varying amount of energy pushed through it per tick.
 */
public final class CableTier implements Comparable<CableTier> {
    // static references
    public static CableTier LV = new CableTier("lv", "LV", "Low Voltage", 32, null);
    public static CableTier MV = new CableTier("mv", "MV", "Medium Voltage", 32 * 4, MIBlockKeys.ADVANCED_MACHINE_HULL);
    public static CableTier HV = new CableTier("hv", "HV", "High Voltage", 32 * 4 * 8, MIBlockKeys.TURBO_MACHINE_HULL);
    public static CableTier EV = new CableTier("ev", "EV", "Extreme Voltage", 32 * 4 * 8 * 8, MIBlockKeys.HIGHLY_ADVANCED_MACHINE_HULL);
    public static CableTier SUPERCONDUCTOR = new CableTier("superconductor", "Superconductor", "Superconductor", 128000000,
            MIBlockKeys.QUANTUM_MACHINE_HULL);

    public final String name;
    public final String shortEnglishName;
    public final String longEnglishName;
    public final long eu;
    /**
     * {@code null} for LV only.
     */
    @Nullable
    public final ResourceLocation itemKey;
    /**
     * {@code true} if this is present in base MI, {@code false} if added using some API.
     */
    public final boolean builtin;

    /**
     * The {@link MachineCasing} that uses this cable tier.
     */
    @ApiStatus.Internal
    public final MachineCasing casing;

    @ApiStatus.Internal
    public CableTier(String name, String shortEnglishName, String longEnglishName, long eu, ResourceLocation itemKey, boolean builtin) {
        StoragePreconditions.notNegative(eu);

        this.name = name;
        this.shortEnglishName = shortEnglishName;
        this.longEnglishName = longEnglishName;
        this.eu = eu;
        this.itemKey = itemKey;
        this.casing = MachineCasings.create(name);
        this.builtin = builtin;
    }

    private CableTier(String name, String shortEnglishName, String longEnglishName, long eu, @Nullable ResourceKey<Block> key) {
        this(name, shortEnglishName, longEnglishName, eu, key == null ? null : key.location(), true);
    }

    public String shortEnglishKey() {
        return "cable_tier_short.modern_industrialization." + name;
    }

    public MutableComponent shortEnglishName() {
        return Component.translatable(shortEnglishKey());
    }

    public String longEnglishKey() {
        return "cable_tier_long.modern_industrialization." + name;
    }

    public MutableComponent longEnglishName() {
        return Component.translatable(longEnglishKey());
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
    private final static Map<String, CableTier> tiers = new HashMap<>();

    /**
     * Adds a new cable tier to the internal mapping of tiers. If the tier already existed,
     * then this will error.
     *
     * @param tier The new tier instance to register.
     */
    @ApiStatus.Internal
    public static void addTier(CableTier tier) {
        for (var existingTier : tiers.values()) {
            if (existingTier.name.equals(tier.name)) {
                throw new IllegalArgumentException("Tier " + tier + " already exists!");
            }
            if (existingTier.eu == tier.eu) {
                throw new IllegalArgumentException("A tier with eu " + tier.eu + " already exists!");
            }
            if (Objects.equals(existingTier.itemKey, tier.itemKey)) {
                throw new IllegalArgumentException("A tier with block key " + tier.itemKey + " already exists!");
            }
        }

        tiers.put(tier.name, tier);
    }

    /**
     * Looks up a cable tier registered by name.
     *
     * @param name The name of the tier, e.g. <code>lv</code>.
     * @return An instance of the cable tier with the provided name.
     */
    public static CableTier getTier(String name) {
        CableTier existing = tiers.get(name);

        if (existing == null) {
            throw new NoSuchElementException("No such cable tier: " + name);
        }

        return existing;
    }

    /**
     * @return A list of all registered CableTier instances, sorted by their EU.
     */
    public static List<CableTier> allTiers() {
        return tiers.values().stream().sorted().toList();
    }

    static {
        addTier(LV);
        addTier(MV);
        addTier(HV);
        addTier(EV);
        addTier(SUPERCONDUCTOR);

        KubeJSProxy.instance.fireCableTiersEvent();
    }
}
