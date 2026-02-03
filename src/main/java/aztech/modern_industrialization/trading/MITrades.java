package aztech.modern_industrialization.trading;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.MIParts;
import net.minecraft.commands.arguments.item.ComponentPredicateParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.Optional;

public class MITrades {
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_1_LIGNITE_EMERALD = resourceKey(1, "lignite_emerald");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_1_EMERALD_FIRE_CLAY = resourceKey(1, "emerald_fire_clay");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_1_EMERALD_STEEL_HAMMER = resourceKey(1, "emerald_steel_hammer");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_1_EMERALD_COPPER = resourceKey(1, "emerald_copper");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_1_EMERALD_TIN = resourceKey(1, "emerald_tin");

    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_2_EMERALD_COPPER_GEAR = resourceKey(2, "emerald_copper_gear");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_2_EMERALD_COPPER_ROTOR = resourceKey(2, "emerald_copper_rotor");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_2_EMERALD_BRONZE_INGOT = resourceKey(2, "emerald_bronze_ingot");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_2_EMERALD_RUBBER_SHEET = resourceKey(2, "emerald_rubber_sheet");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_2_SULFUR_DUST_EMERALD = resourceKey(2, "sulfur_dust_emerald");

    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_3_EMERALD_BRONZE_GEAR = resourceKey(3, "emerald_bronze_gear");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_3_EMERALD_BRONZE_ROTOR = resourceKey(3, "emerald_bronze_rotor");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_3_EMERALD_STEEL_INGOT = resourceKey(3, "emerald_steel_ingot");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_3_ITEM_PIPE_EMERALD = resourceKey(3, "item_pipe_emerald");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_3_FLUID_PIPE_EMERALD = resourceKey(3, "fluid_pipe_emerald");

    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_4_EMERALD_STEEL_GEAR = resourceKey(4, "emerald_steel_gear");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_4_EMERALD_STEEL_PLATE = resourceKey(4, "emerald_steel_plate");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_4_EMERALD_STEEL_UPGRADE = resourceKey(4, "emerald_steel_upgrade");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_4_COPPER_CABLE_EMERALD = resourceKey(4, "copper_cable_emerald");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_4_TIN_CABLE_EMERALD = resourceKey(4, "tin_cable_emerald");

    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_5_EMERALD_ANALOG_CIRCUIT = resourceKey(5, "emerald_analog_circuit");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_5_EMERALD_MOTOR = resourceKey(5, "emerald_motor");
    public static final ResourceKey<VillagerTrade> INDUSTRIALIST_5_EMERALD_BRONZE_DRILL = resourceKey(5, "emerald_bronze_drill");

