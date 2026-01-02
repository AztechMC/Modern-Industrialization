package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.crafting.Ingredient;

public abstract class MIRecipeProvider extends RecipeProvider {
    protected MIRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    protected Ingredient tag(String id) {
        return tag(ItemTags.create(Identifier.parse(id)));
    }

    protected Ingredient conventionTag(String path) {
        return tag("c:" + path);
    }

    protected Ingredient partIngredient(Material material, PartKeyProvider part) {
        return material.getPart(part).asIngredient(items);
    }
}
