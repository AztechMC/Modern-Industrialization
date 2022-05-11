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
package aztech.modern_industrialization.datagen.translation;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.datagen.MIDatagenEntrypoint;
import aztech.modern_industrialization.definition.ItemDefinition;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import oshi.util.tuples.Pair;

public record TranslationProvider(FabricDataGenerator gen) implements DataProvider {

    @Override
    public void run(HashCache cache) throws IOException {
        String langPathClean = "assets/modern_industrialization/lang/en_us_clean.json";
        String langPathConflict = "assets/modern_industrialization/lang/en_us_conflict.json";
        String langPathAll = "assets/modern_industrialization/lang/en_us_all.json";

        Path outputPath = gen.getOutputFolder();

        TreeMap<String, String> translation = MIDatagenEntrypoint.GSON
                .fromJson(new FileReader("../../src/main/resources/assets/modern_industrialization/lang/en_us.json"), TreeMap.class);

        TreeMap<String, String> translationClean = (TreeMap<String, String>) translation.clone();
        TreeMap<String, String> translationAll = (TreeMap<String, String>) translation.clone();
        TreeMap<String, String> translationConflict = new TreeMap<>();

        List<Pair<String, String>> translationsPair = new ArrayList<>();

        for (ItemDefinition<?> itemDefinition : MIItem.ITEMS.values()) {
            translationsPair.add(new Pair<>(itemDefinition.getTranslationKey(), itemDefinition.getEnglishName()));
        }

        for (Pair<String, String> translationPair : translationsPair) {
            if (translation.containsKey(translationPair.getA())) {
                if (translationPair.getB().equals(translation.get(translationPair.getA()))) {
                    translationClean.remove(translationPair.getA());
                } else {
                    translationConflict.put(translationPair.getA(), translationPair.getB());
                }
            } else {
                translationAll.put(translationPair.getA(), translationPair.getB());
            }
        }

        DataProvider.save(MIDatagenEntrypoint.GSON, cache, MIDatagenEntrypoint.GSON.toJsonTree(translationClean), outputPath.resolve(langPathClean));
        DataProvider.save(MIDatagenEntrypoint.GSON, cache, MIDatagenEntrypoint.GSON.toJsonTree(translationConflict),
                outputPath.resolve(langPathConflict));
        DataProvider.save(MIDatagenEntrypoint.GSON, cache, MIDatagenEntrypoint.GSON.toJsonTree(translationAll), outputPath.resolve(langPathAll));

    }

    @Override
    public String getName() {
        return "Translation Provider";
    }
}