    private static ResourceKey<VillagerTrade> resourceKey(int level, String tradeName) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, MI.id("industrialist/%d/%s".formatted(level, tradeName)));
    }

    public static void bootstrap(BootstrapContext<VillagerTrade> context) {
        context.register(INDUSTRIALIST_1_LIGNITE_EMERALD, sellItemsToVillager(MIMaterials.LIGNITE_COAL.getPart(MIParts.GEM).asItem(), 15, 16, 2));
        context.register(INDUSTRIALIST_1_EMERALD_FIRE_CLAY, buyItemsFromVillager(MIMaterials.FIRE_CLAY.getPart(MIParts.INGOT), 2, 6, 2));
        context.register(INDUSTRIALIST_1_EMERALD_STEEL_HAMMER, buyItemsFromVillager(MIItem.STEEL_HAMMER, 8, 1, 10));
        context.register(INDUSTRIALIST_1_EMERALD_COPPER, buyItemsFromVillager(Items.COPPER_INGOT, 4, 8, 5));
        context.register(INDUSTRIALIST_1_EMERALD_TIN, buyItemsFromVillager(MIMaterials.TIN.getPart(MIParts.INGOT), 4, 3, 5));

        context.register(INDUSTRIALIST_2_EMERALD_COPPER_GEAR, buyItemsFromVillager(MIMaterials.COPPER.getPart(MIParts.GEAR), 4, 1, 5));
        context.register(INDUSTRIALIST_2_EMERALD_COPPER_ROTOR, buyItemsFromVillager(MIMaterials.COPPER.getPart(MIParts.ROTOR), 4, 1, 5));
        context.register(INDUSTRIALIST_2_EMERALD_BRONZE_INGOT, buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.INGOT), 4, 3, 2));
        context.register(INDUSTRIALIST_2_EMERALD_RUBBER_SHEET, buyItemsFromVillager(MIItem.RUBBER_SHEET, 1, 6, 2));
        context.register(INDUSTRIALIST_2_SULFUR_DUST_EMERALD, sellItemsToVillager(MIMaterials.SULFUR.getPart(MIParts.DUST), 4, 16, 2));

        context.register(INDUSTRIALIST_3_EMERALD_BRONZE_GEAR, buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.GEAR), 4, 1, 5));
        context.register(INDUSTRIALIST_3_EMERALD_BRONZE_ROTOR, buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.ROTOR), 4, 1, 5));
        context.register(INDUSTRIALIST_3_EMERALD_STEEL_INGOT, buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.INGOT), 6, 3, 10));
        context.register(INDUSTRIALIST_3_ITEM_PIPE_EMERALD, sellItemsToVillager(BuiltInRegistries.ITEM.getValue(MI.id("item_pipe")), 4, 20, 10));
        context.register(INDUSTRIALIST_3_FLUID_PIPE_EMERALD, sellItemsToVillager(BuiltInRegistries.ITEM.getValue(MI.id("fluid_pipe")), 4, 20, 10));

        context.register(INDUSTRIALIST_4_EMERALD_STEEL_GEAR, buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.GEAR), 5, 1, 5));
        context.register(INDUSTRIALIST_4_EMERALD_STEEL_PLATE, buyItemsFromVillager(MIMaterials.STEEL.getPart(MIParts.PLATE), 6, 3, 10));
        context.register(INDUSTRIALIST_4_EMERALD_STEEL_UPGRADE, buyItemsFromVillager(MIItem.STEEL_UPGRADE, 20, 1, 20));
        context.register(INDUSTRIALIST_4_TIN_CABLE_EMERALD, sellItemsToVillager(MIMaterials.TIN.getPart(MIParts.CABLE), 8, 16, 5));
        context.register(INDUSTRIALIST_4_COPPER_CABLE_EMERALD, sellItemsToVillager(MIMaterials.COPPER.getPart(MIParts.CABLE), 8, 16, 5));

        context.register(INDUSTRIALIST_5_EMERALD_ANALOG_CIRCUIT, buyItemsFromVillager(MIItem.ANALOG_CIRCUIT, 12, 1, 20));
        context.register(INDUSTRIALIST_5_EMERALD_MOTOR, buyItemsFromVillager(MIItem.MOTOR, 8, 2, 10));
        context.register(INDUSTRIALIST_5_EMERALD_BRONZE_DRILL, buyItemsFromVillager(MIMaterials.BRONZE.getPart(MIParts.DRILL), 18, 4, 20));
    }

    private static VillagerTrade sellItemsToVillager(ItemLike soldItem, int numberOfItems, int maxUses, int xp) {
        return new VillagerTrade(
                new TradeCost(soldItem.asItem(), numberOfItems),
                new ItemStackTemplate(Items.EMERALD),
                maxUses, xp, 0.05F, Optional.empty(), List.of());
    }

    private static VillagerTrade buyItemsFromVillager(ItemLike boughtItem, int emeraldCost, int numberOfItems, int xp) {
        return new VillagerTrade(
                new TradeCost(Items.EMERALD, emeraldCost),
                new ItemStackTemplate(boughtItem.asItem(), numberOfItems),
                12, xp, 0.05F, Optional.empty(), List.of());
    }
}
