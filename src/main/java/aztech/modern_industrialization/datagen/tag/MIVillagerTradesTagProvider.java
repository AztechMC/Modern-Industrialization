package aztech.modern_industrialization.datagen.tag;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.trading.MITradeTags;
import aztech.modern_industrialization.trading.MITrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.trading.VillagerTrade;

import java.util.concurrent.CompletableFuture;

public class MIVillagerTradesTagProvider extends TagsProvider<VillagerTrade> {
    public MIVillagerTradesTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.VILLAGER_TRADE, lookupProvider, MI.ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(MITradeTags.INDUSTRIALIST_LEVEL_1)
                .add(MITrades.INDUSTRIALIST_1_LIGNITE_EMERALD)
                .add(MITrades.INDUSTRIALIST_1_EMERALD_FIRE_CLAY)
                .add(MITrades.INDUSTRIALIST_1_EMERALD_STEEL_HAMMER)
                .add(MITrades.INDUSTRIALIST_1_EMERALD_COPPER)
                .add(MITrades.INDUSTRIALIST_1_EMERALD_TIN);

        tag(MITradeTags.INDUSTRIALIST_LEVEL_2)
                .add(MITrades.INDUSTRIALIST_2_EMERALD_COPPER_GEAR)
                .add(MITrades.INDUSTRIALIST_2_EMERALD_COPPER_ROTOR)
                .add(MITrades.INDUSTRIALIST_2_EMERALD_BRONZE_INGOT)
                .add(MITrades.INDUSTRIALIST_2_EMERALD_RUBBER_SHEET)
                .add(MITrades.INDUSTRIALIST_2_SULFUR_DUST_EMERALD);

        tag(MITradeTags.INDUSTRIALIST_LEVEL_3)
                .add(MITrades.INDUSTRIALIST_3_EMERALD_BRONZE_GEAR)
                .add(MITrades.INDUSTRIALIST_3_EMERALD_BRONZE_ROTOR)
                .add(MITrades.INDUSTRIALIST_3_EMERALD_STEEL_INGOT)
                .add(MITrades.INDUSTRIALIST_3_ITEM_PIPE_EMERALD)
                .add(MITrades.INDUSTRIALIST_3_FLUID_PIPE_EMERALD);

        tag(MITradeTags.INDUSTRIALIST_LEVEL_4)
                .add(MITrades.INDUSTRIALIST_4_EMERALD_STEEL_GEAR)
                .add(MITrades.INDUSTRIALIST_4_EMERALD_STEEL_PLATE)
                .add(MITrades.INDUSTRIALIST_4_EMERALD_STEEL_UPGRADE)
                .add(MITrades.INDUSTRIALIST_4_COPPER_CABLE_EMERALD)
                .add(MITrades.INDUSTRIALIST_4_TIN_CABLE_EMERALD);

        tag(MITradeTags.INDUSTRIALIST_LEVEL_5)
                .add(MITrades.INDUSTRIALIST_5_EMERALD_ANALOG_CIRCUIT)
                .add(MITrades.INDUSTRIALIST_5_EMERALD_MOTOR)
                .add(MITrades.INDUSTRIALIST_5_EMERALD_BRONZE_DRILL);
    }
}
