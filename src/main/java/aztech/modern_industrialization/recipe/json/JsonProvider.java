package aztech.modern_industrialization.recipe.json;

import com.google.gson.JsonObject;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class JsonProvider implements RecipeJsonProvider {

    private final RecipeSerializer<?> serializer;
    private final Identifier recipeId;
    private final RecipeJson recipe;

    public JsonProvider(RecipeSerializer<?> serializer, Identifier recipeId, RecipeJson recipe) {
        this.serializer = serializer;
        this.recipeId = recipeId;
        this.recipe = recipe;
    }

    @Override
    public void serialize(JsonObject json) {
        throw new UnsupportedOperationException("We override toJson()");
    }

    @Override
    public JsonObject toJson() {
        return recipe.toJsonObject();
    }

    @Override
    public Identifier getRecipeId() {
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
        return null;
    }

    @Nullable
    @Override
    public Identifier getAdvancementId() {
        return null;
    }
}
