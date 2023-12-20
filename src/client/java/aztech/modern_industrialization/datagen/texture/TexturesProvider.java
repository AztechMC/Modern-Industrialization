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
package aztech.modern_industrialization.datagen.texture;

// fetch:

// - datagen override (des resource packs, MI jar)
// - datagen
// - resource pack utilisateur
// - jar MI et MC client
// generate texture:
// - if override exists: copy from override
// - else: generate

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.resource.FastPathPackResources;
import aztech.modern_industrialization.textures.MITextures;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public record TexturesProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper, boolean runtimeDatagen) implements DataProvider {

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        var packs = new ArrayList<PackResources>();

        packs.add(new VanillaPackResourcesBuilder().exposeNamespace("minecraft").pushJarResources().build());

        if (runtimeDatagen) {
            // MI jar
            packs.add(new PathPackResources("mi:runtimedatagen", ModList.get().getModFileById(MI.ID).getFile().getSecureJar().getRootPath(), true));

            // extra_datagen_resources folder
            var extra = FMLPaths.GAMEDIR.get().resolve("modern_industrialization").resolve("extra_datagen_resources");
            packs.add(new FastPathPackResources("extra", extra, true));
        } else {
            var nonGeneratedResources = packOutput.getOutputFolder().resolve("../../main/resources");
            packs.add(new FastPathPackResources("nonGen", nonGeneratedResources, true));
        }

        List<CompletableFuture<?>> jsonSaveFutures = new ArrayList<>();
        var fallbackProvider = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, packs);

        return generateTextures(cache, fallbackProvider, jsonSaveFutures::add)
                .whenComplete((result, throwable) -> fallbackProvider.close())
                .thenRunAsync(() -> CompletableFuture.allOf(jsonSaveFutures.toArray(CompletableFuture[]::new)), Util.backgroundExecutor());
    }

    private CompletableFuture<?> generateTextures(CachedOutput cache, ResourceProvider fallbackResourceProvider,
            Consumer<CompletableFuture<?>> futureList) {
        // This is the order for texture fetching:
        // - texture overrides (in the datagen output folder)
        // - generated textures (also in the output folder)
        // - user-provided resource packs
        // - MI and MC jar textures
        var generatedResources = packOutput.getOutputFolder();
        List<PackResources> generatedPack = List.of(new FastPathPackResources("gen", generatedResources, true));

        var outputPack = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, generatedPack);
        return MITextures.offerTextures(
                (image, textureId) -> writeTexture(cache, image, textureId),
                (json, path) -> futureList.accept(customJsonSave(cache, json, path)),
                resourceLocation -> {
                    // Generated first
                    var generated = outputPack.getResource(resourceLocation);
                    if (generated.isPresent()) {
                        return generated;
                    }
                    return fallbackResourceProvider.getResource(resourceLocation);
                },
                existingFileHelper)
                .whenComplete((result, throwable) -> outputPack.close());
    }

    private void writeTexture(CachedOutput cache, NativeImage image, String textureId) {
        try {
            var path = packOutput.getOutputFolder().resolve("assets").resolve(textureId.replace(':', '/'));
            cache.writeIfNeeded(path, image.asByteArray(), Hashing.sha1().hashBytes(image.asByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write texture " + textureId, ex);
        }
    }

    private CompletableFuture<?> customJsonSave(CachedOutput cache, JsonElement jsonElement, String path) {
        Path pathFormatted = packOutput.getOutputFolder().resolve("assets").resolve(path.replace(':', '/'));
        return DataProvider.saveStable(cache, jsonElement, pathFormatted);
    }

    @Override
    public String getName() {
        return "Textures";
    }
}
