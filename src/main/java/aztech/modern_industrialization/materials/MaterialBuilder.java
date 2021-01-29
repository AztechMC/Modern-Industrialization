package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.materials.part.RegularItemPart;

import java.util.*;
import java.util.function.Function;

public final class MaterialBuilder {
    private final Map<String, MaterialPart> partsMap = new TreeMap<>();
    private final Context context;
    private final String materialName;
    private final String materialSet;
    private final int color;

    public MaterialBuilder(String materialName, MaterialSet materialSet, int color) {
        this.context = new Context(color, materialName);
        this.materialName = materialName;
        this.materialSet = materialSet.name;
        this.color = color;
    }

    public MaterialBuilder addRegularItemParts(String... parts) {
        for (String part : parts) {
            addPart(new RegularItemPart(materialName, part, materialSet, color));
        }
        return this;
    }

    @SafeVarargs
    public final MaterialBuilder addParts(Function<Context, MaterialPart>... partFunctions) {
        for (Function<Context, MaterialPart> partFunction : partFunctions) {
            addPart(partFunction.apply(context));
        }
        return this;
    }

    private void addPart(MaterialPart part) {
        if (partsMap.put(part.getPart(), part) != null) {
            throw new IllegalStateException("Part " + part.getItemId() + " is already registered for this material!");
        }
    }

    public MaterialBuilder overridePart(Function<Context, MaterialPart> partFunction) {
        MaterialPart part = partFunction.apply(context);
        if (partsMap.put(part.getPart(), part) == null) {
            throw new IllegalStateException("Part " + part.getItemId() + " was not already registered for this material!");
        }
        return this;
    }

    public Material build() {
        return new Material(materialName, Collections.unmodifiableMap(partsMap));
    }

    public static class Context {
        public final int color;
        public final String materialName;

        private Context(int color, String materialName) {
            this.color = color;
            this.materialName = materialName;
        }
    }
}
