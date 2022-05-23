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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.definition.Definition;
import aztech.modern_industrialization.misc.tooltips.FaqTooltips;
import aztech.modern_industrialization.pipes.MIPipes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import org.jetbrains.annotations.NotNull;

public record TranslationProvider(FabricDataGenerator gen) implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final String OUTPUT_PATH = "assets/modern_industrialization/lang/en_us.json";

    private static final Map<String, String> TRANSLATION_PAIRS = new TreeMap<>();

    public static void addTranslation(String key, String englishValue) {
        if (!TRANSLATION_PAIRS.containsKey(key)) {
            TRANSLATION_PAIRS.put(key, englishValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Error adding translation key %s for translation %s : already registered for translation %s",
                            key, englishValue, TRANSLATION_PAIRS.get(key)));
        }
    }

    private static void addManualEntries() {
        addTranslation("block.modern_industrialization.pipe", "Pipe(s)");
        addTranslation("book.modern_industrialization.landing_text",
                "Welcome to Modern Industrialization! To get started, be sure to collect a lot of Copper Ore and Tin Ore.");
        addTranslation("item.modern_industrialization.energy_p2p_tunnel", "EU P2P Tunnel");
        addTranslation("key.modern_industrialization.activate", "Toggle Flight");
        addTranslation("text.autoconfig.modern_industrialization.title", "Modern Industrialization Menu");
    }

    @Override
    public void run(@NotNull HashCache cache) throws IOException {
        addManualEntries();

        for (var entry : MIText.values()) {
            addTranslation(entry.getTranslationKey(), entry.getEnglishText());
            for (String additionalKey : entry.getAdditionalTranslationKey()) {
                addTranslation(additionalKey, entry.getEnglishText());
            }
        }

        for (Field f : MIConfig.class.getFields()) {
            EnglishTranslation englishTranslation = f.getAnnotation(EnglishTranslation.class);
            if (englishTranslation != null) {
                addTranslation("text.autoconfig.modern_industrialization.option." + f.getName(), englishTranslation.value());
            }
        }

        for (Definition definition : Definition.TRANSLATABLE_DEFINITION) {
            addTranslation(definition.getTranslationKey(), definition.getEnglishName());
        }

        for (var entry : MIPipes.TRANSLATION.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (var entry : FaqTooltips.TOOLTIPS_ENGLISH_TRANSLATION.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            addTranslation("rei_categories.modern_industrialization." + entry.getKey(), entry.getValue().englishName);
        }

        customJsonSave(cache, GSON.toJsonTree(TRANSLATION_PAIRS), gen.getOutputFolder().resolve(OUTPUT_PATH));
    }

    private void customJsonSave(HashCache cache, JsonElement jsonElement, Path path) throws IOException {
        String sortedJson = GSON.toJson(jsonElement);
        String prettyPrinted = sortedJson.replace("\\u0027", "'");

        String string2 = SHA1.hashUnencodedChars(prettyPrinted).toString();
        if (!Objects.equals(cache.getHash(path), string2) || !Files.exists(path)) {
            Files.createDirectories(path.getParent());

            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path);) {
                bufferedWriter.write(prettyPrinted);
            }
        }

        cache.putNew(path, string2);
    }

    @Override
    public String getName() {
        return "Translation Provider";
    }
}
