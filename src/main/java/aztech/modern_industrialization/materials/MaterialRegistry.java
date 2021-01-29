package aztech.modern_industrialization.materials;

import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class MaterialRegistry {
    static final Map<String, Material> MATERIALS = new TreeMap<>();

    static void addMaterial(Material material) {
        if (MATERIALS.put(material.name, material) != null) {
            throw new IllegalStateException("Duplicate registration of material " + material.name);
        }
    }

    public static Material getMaterial(String name) {
        Material material = MATERIALS.get(name);
        Objects.requireNonNull(material);
        return material;
    }
}
