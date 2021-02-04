package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIRuntimeResourcePack;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.minecraft.resource.ResourceManager;

public final class MIRecipes {
    public static RuntimeResourcePack buildRecipesPack() {
        RuntimeResourcePack pack = RuntimeResourcePack.create("modern_industrialization:recipes");

        PlankRecipes.yes(pack);

        return pack;
    }

    private MIRecipes() {
    }

    public static MIRuntimeResourcePack buildGeneratedRecipesPack(ResourceManager manager) {
        MIRuntimeResourcePack pack = new MIRuntimeResourcePack("MI Generated recipes");

        AssemblerRecipes.yes(pack, manager);

        return pack;
    }
}
