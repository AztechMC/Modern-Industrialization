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
package aztech.modern_industrialization.textures;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class TextureManager {
    private final ResourceProvider rm;
    private final BiConsumer<NativeImage, String> textureWriter;
    private final BiConsumer<JsonElement, String> mcMetaWriter;
    private final Queue<IORunnable> endRunnables = new ConcurrentLinkedQueue<>();
    private final Set<String> generatedTextures = ConcurrentHashMap.newKeySet();

    private final Gson GSON = new Gson();

    public TextureManager(ResourceProvider rm, BiConsumer<NativeImage, String> textureWriter, BiConsumer<JsonElement, String> mcMetaWriter) {
        this.rm = rm;
        this.textureWriter = textureWriter;
        this.mcMetaWriter = mcMetaWriter;
    }

    public boolean hasAsset(String asset) {
        return rm.getResource(new ResourceLocation(asset)).isPresent();
    }

    public NativeImage getAssetAsTexture(String textureId) throws IOException {
        var resource = rm.getResource(new ResourceLocation(textureId));
        if (resource.isPresent()) {
            try (var stream = resource.get().open()) {
                return NativeImage.read(stream);
            }
        } else {
            throw new IOException("Couldn't find texture " + textureId);
        }
    }

    /**
     * Add texture if it's not already loaded, but doesn't close the image.
     */
    public void addTexture(String textureId, NativeImage image) throws IOException {
        addTexture(textureId, image, false);
    }

    public void addTexture(String textureId, NativeImage image, boolean closeImage) throws IOException {
        // The texture adding logic is as follows:
        // - if there is an override, we copy the override over to the output
        // - otherwise, we write the texture to the output
        if (!textureId.contains(":textures/")) {
            throw new IllegalArgumentException("Invalid texture location: " + textureId);
        }

        String overrideId = textureId.replace(":textures/", ":datagen_texture_overrides/");
        Optional<Resource> overrideResource = rm.getResource(new ResourceLocation(overrideId));

        if (overrideResource.isPresent()) {
            // Copy the override over
            try (var stream = overrideResource.get().open();
                    var overrideImage = NativeImage.read(stream)) {
                textureWriter.accept(overrideImage, textureId);
            }
        } else {
            // Write generated texture
            textureWriter.accept(image, textureId);
        }

        generatedTextures.add(textureId.replace(":textures/", ":"));

        // Close image in any case...
        if (closeImage) {
            image.close();
        }
    }

    public void addMcMeta(String path, MCMetaInfo info) {
        mcMetaWriter.accept(GSON.toJsonTree(info), path);
    }

    public void runAtEnd(IORunnable runnable) {
        endRunnables.add(runnable);
    }

    public CompletableFuture<?> doEndWork() {
        var ret = CompletableFuture.allOf(
                endRunnables.stream().map(r -> CompletableFuture.runAsync(r::safeRun, Util.backgroundExecutor())).toArray(CompletableFuture[]::new));
        endRunnables.clear();
        return ret;
    }

    public void markTexturesAsGenerated(ExistingFileHelper helper) {
        for (var texture : generatedTextures) {
            helper.trackGenerated(new ResourceLocation(texture), PackType.CLIENT_RESOURCES, "", "textures");
        }
    }
}
