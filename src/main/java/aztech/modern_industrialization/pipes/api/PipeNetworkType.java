/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package aztech.modern_industrialization.pipes.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * A pipe network type.
 */
public final class PipeNetworkType implements Comparable<PipeNetworkType> {
    private final ResourceLocation identifier;
    private final BiFunction<Integer, @Nullable PipeNetworkData, PipeNetwork> networkCtor;
    private final Supplier<PipeNetworkNode> nodeCtor;
    /**
     * A "serial number" allowing type comparison for rendering.
     */
    private final int serialNumber;
    private final int color;
    private final boolean opensGui;
    public Object renderer;

    private static final Map<ResourceLocation, PipeNetworkType> types = new HashMap<>();
    private static int nextSerialNumber = 0;

    private PipeNetworkType(ResourceLocation identifier, BiFunction<Integer, @Nullable PipeNetworkData, PipeNetwork> networkCtor,
            Supplier<PipeNetworkNode> nodeCtor, int color, boolean opensGui, int serialNumber) {
        this.identifier = identifier;
        this.networkCtor = networkCtor;
        this.nodeCtor = nodeCtor;
        this.color = color;
        this.opensGui = opensGui;
        this.serialNumber = serialNumber;
    }

    public ResourceLocation getIdentifier() {
        return identifier;
    }

    BiFunction<Integer, @Nullable PipeNetworkData, PipeNetwork> getNetworkCtor() {
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

    @Nullable
    public static PipeNetworkType get(ResourceLocation identifier) {
        return types.get(identifier);
    }

    public static Map<ResourceLocation, PipeNetworkType> getTypes() {
        return new HashMap<>(types);
    }

    public static PipeNetworkType register(ResourceLocation identifier, BiFunction<Integer, PipeNetworkData, PipeNetwork> networkCtor,
            Supplier<PipeNetworkNode> nodeCtor, int color, boolean opensGui) {
        color |= 0xff000000;
        PipeNetworkType type = new PipeNetworkType(identifier, networkCtor, nodeCtor, color, opensGui, nextSerialNumber++);
        PipeNetworkType previousType = types.put(identifier, type);
        if (previousType != null) {
            throw new IllegalArgumentException("Attempting to register another PipeNetworkType with the same identifier.");
        }
        return type;
    }

    @Override
    public int compareTo(PipeNetworkType o) {
        return Integer.compare(serialNumber, o.serialNumber);
    }
}
