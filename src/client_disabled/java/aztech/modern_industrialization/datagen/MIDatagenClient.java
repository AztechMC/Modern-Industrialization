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

import aztech.modern_industrialization.datagen.model.MachineModelsProvider;
import aztech.modern_industrialization.datagen.model.ModelProvider;
import aztech.modern_industrialization.datagen.texture.SpriteSourceProvider;
import aztech.modern_industrialization.datagen.texture.TexturesProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

public class MIDatagenClient {
    public static void configure(FabricDataGenerator.Pack pack, boolean runtimeDatagen) {
        var aggregate = pack.addProvider(AggregateDataProvider.create("Client Resources"));

        aggregate.addProvider(MachineModelsProvider::new);
        aggregate.addProvider(ModelProvider::new);
        aggregate.addProvider(SpriteSourceProvider::new);

        pack.addProvider((FabricDataOutput packOutput) -> new TexturesProvider(packOutput, runtimeDatagen));
    }
}
