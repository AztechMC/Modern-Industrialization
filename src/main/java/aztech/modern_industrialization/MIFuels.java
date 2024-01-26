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
package aztech.modern_industrialization;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;

public class MIFuels {
    private static final Map<Item, Integer> fuels = new IdentityHashMap<>();
    private static final TagKey<Item> COAL_DUSTS = ItemTags.create(new ResourceLocation("forge:dusts/coal"));

    /**
     * Call in common setup!
     */
    public static void init() {
        addFuel("coke", 6400);
        addFuel("coke_dust", 6400);
        addFuel("coke_block", Short.MAX_VALUE); // F*** YOU VANILLA ! (Should be 6400*9 but it overflows ...)
        addFuel("coal_crushed_dust", 1600);
        addFuel("coal_tiny_dust", 160);
        addFuel("lignite_coal", 1600);
        addFuel("lignite_coal_block", 16000);
        addFuel("lignite_coal_crushed_dust", 1600);
        addFuel("lignite_coal_dust", 1600);
        addFuel("lignite_coal_tiny_dust", 160);
        addFuel("carbon_dust", 6400);
        addFuel("carbon_tiny_dust", 640);

        NeoForge.EVENT_BUS.addListener(FurnaceFuelBurnTimeEvent.class, event -> {
            var burnTime = fuels.get(event.getItemStack().getItem());
            if (burnTime != null) {
                event.setBurnTime(burnTime);
            }

            if (event.getItemStack().is(COAL_DUSTS)) {
                event.setBurnTime(1600);
            }
        });
    }

    private static void addFuel(String id, int burnTicks) {
        Item item = BuiltInRegistries.ITEM.get(new MIIdentifier(id));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Couldn't find item " + id);
        }
        fuels.put(item, burnTicks);
    }
}
