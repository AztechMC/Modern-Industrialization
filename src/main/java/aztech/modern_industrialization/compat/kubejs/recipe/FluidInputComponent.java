package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import dev.latvian.mods.kubejs.fluid.FluidWrapper;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.FluidMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.kubejs.util.RegistryAccessContainer;
import dev.latvian.mods.rhino.Context;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidInputComponent extends SimpleRecipeComponent<MachineRecipe.FluidInput> {
	public static final FluidInputComponent FLUID_INPUT = new FluidInputComponent();

	public FluidInputComponent() {
		super(MI.ID + ":fluid_input", MachineRecipe.FluidInput.CODEC, FluidWrapper.SIZED_INGREDIENT_TYPE_INFO);
	}

	@Override
	public MachineRecipe.FluidInput wrap(Context cx, KubeRecipe recipe, Object from) {
		var fs = FluidWrapper.wrap(RegistryAccessContainer.of(cx), from);
		return new MachineRecipe.FluidInput(fs.getFluid(), fs.getAmount(), 1);
	}

	@Override
	public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.FluidInput value, ReplacementMatchInfo match) {
		return match.match() instanceof FluidMatch m && m.matches(cx, new FluidStack(value.fluid(), 1), match.exact());
	}

	@Override
	public MachineRecipe.FluidInput replace(Context cx, KubeRecipe recipe, MachineRecipe.FluidInput original, ReplacementMatchInfo match, Object with) {
		if (matches(cx, recipe, original, match)) {
			var fs = FluidWrapper.wrap(RegistryAccessContainer.of(cx), with);
			return new MachineRecipe.FluidInput(fs.getFluid(), original.amount(), original.probability());
		} else {
			return original;
		}
	}

	@Override
	public boolean isEmpty(MachineRecipe.FluidInput value) {
		return value.amount() <= 0 || value.fluid().isSame(Fluids.EMPTY);
	}

	@Override
	public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.FluidInput value) {
		if (!isEmpty(value)) {
			builder.append(value.fluid().builtInRegistryHolder().getKey().location());
		}
	}
}
