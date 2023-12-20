package aztech.modern_industrialization;

import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

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

    static void init(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
    }
}
