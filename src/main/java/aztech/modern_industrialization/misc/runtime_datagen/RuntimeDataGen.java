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

package aztech.modern_industrialization.misc.runtime_datagen;

import aztech.modern_industrialization.MI;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.DetectedVersion;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class RuntimeDataGen {
    @FunctionalInterface
    public interface DataGenConfig {
        void run(DataGenerator gen, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries, boolean run,
                boolean runtimeDatagen);
    }

    public static void run(DataGenConfig... configs) {
        try {
            runInner(configs);
        } catch (Exception ex) {
            MI.LOGGER.error("Failed to runtime datagen", ex);
        }
    }

    private static void runInner(DataGenConfig... configs) throws Exception {
        var miFolder = FMLPaths.GAMEDIR.get().resolve("modern_industrialization");

        // Create some relevant texture folders because I'm sure some people will forget it
        var datagenOverridesFolder = miFolder.resolve("extra_datagen_resources");
        Files.createDirectories(datagenOverridesFolder
                .resolve("assets")
                .resolve("modern_industrialization")
                .resolve("textures"));
        Files.createDirectories(datagenOverridesFolder
                .resolve("assets")
                .resolve("modern_industrialization")
                .resolve("datagen_texture_overrides"));

        var dataOutput = miFolder.resolve("runtime_datagen");

        MI.LOGGER.info("Starting MI runtime data generation");

        var modContainer = LoadingModList.get().getModFileById(MI.ID);
        var registriesFuture = CompletableFuture.supplyAsync(VanillaRegistries::createLookup, Util.backgroundExecutor());
        var gen = new DataGenerator(dataOutput, DetectedVersion.tryDetectVersion(), true);

        for (var config : configs) {
            config.run(
                    gen,
                    new ExistingFileHelper(List.of(), Set.of(), false, null, null),
                    registriesFuture,
                    true,
                    true);
        }

        gen.run();

        MI.LOGGER.info("Starting MI runtime pack calculation");

        var cleanedOutput = miFolder.resolve("generated_resources");
        // Delete output folder first before copying, otherwise Windows will complain
        // Code from https://stackoverflow.com/questions/35988192/java-nio-most-concise-recursive-directory-delete
        if (Files.exists(cleanedOutput)) {
            try (Stream<Path> walk = Files.walk(cleanedOutput)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }

        // Check caches to see which files need to be copied over
        try (var cacheStream = Files.walk(dataOutput.resolve(".cache"), 1)) {
            cacheStream.forEach(cachePath -> {
                if (Files.isDirectory(cachePath)) {
                    return;
                }

                var newCache = readCache(cachePath);
                var oldCache = readCache(modContainer.getFile().findResource(".cache/" + cachePath.getFileName()));

                for (var newEntry : newCache.entrySet()) {
                    var oldHash = oldCache.get(newEntry.getKey());

                    if (newEntry.getValue().equals(oldHash)) {
                        // File is unchanged, don't copy it
                        continue;
                    }

                    // File is changed, copy it
                    var newPath = cleanedOutput.resolve(newEntry.getKey());
                    try {
                        Files.createDirectories(newPath.getParent());
                        Files.copy(dataOutput.resolve(newEntry.getKey()), newPath);
                    } catch (IOException e) {
                        MI.LOGGER.error("Failed to copy file " + newEntry.getKey(), e);
                    }
                }
            });
        }

        MI.LOGGER.info("Successfully finished MI runtime data generation");
    }

    /**
     * Read cache, and return map from resource path to hash.
     */
    private static Map<String, String> readCache(Path path) {
        try (var reader = Files.newBufferedReader(path)) {
            Map<String, String> map = new HashMap<>();

            // Skip header line
            reader.readLine();

            reader.lines().forEach(line -> {
                String[] parts = line.split(" ", 2);
                map.put(parts[1], parts[0]);
            });

            return map;
        } catch (IOException e) {
            MI.LOGGER.warn("Failed to read cache file " + path, e);
            return Map.of();
        }
    }
}
