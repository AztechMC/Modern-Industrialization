package aztech.modern_industrialization;

import com.google.common.collect.Sets;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MIRuntimeResourcePack implements ResourcePack {
    private final String name;
    private static final Set<String> NAMESPACES = Sets.newHashSet("modern_industrialization");
    private final Map<String, byte[]> resources = new HashMap<>();

    public MIRuntimeResourcePack(String name) {
        this.name = name;
    }

    public void addAsset(String path, byte[] asset) {
        if (resources.put("assets/" + path, asset) != null) {
            throw new IllegalStateException("Asset already exists in the runtime resource pack: " + path);
        }
    }

    public void addData(String path, byte[] asset) {
        if (resources.put("data/" + path, asset) != null) {
            throw new IllegalStateException("Data already exists in the runtime resource pack: " + path);
        }
    }

    @Override
    public InputStream openRoot(String fileName) throws IOException {
        if (resources.containsKey(fileName)) {
            return new ByteArrayInputStream(resources.get(fileName));
        }
        throw new IOException("Runtime resource pack doesn't contain " + fileName);
    }

    @Override
    public InputStream open(ResourceType type, Identifier id) throws IOException {
        return openRoot(type.getDirectory() + "/" + id.getNamespace() + "/" + id.getPath());
    }

    @Override
    public Collection<Identifier> findResources(ResourceType type, String namespace, String prefix, int maxDepth, Predicate<String> pathFilter) {
        String start = type.getDirectory() + "/" + namespace + "/" + prefix;
        return this.resources.keySet().stream().filter(s -> s.startsWith(start) && pathFilter.test(s))
                .map(string -> {
                    String[] parts = string.split("/", 3);
                    return new Identifier(parts[1], parts[2]);
                })
                .collect(Collectors.toList());
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
        return name;
    }

    @Override
    public void close() {
    }
}
