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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import oshi.util.tuples.Pair;

public record TranslationProvider(FabricDataGenerator gen) implements DataProvider {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public void run(HashCache cache) throws IOException {
        String langPathAll = "assets/modern_industrialization/lang/en_us.json";

        Path outputPath = gen.getOutputFolder();

        TreeMap<String, String> translation = GSON
                .fromJson(new FileReader("../../src/main/resources/assets/modern_industrialization/lang/en_us_not_generated.json",
                        StandardCharsets.UTF_8), TreeMap.class);

        TreeMap<String, String> translationAll = (TreeMap<String, String>) translation.clone();

        List<Pair<String, String>> translationsPair = new ArrayList<>();

        for (Definition definition : Definition.TRANSLATABLE_DEFINITION) {
            translationsPair.add(new Pair<>(definition.getTranslationKey(), definition.getEnglishName()));
        }

        for (var entry : MIPipes.TRANSLATION.entrySet()) {
            translationsPair.add(new Pair<>(entry.getKey(), entry.getValue()));
        }

        for (Pair<String, String> translationPair : translationsPair) {

            translationAll.put(translationPair.getA(), translationPair.getB());

        }

        save(cache, GSON.toJsonTree(translationAll), outputPath.resolve(langPathAll));

    }

    private void save(HashCache cache, JsonElement jsonElement, Path path) throws IOException {

        String sortedJson = GSON.toJson(jsonElement);
        String prettyPrinted = sortedJson.replace("\\u0027", "\'");

        String string2 = SHA1.hashUnencodedChars(prettyPrinted).toString();
        if (!Objects.equals(cache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent());
            BufferedWriter bufferedWriter = Files.newBufferedWriter(path);

            try {
                bufferedWriter.write(prettyPrinted);
            } catch (Throwable var10) {
                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                    }
                }

                throw var10;
            }

            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }

        cache.putNew(path, string2);
    }

    @Override
    public String getName() {
        return "Translation Provider";
    }
}
