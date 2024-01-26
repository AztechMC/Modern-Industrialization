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
package aztech.modern_industrialization.datagen.dynreg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;

public final class DynamicRegistryDatagen {
    private static final List<Runnable> actions = new ArrayList<>();
    private static final Map<ResourceKey<? extends Registry<?>>, List<RegistrySetBuilder.RegistryBootstrap<?>>> entries = new LinkedHashMap<>();

    public static void addAction(Runnable action) {
        actions.add(action);
    }

    public static <T> void add(ResourceKey<? extends Registry<T>> registry, RegistrySetBuilder.RegistryBootstrap<T> bootstrap) {
        entries.computeIfAbsent(registry, k -> new ArrayList<>()).add(bootstrap);
    }

    public static RegistrySetBuilder getBuilder() {
        var builder = new RegistrySetBuilder();
        actions.forEach(Runnable::run);
        for (var entry : entries.entrySet()) {
            builder.add((ResourceKey) entry.getKey(), ctx -> {
                for (var bootstrap : entry.getValue()) {
                    bootstrap.run((BootstapContext) ctx);
                }
            });
        }
        return builder;
    }

    private DynamicRegistryDatagen() {
    }
}
