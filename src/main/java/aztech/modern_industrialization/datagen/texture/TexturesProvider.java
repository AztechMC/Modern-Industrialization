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

import aztech.modern_industrialization.textures.MITextures;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.client.resource.DefaultClientResourcePack;
import net.minecraft.client.resource.ResourceIndex;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.data.DataCache;
import net.minecraft.data.DataProvider;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceType;

public class TexturesProvider implements DataProvider {
    private final FabricDataGenerator dataGenerator;

    public TexturesProvider(FabricDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void run(DataCache cache) throws IOException {
        // Delete output folder first, because textures won't be generated if they already exist,
        // leading to the DataCache clearing the textures when it deletes unused paths from the cache.
        // Code from https://stackoverflow.com/questions/35988192/java-nio-most-concise-recursive-directory-delete
        try (Stream<Path> walk = Files.walk(dataGenerator.getOutput().resolve("assets/modern_industrialization/textures"))) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

        var generatedResources = dataGenerator.getOutput();
        var nonGeneratedResources = dataGenerator.getOutput().resolve("../../main/resources");
        var manager = new ReloadableResourceManagerImpl(ResourceType.CLIENT_RESOURCES);
        manager.addPack(new DefaultClientResourcePack(ClientBuiltinResourcePackProvider.DEFAULT_PACK_METADATA, new ResourceIndex(new File(""), "")));
        manager.addPack(new DirectoryResourcePack(nonGeneratedResources.toFile()));
        manager.addPack(new DirectoryResourcePack(generatedResources.toFile()));

        MITextures.offerTextures((image, textureId) -> writeTexture(cache, image, textureId), manager);
    }

    private void writeTexture(DataCache cache, NativeImage image, String textureId) {
        try {
            var path = dataGenerator.getOutput().resolve("assets").resolve(textureId.replace(':', '/'));
            var sha = SHA1.hashBytes(image.getBytes()).toString();
            if (!Objects.equals(cache.getOldSha1(path), sha) || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                image.writeTo(path);
            }
            cache.updateSha1(path, sha);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write texture " + textureId, ex);
        }
    }

    @Override
    public String getName() {
        return "Machine Textures Provider";
    }
}
