package aztech.modern_industrialization;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerRecipe;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.Comparator;
import java.util.function.Supplier;

/**
 * Misc registry objects.
 */
public class MIRegistries {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MI.ID);

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MI.ID);

    public static final Supplier<MenuType<? extends ForgeHammerScreenHandler>> FORGE_HAMMER_MENU = MENUS.register("forge_hammer", () -> {
        return new MenuType<>(ForgeHammerScreenHandler::new, FeatureFlags.VANILLA_SET);
    })::get;
    public static final Supplier<MenuType<? extends MachineMenuCommon>> MACHINE_MENU = MENUS.register("machine", () -> {
        return IMenuTypeExtension.create(CommonProxy.INSTANCE::createClientMachineMenu);
    })::get;

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MI.ID);

    public static final Supplier<RecipeSerializer<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register("forge_hammer", ForgeHammerRecipe.Serializer::new);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MI.ID);

    public static final Supplier<RecipeType<ForgeHammerRecipe>> FORGE_HAMMER_RECIPE_TYPE = RECIPE_TYPES.register("forge_hammer", () -> RecipeType.simple(MI.id("forge_hammer")));

    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MI.ID);

    private static final Supplier<CreativeModeTab> TAB = TABS.register("general", () -> CreativeModeTab.builder()
            .title(MIText.ModernIndustrialization.text())
            .icon(() -> Items.IRON_BLOCK.getDefaultInstance())
            // TODO NEO restore forge hammer tab icon
//            .icon(() -> MIBlock.FORGE_HAMMER.asItem().getDefaultInstance())
            .displayItems((params, output) -> {
                MIItem.ITEM_DEFINITIONS.values().stream()
                        .sorted(Comparator.comparing(e -> e.sortOrder))
                        .forEach(output::accept);
            })
            .build());

    static void init(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        RECIPE_TYPES.register(modBus);
        TABS.register(modBus);
    }
}
