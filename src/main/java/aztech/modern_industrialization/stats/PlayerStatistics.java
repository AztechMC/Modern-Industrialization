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
package aztech.modern_industrialization.stats;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class PlayerStatistics {
    public static final PlayerStatistics DUMMY = new PlayerStatistics();

    private final Map<Item, StatisticValue> usedItems = new IdentityHashMap<>(), producedItems = new IdentityHashMap<>();
    private final Map<Fluid, StatisticValue> usedFluids = new IdentityHashMap<>(), producedFluids = new IdentityHashMap<>();

    PlayerStatistics() {
    }

    PlayerStatistics(CompoundTag nbt) {
        readNbt(Registry.ITEM, usedItems, nbt.getCompound("usedItems"));
        readNbt(Registry.ITEM, producedItems, nbt.getCompound("producedItems"));
        readNbt(Registry.FLUID, usedFluids, nbt.getCompound("usedFluids"));
        readNbt(Registry.FLUID, producedFluids, nbt.getCompound("producedFluids"));
    }

    public CompoundTag toTag() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("usedItems", toNbt(Registry.ITEM, usedItems));
        nbt.put("producedItems", toNbt(Registry.ITEM, producedItems));
        nbt.put("usedFluids", toNbt(Registry.FLUID, usedFluids));
        nbt.put("producedFluids", toNbt(Registry.FLUID, producedFluids));
        return nbt;
    }

    public void addUsedItems(ItemLike what, long amount) {
        usedItems.computeIfAbsent(what.asItem(), i -> new StatisticValue()).add(amount);
    }

    public void addProducedItems(ItemLike what, long amount) {
        producedItems.computeIfAbsent(what.asItem(), i -> new StatisticValue()).add(amount);
    }

    public void addUsedFluids(Fluid what, long amount) {
        usedFluids.computeIfAbsent(what, i -> new StatisticValue()).add(amount);
    }

    public void addProducedFluids(Fluid what, long amount) {
        producedFluids.computeIfAbsent(what, i -> new StatisticValue()).add(amount);
    }

    private static <T> void readNbt(Registry<T> registry, Map<T, StatisticValue> map, CompoundTag tag) {
        for (var key : tag.getAllKeys()) {
            try {
                var val = registry.get(new ResourceLocation(key));
                if (val != Items.AIR && val != Fluids.EMPTY) {
                    map.put(val, new StatisticValue(tag.getCompound(key)));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static <T> CompoundTag toNbt(Registry<T> registry, Map<T, StatisticValue> map) {
        CompoundTag tag = new CompoundTag();
        for (var entry : map.entrySet()) {
            tag.put(registry.getKey(entry.getKey()).toString(), entry.getValue().toNbt());
        }
        return tag;
    }
}
