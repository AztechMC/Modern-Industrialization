package aztech.modern_industrialization;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.ILongEnergyStorage;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.blocks.storage.tank.TankItem;
import aztech.modern_industrialization.items.ContainerItem;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotFluidHandler;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotItemHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class MICapabilities {
    private static final List<Consumer<RegisterCapabilitiesEvent>> processors = new ArrayList<>();

    public static void init(RegisterCapabilitiesEvent event) {
        // Fluids
        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), fluid.getBucket());
        }

        // Delayed processors
        processors.forEach(c -> c.accept(event));

        // Misc
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MIRegistries.CREATIVE_BARREL_BE.get(), (be, side) -> new SlotItemHandler(be));
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MIRegistries.CREATIVE_TANK_BE.get(), (be, side) -> new SlotFluidHandler(be));
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, MIRegistries.CREATIVE_STORAGE_UNIT_BE.get(), (be, side) -> EnergyApi.CREATIVE);

        // Energy compat
        var allBlocks = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                .toArray(Block[]::new);
        var allItems = StreamSupport.stream(BuiltInRegistries.ITEM.spliterator(), false)
                .toArray(Item[]::new);
        ILongEnergyStorage.init(event, allBlocks, allItems);
        EnergyApi.init(event, allBlocks, allItems);
    }

    public static void onEvent(Consumer<RegisterCapabilitiesEvent> consumer) {
        processors.add(consumer);
    }
}
