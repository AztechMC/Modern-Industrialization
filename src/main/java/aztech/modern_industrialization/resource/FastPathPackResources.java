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

package aztech.modern_industrialization.resource;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

/**
 * A PathPackResources that uses {@link File#exists()} instead of {@link Files#exists} with the default FS.
 */
public class FastPathPackResources extends PathPackResources {
    private final Path root;

    public FastPathPackResources(PackLocationInfo locationInfo, Path root) {
        super(locationInfo, root);
        this.root = root;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        Path path = this.root.resolve(packType.getDirectory()).resolve(location.getNamespace());
        return getResource(location, path);
    }

    public static IoSupplier<InputStream> getResource(ResourceLocation location, Path path) {
        return FileUtil.decomposePath(location.getPath()).mapOrElse(list -> {
            Path path2 = FileUtil.resolvePath(path, list);
            return returnFileIfExists(path2);
        }, error -> {
            LogUtils.getLogger().error("Invalid path {}: {}", path, error.message());
            return null;
        });
    }

    @Nullable
    private static IoSupplier<InputStream> returnFileIfExists(Path path) {
        return exists(path) && validatePath(path) ? IoSupplier.create(path) : null;
    }

    private static final FileSystem DEFAULT_FS = FileSystems.getDefault();

    private static boolean exists(Path path) {
        // NIO Files.exists is notoriously slow when checking the file system
        return path.getFileSystem() == DEFAULT_FS ? path.toFile().exists() : Files.exists(path);
    }
}
