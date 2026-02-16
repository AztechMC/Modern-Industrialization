package aztech.modern_industrialization.trading;

import aztech.modern_industrialization.MI;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;

public class MITradeSets {
    public static final ResourceKey<TradeSet> INDUSTRIALIST_LEVEL_1 = resourceKey("industrialist/level_1");
    public static final ResourceKey<TradeSet> INDUSTRIALIST_LEVEL_2 = resourceKey("industrialist/level_2");
    public static final ResourceKey<TradeSet> INDUSTRIALIST_LEVEL_3 = resourceKey("industrialist/level_3");
    public static final ResourceKey<TradeSet> INDUSTRIALIST_LEVEL_4 = resourceKey("industrialist/level_4");
    public static final ResourceKey<TradeSet> INDUSTRIALIST_LEVEL_5 = resourceKey("industrialist/level_5");

    public static void bootstrap(BootstrapContext<TradeSet> context) {
        TradeSets.register(context, INDUSTRIALIST_LEVEL_1, MITradeTags.INDUSTRIALIST_LEVEL_1);
        TradeSets.register(context, INDUSTRIALIST_LEVEL_2, MITradeTags.INDUSTRIALIST_LEVEL_2);
        TradeSets.register(context, INDUSTRIALIST_LEVEL_3, MITradeTags.INDUSTRIALIST_LEVEL_3);
        TradeSets.register(context, INDUSTRIALIST_LEVEL_4, MITradeTags.INDUSTRIALIST_LEVEL_4);
        TradeSets.register(context, INDUSTRIALIST_LEVEL_5, MITradeTags.INDUSTRIALIST_LEVEL_5);
    }

    private static ResourceKey<TradeSet> resourceKey(String path) {
        return ResourceKey.create(Registries.TRADE_SET, MI.id(path));
    }
}
