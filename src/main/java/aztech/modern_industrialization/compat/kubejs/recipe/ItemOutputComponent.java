package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import dev.latvian.mods.kubejs.core.ItemStackKJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.kubejs.recipe.component.UniqueIdBuilder;
import dev.latvian.mods.kubejs.recipe.match.ItemMatch;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatchInfo;
import dev.latvian.mods.rhino.Context;
import net.minecraft.world.item.ItemStack;

public class ItemOutputComponent extends SimpleRecipeComponent<MachineRecipe.ItemOutput> {
	public static final ItemOutputComponent ITEM_OUTPUT = new ItemOutputComponent();

	public ItemOutputComponent() {
		super(MI.ID + ":item_input", MachineRecipe.ItemOutput.CODEC, ItemStackJS.TYPE_INFO);
	}

	@Override
	public MachineRecipe.ItemOutput wrap(Context cx, KubeRecipe recipe, Object from) {
		var itemStack = (ItemStack) cx.jsToJava(from, typeInfo());
		return new MachineRecipe.ItemOutput(ItemVariant.of(itemStack), itemStack.getCount(), 1);
	}

	@Override
	public boolean matches(Context cx, KubeRecipe recipe, MachineRecipe.ItemOutput value, ReplacementMatchInfo match) {
		return match.match() instanceof ItemMatch m && !value.variant().isBlank() && value.amount() > 0
			&& m.matches(cx, value.getStack(), match.exact());
	}

	@Override
	public MachineRecipe.ItemOutput replace(Context cx, KubeRecipe recipe, MachineRecipe.ItemOutput original, ReplacementMatchInfo match,
											Object with) {
		if (matches(cx, recipe, original, match)) {
			var withJava = (ItemStack) cx.jsToJava(with, typeInfo());
			return new MachineRecipe.ItemOutput(ItemVariant.of(withJava), withJava.getCount(), original.probability());
		} else {
			return original;
		}
	}

	@Override
	public boolean isEmpty(MachineRecipe.ItemOutput value) {
		return value.getStack().isEmpty();
	}

	@Override
	public void buildUniqueId(UniqueIdBuilder builder, MachineRecipe.ItemOutput value) {
		if (!value.getStack().isEmpty()) {
			builder.append(ItemStackKJS.class.cast(value.getStack()).kjs$getIdLocation());
		}
	}
}
