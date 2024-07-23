package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.schema.KubeRecipeFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.List;

public class MachineKubeRecipe extends KubeRecipe implements ProcessConditionHelper {
	public static final KubeRecipeFactory FACTORY = new KubeRecipeFactory(MI.id("machine"), MachineKubeRecipe.class, MachineKubeRecipe::new);

	private <T> MachineKubeRecipe addToList(RecipeKey<List<T>> key, T element) {
		// In 1.21, this can be null when the recipe is constructed via a JS constructor apparently.
		var value = getValue(key);
		var list = value == null ? new ArrayList<T>() : new ArrayList<>(value);
		list.add(element);
		setValue(key, list);
		return this;
	}

	public MachineKubeRecipe itemIn(SizedIngredient ingredient) {
		return itemIn(ingredient, 1F);
	}

	public MachineKubeRecipe itemIn(SizedIngredient ingredient, float chance) {
		return addToList(MachineRecipeSchema.ITEM_INPUTS, new MachineRecipe.ItemInput(ingredient.ingredient(), ingredient.count(), chance));
	}

	public MachineKubeRecipe itemOut(ItemStack output) {
		return itemOut(output, 1F);
	}

	public MachineKubeRecipe itemOut(ItemStack output, float chance) {
		return addToList(MachineRecipeSchema.ITEM_OUTPUTS, new MachineRecipe.ItemOutput(ItemVariant.of(output), output.getCount(), chance));
	}

	public MachineKubeRecipe fluidIn(Fluid fluid, long mbs) {
		return fluidIn(fluid, mbs, 1F);
	}

	public MachineKubeRecipe fluidIn(Fluid fluid, long mbs, float probability) {
		return addToList(MachineRecipeSchema.FLUID_INPUTS, new MachineRecipe.FluidInput(fluid, mbs, probability));
	}

	public MachineKubeRecipe fluidOut(Fluid fluid, long mbs) {
		return fluidOut(fluid, mbs, 1F);
	}

	public MachineKubeRecipe fluidOut(Fluid fluid, long mbs, float probability) {
		return addToList(MachineRecipeSchema.FLUID_OUTPUTS, new MachineRecipe.FluidOutput(fluid, mbs, probability));
	}

	@Override
	public MachineKubeRecipe processCondition(MachineProcessCondition condition) {
		return addToList(MachineRecipeSchema.MACHINE_PROCESS_CONDITIONS, condition);
	}
}
