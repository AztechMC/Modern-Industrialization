package aztech.modern_industrialization;

import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
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

    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MI.ID);

    public static final Supplier<MenuType<? extends MachineMenuCommon>> MACHINE_MENU = MENUS.register("machine", () -> {
        return IMenuTypeExtension.create(CommonProxy.INSTANCE::createClientMachineMenu);
    })::get;

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
        TABS.register(modBus);
    }
}
