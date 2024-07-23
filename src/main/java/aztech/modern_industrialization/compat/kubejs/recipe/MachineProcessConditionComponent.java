package aztech.modern_industrialization.compat.kubejs.recipe;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import dev.latvian.mods.kubejs.recipe.component.SimpleRecipeComponent;
import dev.latvian.mods.rhino.type.TypeInfo;

public class MachineProcessConditionComponent extends SimpleRecipeComponent<MachineProcessCondition> {
	public static final MachineProcessConditionComponent MACHINE_PROCESS_CONDITION = new MachineProcessConditionComponent();

	public MachineProcessConditionComponent() {
		super(MI.ID + ":machine_process_condition", MachineProcessCondition.CODEC, TypeInfo.of(MachineProcessCondition.class));
	}
}
