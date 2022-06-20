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
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class TextureManager {
    private final ResourceManager rm;
    private final BiConsumer<NativeImage, String> textureWriter;
    private final BiConsumer<JsonElement, String> mcMetaWriter;
    private final List<Runnable> endRunnables = new ArrayList<>();

    private final Gson GSON = new Gson();

    public TextureManager(ResourceManager rm, BiConsumer<NativeImage, String> textureWriter, BiConsumer<JsonElement, String> mcMetaWriter) {
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
        ResourceLocation id = new ResourceLocation(textureId);
        if (rm.getResource(id).isEmpty()) { // Allow textures to be manually overridden.
            textureWriter.accept(image, textureId);
        }
        if (closeImage) {
            image.close();
        }
    }

    public void addMcMeta(String path, MCMetaInfo info) {
        mcMetaWriter.accept(GSON.toJsonTree(info), path);
    }

    public void runAtEnd(Runnable runnable) {
        endRunnables.add(runnable);
    }

    public void onEnd() {
        for (Runnable runnable : endRunnables) {
            runnable.run();
        }
        endRunnables.clear();
    }

    public static class Animation {
        private final int frametime;

        public Animation(int frametime) {
            this.frametime = frametime;
        }

        public int frametime() {
            return frametime;
        }
    }

    public static class AnimationWithFrames extends Animation {
        public final List<Integer> frames;

        public AnimationWithFrames(int frametime, List<Integer> frames) {
            super(frametime);
            this.frames = frames;
        }
    }

    public static class MCMetaInfo {
        public final Animation animation;

        public MCMetaInfo(Animation animation) {
            this.animation = animation;
        }

    }

}
