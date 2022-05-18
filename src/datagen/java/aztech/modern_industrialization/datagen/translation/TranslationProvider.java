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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.definition.Definition;
import aztech.modern_industrialization.pipes.MIPipes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Pair;

public record TranslationProvider(FabricDataGenerator gen) implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String OUTPUT_PATH = "assets/modern_industrialization/lang/en_us.json";
    private static final String INPUT_PATH = "../../src/main/resources/assets/modern_industrialization/lang/en_us_not_generated.json";

    @Override
    public void run(@NotNull HashCache cache) throws IOException {

        Path outputPath = gen.getOutputFolder();

        TreeMap<String, String> translation = GSON.fromJson(new FileReader(INPUT_PATH, StandardCharsets.UTF_8), TreeMap.class);

        List<Pair<String, String>> translationsPair = new ArrayList<>();

        for (var entry : MIText.values()) {
            translationsPair.add(new Pair<>(entry.getTranslationKey(), entry.getEnglishText()));
            for (String additionalKey : entry.getAdditionalTranslationKey()) {
                translationsPair.add(new Pair<>(additionalKey, entry.getEnglishText()));
            }
        }

        for (Definition definition : Definition.TRANSLATABLE_DEFINITION) {
            translationsPair.add(new Pair<>(definition.getTranslationKey(), definition.getEnglishName()));
        }

        for (var entry : MIPipes.TRANSLATION.entrySet()) {
            translationsPair.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        for (Pair<String, String> translationPair : translationsPair) {
            String key = translationPair.getA();
            String value = translationPair.getB();

            if (translation.containsKey(key)) {
                ModernIndustrialization.LOGGER.warn("Warning duplicate translation " + key + " : " + value + "/" + translation.get(key));
            }

            translation.put(key, value);

        }

        save(cache, GSON.toJsonTree(translation), outputPath.resolve(OUTPUT_PATH));

    }

    private void save(HashCache cache, JsonElement jsonElement, Path path) throws IOException {

        String sortedJson = GSON.toJson(jsonElement);
        String prettyPrinted = sortedJson.replace("\\u0027", "'");

        String string2 = SHA1.hashUnencodedChars(prettyPrinted).toString();
        if (!Objects.equals(cache.getHash(path), string2) || !Files.exists(path)) {
            Files.createDirectories(path.getParent());
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path);

            try {
                bufferedWriter.write(prettyPrinted);
            } catch (Throwable var10) {
                try {
                    bufferedWriter.close();
                } catch (Throwable var9) {
                    var10.addSuppressed(var9);
                }

                throw var10;
            }

            bufferedWriter.close();
        }

        cache.putNew(path, string2);
    }

    @Override
    public String getName() {
        return "Translation Provider";
    }
}
