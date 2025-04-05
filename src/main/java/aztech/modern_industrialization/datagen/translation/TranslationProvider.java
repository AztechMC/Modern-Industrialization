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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.config.MIConfigBuilder;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.Definition;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasings;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class TranslationProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String OUTPUT_PATH = "assets/modern_industrialization/lang/en_us.json";

    private final PackOutput packOutput;
    private final Map<String, String> translationPairs = new TreeMap<>();

    public TranslationProvider(PackOutput packOutput) {
        this.packOutput = packOutput;
    }

    public void addTranslation(String key, String englishValue) {
        if (!translationPairs.containsKey(key)) {
            translationPairs.put(key, englishValue);
        } else {
            throw new IllegalArgumentException(
                    String.format("Error adding translation key %s for translation %s : already registered for translation %s", key, englishValue,
                            translationPairs.get(key)));
        }
    }

    private void addManualEntries() {
        addTranslation("block.modern_industrialization.pipe", "Pipe(s)");

        addTranslation("entity.minecraft.villager.modern_industrialization.industrialist", "Industrialist");

        addTranslation("key.categories.modern_industrialization.modern_industrialization", "Modern Industrialization");
        addTranslation("key.modern_industrialization.toggle_flight", "Toggle Flight");
        addTranslation("key.modern_industrialization.toggle_3x3", "Toggle 3x3 Mining");
        addTranslation("text.autoconfig.modern_industrialization.title", "Modern Industrialization Menu");
        addTranslation("tag.modern_industrialization.replicator_blacklist", "Replicator Blacklist");

        addTranslation("config.jade.plugin_modern_industrialization.overclock", "Machine Overclock");
        addTranslation("config.jade.plugin_modern_industrialization.pipe", "Pipe Information");
    }

    private void collectTranslationEntries() {
        addManualEntries();

        for (var entry : MIText.values()) {
            addTranslation(entry.getTranslationKey(), entry.getEnglishText());
            for (String additionalKey : entry.getAdditionalTranslationKey()) {
                addTranslation(additionalKey, entry.getEnglishText());
            }
        }

        for (var entry : MIConfigBuilder.configTranslations.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (Definition definition : Definition.TRANSLATABLE_DEFINITION) {
            addTranslation(definition.getTranslationKey(), definition.getEnglishName());
        }

        for (var entry : MITooltips.TOOLTIPS_ENGLISH_TRANSLATION.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (var entry : ReiMachineRecipes.categories.entrySet()) {
            if (entry.getKey().getNamespace().equals(MI.ID)) {
                addTranslation("rei_categories.modern_industrialization." + entry.getKey().getPath(), entry.getValue().englishName);
            }
        }

        for (var entry : TagsToGenerate.tagTranslations.entrySet()) {
            addTranslation(entry.getKey(), entry.getValue());
        }

        for (var ebfTier : ElectricBlastFurnaceBlockEntity.tiers) {
            addTranslation(ebfTier.getTranslationKey(), ebfTier.englishName());
        }

        for (var cableTier : CableTier.allTiers()) {
            addTranslation(cableTier.shortEnglishKey(), cableTier.shortEnglishName);
            addTranslation(cableTier.longEnglishKey(), cableTier.longEnglishName);
        }

        for (var casingName : MachineCasings.translations) {
            addTranslation(casingName.casing().getTranslationKey(), casingName.englishName());
        }
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(() -> {
            try {
                innerRun(output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, Util.backgroundExecutor());
    }

    public void innerRun(CachedOutput cache) throws IOException {
        collectTranslationEntries();

        customJsonSave(cache, GSON.toJsonTree(translationPairs), packOutput.getOutputFolder().resolve(OUTPUT_PATH));
    }

    private void customJsonSave(CachedOutput cache, JsonElement jsonElement, Path path) throws IOException {
        String sortedJson = GSON.toJson(jsonElement);
        String prettyPrinted = sortedJson.replace("\\u0027", "'");
        cache.writeIfNeeded(path, prettyPrinted.getBytes(StandardCharsets.UTF_8), Hashing.sha1().hashString(prettyPrinted, StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return "Translations";
    }
}
