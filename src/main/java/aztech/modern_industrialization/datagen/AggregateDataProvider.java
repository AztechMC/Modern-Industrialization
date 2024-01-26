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
package aztech.modern_industrialization.datagen;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.slf4j.Logger;

public class AggregateDataProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackOutput packOutput;
    private final String name;
    private final List<DataProvider> providers = new ArrayList<>();

    public AggregateDataProvider(PackOutput packOutput, String name) {
        this.packOutput = packOutput;
        this.name = name;
    }

    private <T extends DataProvider> T addProvider(T provider) {
        providers.add(provider);
        return provider;
    }

    public <T extends DataProvider> T addProvider(Function<PackOutput, T> providerConstructor) {
        return addProvider(providerConstructor.apply(packOutput));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        LOGGER.info("Will run the following providers in parallel:" +
                providers.stream().map(provider -> "\n - Modern Industrialization/%s".formatted(provider.getName())).collect(Collectors.joining()));
        return CompletableFuture.allOf(providers.stream().map(provider -> provider.run(output)).toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return name + " (aggregated)";
    }
}
