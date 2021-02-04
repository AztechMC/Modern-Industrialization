package aztech.modern_industrialization.recipe.json;

@SuppressWarnings({ "FieldCanBeLocal", "MismatchedQueryAndUpdateOfCollection", "UnusedDeclaration" })
public class SmeltingRecipeJson {
    private final String type;
    private final int cookingtime;
    private final double experience;
    private final Ingredient ingredient;
    private final String result;

    public enum SmeltingRecipeType {
        SMELTING,
        BLASTING;

        public static SmeltingRecipeType ofBlasting(boolean blasting) {
            return blasting ? BLASTING : SMELTING;
        }
    }

    public static class Ingredient {
        String item;
    }

    public SmeltingRecipeJson(SmeltingRecipeType type, String inputItem, String outputItem, int cookingtime, double experience) {
        this.type = type == SmeltingRecipeType.SMELTING ? "minecraft:smelting" : "minecraft:blasting";
        this.cookingtime = cookingtime;
        this.experience = experience;
        this.ingredient = new Ingredient();
        ingredient.item = inputItem;
        result = outputItem;
    }
}
