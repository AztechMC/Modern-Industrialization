package aztech.modern_industrialization;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MICapabilities {
    private static final List<Consumer<RegisterCapabilitiesEvent>> processors = new ArrayList<>();

    public static void init(RegisterCapabilitiesEvent event) {
        for (var fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), fluid.getBucket());
        }

        processors.forEach(c -> c.accept(event));
    }

    public static void onEvent(Consumer<RegisterCapabilitiesEvent> consumer) {
        processors.add(consumer);
    }
}
