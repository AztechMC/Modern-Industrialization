package aztech.modern_industrialization.recipe;

import net.devtech.arrp.api.RuntimeResourcePack;

public final class MIRecipes {
    public static RuntimeResourcePack buildRecipeDataPack() {
        RuntimeResourcePack pack = RuntimeResourcePack.create("modern_industrialization:recipes");

        PlankRecipes.yes(pack);

        return pack;
    }

    private MIRecipes() {
    }
}
