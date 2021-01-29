package aztech.modern_industrialization.materials;

public class MaterialHelper {
    public static boolean hasBlock(String part) {
        return part.equals("block") || part.equals("ore");
    }

    public static boolean isOre(String part) {
        return part.equals("ore");
    }
}
