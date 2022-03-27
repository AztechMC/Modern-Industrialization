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
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.client.resources.AssetIndex;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.DefaultClientPackResources;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;

public class TexturesProvider implements DataProvider {
    private final FabricDataGenerator dataGenerator;

    public TexturesProvider(FabricDataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public void run(HashCache cache) throws IOException {
        // Delete output folder first, because textures won't be generated if they already exist,
        // leading to the DataCache clearing the textures when it deletes unused paths from the cache.
        // Code from https://stackoverflow.com/questions/35988192/java-nio-most-concise-recursive-directory-delete
        var textureDir = dataGenerator.getOutputFolder().resolve("assets/modern_industrialization/textures");
        if (Files.exists(textureDir)) {
            try (Stream<Path> walk = Files.walk(textureDir)) {
                walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            }
        }

        var generatedResources = dataGenerator.getOutputFolder();
        var nonGeneratedResources = dataGenerator.getOutputFolder().resolve("../../main/resources");
        var manager = new MultiPackResourceManager(PackType.CLIENT_RESOURCES, List.of(
                new DefaultClientPackResources(ClientPackSource.BUILT_IN, new AssetIndex(new File(""), "")),
                new FolderPackResources(nonGeneratedResources.toFile()),
                new FolderPackResources(generatedResources.toFile())));

        MITextures.offerTextures((image, textureId) -> writeTexture(cache, image, textureId), manager);
    }

    private void writeTexture(HashCache cache, NativeImage image, String textureId) {
        try {
            var path = dataGenerator.getOutputFolder().resolve("assets").resolve(textureId.replace(':', '/'));
            var sha = SHA1.hashBytes(image.asByteArray()).toString();
            if (!Objects.equals(cache.getHash(path), sha) || !Files.exists(path)) {
                Files.createDirectories(path.getParent());
                image.writeToFile(path);
            }
            cache.putNew(path, sha);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write texture " + textureId, ex);
        }
    }

    @Override
    public String getName() {
        return "Machine Textures Provider";
    }
}
