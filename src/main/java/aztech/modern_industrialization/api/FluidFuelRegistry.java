package aztech.modern_industrialization.api;

import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import java.util.*;

public class FluidFuelRegistry {
    private static final Map<FluidKey, Integer> fluidBurnTicks = new HashMap<>();

    public static void register(FluidKey fluid, int burnTicks) {
        if (burnTicks <= 0) {
            throw new RuntimeException("Fluids must have a positive burn time!");
        }
        if (fluid == null || fluid.isEmpty()) {
            throw new RuntimeException("May not register a null or empty fluid!");
        }
        if (fluidBurnTicks.containsKey(fluid)) {
            throw new RuntimeException("May not re-register a fluid fuel!");
        }
        fluidBurnTicks.put(fluid, burnTicks);
    }

    /**
     * Get the burn time of a fluid, or 0 if the fluid is not a registered fuel.
     */
    public static int getBurnTicks(FluidKey fluid) {
        return fluidBurnTicks.getOrDefault(fluid, 0);
    }

    public static List<FluidKey> getRegisteredFluids() {
        List<FluidKey> fluids = new ArrayList<>(fluidBurnTicks.keySet());
        fluids.sort(Comparator.comparing(fluidBurnTicks::get));
        return fluids;
    }
}
