package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

public class MIMachineRecipeTypes {
    public static final MachineRecipeType MACERATOR = create("macerator").withItemInputs().withItemOutputs();

    public static void init() {
        // init static
    }

    private static MachineRecipeType create(String name) {
        return create(name, MachineRecipeType::new);
    }

    private static MachineRecipeType create(String name, Function<Identifier, MachineRecipeType> ctor) {
        MachineRecipeType type = ctor.apply(new MIIdentifier(name));
        Registry.register(Registry.RECIPE_SERIALIZER, type.getId(), type);
        Registry.register(Registry.RECIPE_TYPE, type.getId(), type);
        return type;
    }
}
