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
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class MIVillager {
    public static final ResourceLocation ID = new MIIdentifier("industrialist");

    public static final PoiType POI_TYPE = PointOfInterestHelper.register(ID, 1, 1, MIBlock.FORGE_HAMMER.asBlock());
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ID);

    public static final VillagerProfession PROFESSION = new VillagerProfession(ID.toString(), e -> e.is(POI_KEY),
            e -> e.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_TOOLSMITH);

    public static void init() {
        Registry.register(BuiltInRegistries.VILLAGER_PROFESSION, ID, PROFESSION);

        if (MIConfig.getConfig().removeIndustrialistTrades)
            return;

        sellItemsToVillager(1, MIMaterials.LIGNITE_COAL.getPart(MIParts.GEM).asItem(), 15,
                16, 2);
        buyItemsFromVillager(1, MIMaterials.FIRE_CLAY.getPart(MIParts.INGOT), 2, 6, 2);
        buyItemsFromVillager(1, MIItem.STEEL_HAMMER, 8, 1, 10);
        buyItemsFromVillager(1, Items.COPPER_INGOT, 4, 8, 5);
        buyItemsFromVillager(1, MIMaterials.TIN.getPart(MIParts.INGOT), 4, 3, 5);

        buyItemsFromVillager(2, MIMaterials.COPPER.getPart(MIParts.GEAR), 4, 1, 5);
        buyItemsFromVillager(2, MIMaterials.COPPER.getPart(MIParts.ROTOR), 4, 1, 5);
        buyItemsFromVillager(2, MIMaterials.BRONZE.getPart(MIParts.INGOT), 4, 3, 2);
        buyItemsFromVillager(2, MIItem.RUBBER_SHEET, 1, 6, 2);
        sellItemsToVillager(2, MIMaterials.SULFUR.getPart(MIParts.DUST), 4, 16, 2);

        buyItemsFromVillager(3, MIMaterials.BRONZE.getPart(MIParts.GEAR), 4, 1, 5);
        buyItemsFromVillager(3, MIMaterials.BRONZE.getPart(MIParts.ROTOR), 4, 1, 5);
        buyItemsFromVillager(3, MIMaterials.STEEL.getPart(MIParts.INGOT), 6, 3, 10);
        sellItemsToVillager(3, BuiltInRegistries.ITEM.get(new MIIdentifier("item_pipe")), 4, 20, 10);
        sellItemsToVillager(3, BuiltInRegistries.ITEM.get(new MIIdentifier("fluid_pipe")), 4, 20, 10);

        buyItemsFromVillager(4, MIMaterials.STEEL.getPart(MIParts.GEAR), 5, 1, 5);
        buyItemsFromVillager(4, MIMaterials.STEEL.getPart(MIParts.PLATE), 6, 3, 10);
        buyItemsFromVillager(4, MIItem.STEEL_UPGRADE, 20, 1, 20);
        sellItemsToVillager(4, MIMaterials.TIN.getPart(MIParts.CABLE), 8, 16, 5);
        sellItemsToVillager(4, MIMaterials.COPPER.getPart(MIParts.CABLE), 8, 16, 5);

        buyItemsFromVillager(5, MIItem.ANALOG_CIRCUIT, 12, 1, 20);
        buyItemsFromVillager(5, MIItem.MOTOR, 8, 2, 10);
        buyItemsFromVillager(5, MIMaterials.BRONZE.getPart(MIParts.DRILL), 18, 4, 20);

    }

    private static void sellItemsToVillager(int minLevel, ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        TradeOfferHelper.registerVillagerOffers(PROFESSION, minLevel, builder -> {
            builder.add(new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp));
        });
    }

    private static void buyItemsFromVillager(int minLevel, ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        TradeOfferHelper.registerVillagerOffers(PROFESSION, minLevel, builder -> {
            builder.add(new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp));
        });
    }
}
