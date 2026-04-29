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

package aztech.modern_industrialization.client.datagen;

import aztech.modern_industrialization.client.datagen.model.MIModelProvider;
import aztech.modern_industrialization.client.datagen.texture.MISpriteSourceProvider;
import aztech.modern_industrialization.client.datagen.texture.TexturesProvider;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;

public class MIDatagenClient {
    public static void configure(
            DataGenerator gen,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            boolean runtimeDatagen) {
        if (!runtimeDatagen) {
            // Runtime datagen runs before sprite sources are registered, so skip this provider
            gen.addProvider(true, new MISpriteSourceProvider(gen.getPackOutput(), lookupProvider));
        }
        gen.addProvider(true, new TexturesProvider(gen.getPackOutput(), runtimeDatagen));
        gen.addProvider(true, new MIModelProvider(gen.getPackOutput()));
    }
}
