package aztech.modern_industrialization.pipes.api;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private final int color;
    private final boolean opensGui;
    private final PipeRenderer.Factory renderer;

    private static Map<Identifier, PipeNetworkType> types = new HashMap<>();
    private static Set<PipeRenderer.Factory> pipeRenderers = new HashSet<>();
    private static int nextSerialNumber = 0;

    private PipeNetworkType(Identifier identifier, BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor, Supplier<PipeNetworkNode> nodeCtor, int color, boolean opensGui, PipeRenderer.Factory renderer, int serialNumber) {
        this.identifier = identifier;
        this.networkCtor = networkCtor;
        this.nodeCtor = nodeCtor;
        this.color = color;
        this.opensGui = opensGui;
        this.renderer = renderer;
        this.serialNumber = serialNumber;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    BiFunction<Integer, PipeNetworkData, PipeNetwork> getNetworkCtor() {
        return networkCtor;
    }

    public Supplier<PipeNetworkNode> getNodeCtor() {
        return nodeCtor;
    }

    public int getColor() {
        return color;
    }

    public boolean opensGui() {
        return opensGui;
    }

    public static PipeNetworkType get(Identifier identifier) {
        return types.get(identifier);
    }

    public static Map<Identifier, PipeNetworkType> getTypes() { return new HashMap<>(types); }

    public static PipeNetworkType register(Identifier identifier, BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor, Supplier<PipeNetworkNode> nodeCtor, int color, boolean opensGui, PipeRenderer.Factory renderer) {
        PipeNetworkType type = new PipeNetworkType(identifier, networkCtor, nodeCtor, color, opensGui, renderer, nextSerialNumber++);
        PipeNetworkType previousType = types.put(identifier, type);
        if(previousType != null) {
            throw new IllegalArgumentException("Attempting to register another PipeNetworkType with the same identifier.");
        }
        pipeRenderers.add(renderer);
        return type;
    }

    @Override
    public int compareTo(PipeNetworkType o) {
        return Integer.compare(serialNumber, o.serialNumber);
    }

    public PipeRenderer.Factory getRenderer() {
        return renderer;
    }

    public static Set<PipeRenderer.Factory> getRenderers() {
        return new HashSet<>(pipeRenderers);
    }
}
