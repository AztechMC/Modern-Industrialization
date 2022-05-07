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
package aztech.modern_industrialization.datagen.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

public class MachineModelsProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final FabricDataGenerator gen;

    public MachineModelsProvider(FabricDataGenerator gen) {
        this.gen = gen;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        Path outputPath = gen.getOutputFolder();
        Path nonGeneratedPath = gen.getOutputFolder().resolve("../../main/resources");

        for (var entry : MachineModelsToGenerate.PROPS.entrySet()) {
            var modelPath = "assets/%s/models/machine/%s.json".formatted(gen.getModId(), entry.getKey());
            if (!Files.exists(nonGeneratedPath.resolve(modelPath))) {
                // Only generate the model json if it doesn't exist in the non-generated assets.
                DataProvider.save(GSON, cache, entry.getValue().toMachineJson(), outputPath.resolve(modelPath));
            }
        }
    }

    @Override
    public String getName() {
        return "Machine Models Provider";
    }
}
