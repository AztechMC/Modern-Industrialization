package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.MaterialPart;

import java.util.Map;

/**
 * A read-only material. Build with {@link MaterialBuilder}.
 */
public class Material {
    public final String name;
    final Map<String, MaterialPart> parts;

    Material(String name, Map<String, MaterialPart> parts) {
        this.name = name;
        this.parts = parts;
    }
}
