package aztech.modern_industrialization.recipe.json;

import net.minecraft.fluid.Fluid;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public final class MIRecipeJson {
    private final String type;
    private final int eu;
    private final int duration;
    private final List<MIItemInput> item_inputs = new ArrayList<>();
    private final List<MIFluidInput> fluid_inputs = new ArrayList<>();
    private final List<MIItemOutput> item_outputs = new ArrayList<>();

    private static class MIItemInput {
        String item;
        String tag;
        int amount;
    }

    private static class MIFluidInput {
        String fluid;
        int amount;
    }

    private static class MIItemOutput {
        String item;
        int amount;
    }

    public MIRecipeJson(String type, int eu, int duration) {
        this.type = type;
        this.eu = eu;
        this.duration = duration;
    }

    public MIRecipeJson addItemInput(String maybeTag, int amount) {
        MIItemInput input = new MIItemInput();
        input.amount = amount;
        if (maybeTag.startsWith("#")) {
            input.tag = maybeTag.substring(1);
        } else {
            input.item = maybeTag;
        }
        item_inputs.add(input);
        return this;
    }

    public MIRecipeJson addFluidInput(String fluid, int amount) {
        MIFluidInput input = new MIFluidInput();
        input.fluid = fluid;
        input.amount = amount;
        fluid_inputs.add(input);
        return this;
    }

    public MIRecipeJson addFluidInput(Fluid fluid, int amount) {
        Identifier id = Registry.FLUID.getId(fluid);
        if (id.equals(Registry.FLUID.getDefaultId())) {
            throw new RuntimeException("Could not find id for fluid " + fluid);
        }
        return addFluidInput(id.toString(), amount);
    }

    public MIRecipeJson addOutput(String itemId, int amount) {
        MIItemOutput output = new MIItemOutput();
        output.item = itemId;
        output.amount = amount;
        item_outputs.add(output);
        return this;
    }
}
