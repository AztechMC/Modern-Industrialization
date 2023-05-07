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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.textures.MITextures;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;

public record TexturesProvider(FabricDataGenerator dataGenerator, boolean runtimeDatagen) implements DataProvider {

    @Override
    public void run(CachedOutput cache) {
        var packs = new ArrayList<PackResources>();

        packs.add(new DefaultClientPackResources(ClientPackSource.BUILT_IN, new AssetIndex(new File(""), "")));

        if (runtimeDatagen) {
            // MI jar
            ModContainer container = FabricLoader.getInstance().getModContainer(ModernIndustrialization.MOD_ID).get();
            packs.add(ModNioResourcePack.create(new ResourceLocation("fabric", container.getMetadata().getId()),
                    container.getMetadata().getName(), container, null, PackType.CLIENT_RESOURCES, ResourcePackActivationType.ALWAYS_ENABLED));

            // extra_datagen_resources folder
            var extra = FabricLoader.getInstance().getGameDir().resolve("modern_industrialization").resolve("extra_datagen_resources");
            packs.add(new FolderPackResources(extra.toFile()));
        } else {
            var nonGeneratedResources = dataGenerator.getOutputFolder().resolve("../../main/resources").toFile();
            packs.add(new FolderPackResources(nonGeneratedResources));
        }

        try (var fallbackProvider = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, packs)) {
            generateTextures(cache, fallbackProvider);
        }
    }

    private void generateTextures(CachedOutput cache, ResourceProvider fallbackResourceProvider) {
        // This is the order for texture fetching:
        // - texture overrides (in the datagen output folder)
        // - generated textures (also in the output folder)
        // - user-provided resource packs
        // - MI and MC jar textures
        var generatedResources = dataGenerator.getOutputFolder().toFile();
        List<PackResources> generatedPack = List.of(new FolderPackResources(generatedResources));

        try (var outputPack = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, generatedPack)) {
            MITextures.offerTextures(
                    (image, textureId) -> writeTexture(cache, image, textureId),
                    (json, path) -> customJsonSave(cache, json, path),
                    resourceLocation -> {
                        // Generated first
                        var generated = outputPack.getResource(resourceLocation);
                        if (generated.isPresent()) {
                            return generated;
                        }
                        return fallbackResourceProvider.getResource(resourceLocation);
                    });
        }
    }

    private void writeTexture(CachedOutput cache, NativeImage image, String textureId) {
        try {
            var path = dataGenerator.getOutputFolder().resolve("assets").resolve(textureId.replace(':', '/'));
            cache.writeIfNeeded(path, image.asByteArray(), Hashing.sha1().hashBytes(image.asByteArray()));
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write texture " + textureId, ex);
        }
    }

    private void customJsonSave(CachedOutput cache, JsonElement jsonElement, String path) {
        try {
            Path pathFormatted = dataGenerator.getOutputFolder().resolve("assets").resolve(path.replace(':', '/'));
            DataProvider.saveStable(cache, jsonElement, pathFormatted);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write element in texture creation " + path, ex);
        }
    }

    @Override
    public String getName() {
        return "Machine Textures Provider";
    }
}
