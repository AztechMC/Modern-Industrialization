package aztech.modern_industrialization.datagen.recipe;

import aztech.modern_industrialization.machines.recipe.MachineRecipeBuilder;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.part.PartKeyProvider;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public abstract class BaseRecipeProvider extends RecipeProvider {
    protected final HolderGetter<Fluid> fluids;

    protected BaseRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        this.fluids = registries.lookupOrThrow(Registries.FLUID);
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

    protected Criterion<InventoryChangeTrigger.TriggerInstance> hasPartTag(Material material, PartKeyProvider part) {
        var tag = material.getPart(part).asIngredient(items).getValues().unwrapKey().orElseThrow(() -> new IllegalArgumentException("No tag for part: " + part.key()));
        return has(tag);
    }

    protected ForgeHammerRecipeBuilder forgeHammer(ItemLike input, ItemLike result, int hammerDamage) {
        return new ForgeHammerRecipeBuilder(Ingredient.of(input), 1, new ItemStackTemplate(result.asItem()), hammerDamage);
    }

    protected ForgeHammerRecipeBuilder forgeHammer(ItemLike input, ItemStackTemplate result, int hammerDamage) {
        return new ForgeHammerRecipeBuilder(Ingredient.of(input), 1, result, hammerDamage);
    }

    protected ForgeHammerRecipeBuilder forgeHammer(TagKey<Item> input, ItemStackTemplate result, int hammerDamage) {
        return new ForgeHammerRecipeBuilder(tag(input), 1, result, hammerDamage);
    }

    protected MachineRecipeBuilder machine(MachineRecipeType type) {
        return machine(type, 2, 100);
    }

    protected MachineRecipeBuilder machine(MachineRecipeType type, int eu, int duration) {
        return new MachineRecipeBuilder(fluids, items, type, eu, duration);
    }
}
