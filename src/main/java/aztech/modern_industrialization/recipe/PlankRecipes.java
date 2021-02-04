package aztech.modern_industrialization.recipe;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.recipe.json.MIRecipeJson;
import com.google.gson.Gson;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.minecraft.fluid.Fluids;

public final class PlankRecipes {
    private static final Gson GSON = new Gson();

    public static void yes(RuntimeResourcePack pack) {
        genPlanks(pack, "oak", "logs");
        genPlanks(pack, "spruce", "logs");
        genPlanks(pack, "birch", "logs");
        genPlanks(pack, "jungle", "logs");
        genPlanks(pack, "acacia", "logs");
        genPlanks(pack, "dark_oak", "logs");
        genPlanks(pack, "crimson", "stems");
        genPlanks(pack, "warped", "stems");
    }

    private static void genPlanks(RuntimeResourcePack pack, String prefix, String suffix) {
        MIRecipeJson json = new MIRecipeJson("cutting_machine", 2, 200)
                .addFluidInput(Fluids.WATER, 1)
                .addItemInput("#minecraft:" + prefix + "_" + suffix, 1)
                .addOutput("minecraft:" + prefix + "_planks", 4);
        pack.addData(new MIIdentifier("recipes/generated/planks/" + prefix + ".json"), GSON.toJson(json).getBytes());
    }

    private PlankRecipes() {
    }
}
