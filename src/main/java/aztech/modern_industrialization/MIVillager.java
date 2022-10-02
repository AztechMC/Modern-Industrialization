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

import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.level.ItemLike;

public class MIVillager {
    public static final ResourceLocation ID = new MIIdentifier("industrialist");

    public static final PoiType POI_TYPE = PointOfInterestHelper.register(ID, 1, 1, MIBlock.FORGE_HAMMER.asBlock());
    public static final ResourceKey<PoiType> POI_KEY = ResourceKey.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY, ID);

    public static final VillagerProfession PROFESSION = new VillagerProfession(ID.toString(), e -> e.is(POI_KEY),
            e -> e.is(POI_KEY), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_TOOLSMITH);

    public static void init() {
        Registry.register(Registry.VILLAGER_PROFESSION, ID, PROFESSION);

        sellItems(2, MIItem.WRENCH, 3, 10, 15);
        buyItems(1, MIBlock.CREATIVE_TANK_BLOCK, 5, 10, 12);
    }

    private static void sellItems(int minLevel, ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        TradeOfferHelper.registerVillagerOffers(PROFESSION, minLevel, builder -> {
            builder.add(new VillagerTrades.EmeraldForItems(soldItem, numberOfItems, maxUses, xp));
        });
    }

    private static void buyItems(int minLevel, ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        TradeOfferHelper.registerVillagerOffers(PROFESSION, minLevel, builder -> {
            builder.add(new VillagerTrades.ItemsForEmeralds(boughtItem.asItem(), emeraldCost, numberOfItems, xp));
        });
    }
}
