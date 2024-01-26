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

import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

public class MIVillager {
    public static void init(VillagerTradesEvent event) {
        if (event.getType() != MIRegistries.INDUSTRIALIST.get()) {
            return;
        }

        if (MIConfig.getConfig().removeIndustrialistTrades)
            return;

        var level1 = event.getTrades().get(1);
        level1.add(sellItemsToVillager(MIMaterials.LIGNITE_COAL.getPart(MIParts.GEM).asItem(), 15, 16, 2));
        level1.add(buyItemsFromVillager(MIMaterials.FIRE_CLAY.getPart(MIParts.INGOT), 2, 6, 2));
        level1.add(buyItemsFromVillager(MIItem.STEEL_HAMMER, 8, 1, 10));
        level1.add(buyItemsFromVillager(Items.COPPER_INGOT, 4, 8, 5));
        level1.add(buyItemsFromVillager(MIMaterials.TIN.getPart(MIParts.INGOT), 4, 3, 5));

        var level2 = event.getTrades().get(2);
        level2.add(buyItemsFromVillager(MIMaterials.COPPER.getPart(MIParts.GEAR), 4, 1, 5));
        level2.add(buyItemsFromVillager(MIMaterials.COPPER.getPart(MIParts.ROTOR), 4, 1, 5));
        level2.add(buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.INGOT), 4, 3, 2));
        level2.add(buyItemsFromVillager(MIItem.RUBBER_SHEET, 1, 6, 2));
        level2.add(sellItemsToVillager(MIMaterials.SULFUR.getPart(MIParts.DUST), 4, 16, 2));

        var level3 = event.getTrades().get(3);
        level3.add(buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.GEAR), 4, 1, 5));
        level3.add(buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.ROTOR), 4, 1, 5));
        level3.add(buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.INGOT), 6, 3, 10));
        level3.add(sellItemsToVillager(BuiltInRegistries.ITEM.get(new MIIdentifier("item_pipe")), 4, 20, 10));
        level3.add(sellItemsToVillager(BuiltInRegistries.ITEM.get(new MIIdentifier("fluid_pipe")), 4, 20, 10));

        var level4 = event.getTrades().get(4);
        level4.add(buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.GEAR), 5, 1, 5));
        level4.add(buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.PLATE), 6, 3, 10));
        level4.add(buyItemsFromVillager(MIItem.STEEL_UPGRADE, 20, 1, 20));
        level4.add(sellItemsToVillager(MIMaterials.TIN.getPart(MIParts.CABLE), 8, 16, 5));
        level4.add(sellItemsToVillager(MIMaterials.COPPER.getPart(MIParts.CABLE), 8, 16, 5));

        var level5 = event.getTrades().get(5);
        level5.add(buyItemsFromVillager(MIItem.ANALOG_CIRCUIT, 12, 1, 20));
        level5.add(buyItemsFromVillager(MIItem.MOTOR, 8, 2, 10));
        level5.add(buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.DRILL), 18, 4, 20));

    }

    private static VillagerTrades.ItemListing sellItemsToVillager(ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        return new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp);
    }

    private static VillagerTrades.ItemListing buyItemsFromVillager(ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        return new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp);
    }
}
