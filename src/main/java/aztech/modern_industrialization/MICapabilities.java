package aztech.modern_industrialization;

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.ILongEnergyStorage;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import net.minecraft.core.registries.BuiltInRegistries;
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
        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), fluid.getBucket());
        }

        processors.forEach(c -> c.accept(event));

        var allBlocks = StreamSupport.stream(BuiltInRegistries.BLOCK.spliterator(), false)
                .toArray(Block[]::new);
        ILongEnergyStorage.init(event, allBlocks);
        EnergyApi.init(event, allBlocks);
    }

    public static void onEvent(Consumer<RegisterCapabilitiesEvent> consumer) {
        processors.add(consumer);
    }
}
