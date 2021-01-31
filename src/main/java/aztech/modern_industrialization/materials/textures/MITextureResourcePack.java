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
package aztech.modern_industrialization.materials.textures;

import com.google.common.collect.Sets;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

// ty Lambda
public class MITextureResourcePack implements ResourcePack {
    private static final Set<String> NAMESPACES = Sets.newHashSet("modern_industrialization");
    private final Map<String, byte[]> resources = new HashMap<>();

    MITextureResourcePack() {
    }

    public void addAsset(String path, byte[] asset) {
        if (resources.put("assets/" + path, asset) != null) {
            throw new IllegalStateException("Resource already exists in the generated texture resource pack: " + path);
        }
    }

    @Override
    public InputStream openRoot(String fileName) throws IOException {
        if (resources.containsKey(fileName)) {
            return new ByteArrayInputStream(resources.get(fileName));
        }
        throw new IOException("Generated pack doesn't contain " + fileName);
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        if (type == ResourceType.CLIENT_RESOURCES) {
            return openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
        } else {
            throw new IOException("Reading server data from texture pack.");
        }
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        if (type == ResourceType.CLIENT_RESOURCES) {
            String start = "assets/" + namespace + "/" + prefix;
            return this.resources.keySet().stream().filter(s -> s.startsWith(start) && pathFilter.test(s)).map(Identifier::new)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean contains(ResourceType type, Identifier id) {
        return resources.containsKey(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
    }

    @Override
    public Set<String> getNamespaces(ResourceType type) {
        return NAMESPACES;
    }

    @Override
    public <T> T parseMetadata(ResourceMetadataReader<T> metaReader) {
        return null;
    }

    @Override
    public String getName() {
        return "Modern Industrialization generated textures";
    }

    @Override
    public void close() {
    }
}
