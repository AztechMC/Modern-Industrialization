package aztech.modern_industrialization.mixin;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/**
 * The point of this mixin is to directly access the map {@code RecipeType -> List<Recipe>}, because the public access functions
 * all require an allocation and go through a useless stream.
 */
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Invoker("getAllOfType")
    Map<Identifier, Recipe> modern_industrialization_getAllOfType(RecipeType type);
}
