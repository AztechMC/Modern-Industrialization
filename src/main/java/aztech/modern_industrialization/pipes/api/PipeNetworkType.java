package aztech.modern_industrialization.pipes.api;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * A pipe network type.
 */
public final class PipeNetworkType implements Comparable<PipeNetworkType> {
    private final Identifier identifier;
    private final BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor;
    private final Supplier<PipeNetworkNode> nodeCtor;
    /**
     * A "serial number" allowing type comparison for rendering.
     */
    private final int serialNumber;
    private static Map<Identifier, PipeNetworkType> types = new HashMap<>();
    private static int nextSerialNumber = 0;

    private PipeNetworkType(Identifier identifier, BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor, Supplier<PipeNetworkNode> nodeCtor, int serialNumber) {
        this.identifier = identifier;
        this.networkCtor = networkCtor;
        this.nodeCtor = nodeCtor;
        this.serialNumber = serialNumber;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    BiFunction<Integer, PipeNetworkData, PipeNetwork> getNetworkCtor() {
        return networkCtor;
    }

    Supplier<PipeNetworkNode> getNodeCtor() {
        return nodeCtor;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public static Map<Identifier, PipeNetworkType> getTypes() {
        return types;
    }

    public static PipeNetworkType register(Identifier identifier, BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor, Supplier<PipeNetworkNode> nodeCtor) {
        PipeNetworkType type = new PipeNetworkType(identifier, networkCtor, nodeCtor, nextSerialNumber++);
        PipeNetworkType previousType = types.put(identifier, type);
        if(previousType != null) {
            throw new IllegalArgumentException("Attempting to register another PipeNetworkType with the same identifier.");
        }
        return type;
    }

    @Override
    public int compareTo(PipeNetworkType o) {
        return Integer.compare(serialNumber, o.serialNumber);
    }
}
