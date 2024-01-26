/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.init;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.machines.recipe.CentrifugeMachineRecipeType;
import aztech.modern_industrialization.machines.recipe.CuttingMachineRecipeType;
import aztech.modern_industrialization.machines.recipe.FurnaceRecipeProxy;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public class MIMachineRecipeTypes {
    private static final List<MachineRecipeType> recipeTypes = new ArrayList<>();
    // @formatter:off
    // Single block
    public static final MachineRecipeType ASSEMBLER = create("assembler").withItemInputs().withFluidInputs().withItemOutputs();
    public static final MachineRecipeType CENTRIFUGE = create("centrifuge", CentrifugeMachineRecipeType::new).withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType CHEMICAL_REACTOR = create("chemical_reactor").withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType COMPRESSOR = create("compressor").withItemInputs().withItemOutputs();
    public static final MachineRecipeType CUTTING_MACHINE = create("cutting_machine", CuttingMachineRecipeType::new).withItemInputs().withFluidInputs().withItemOutputs();
    public static final MachineRecipeType DISTILLERY = create("distillery").withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType ELECTROLYZER = create("electrolyzer").withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType FURNACE = create("furnace", FurnaceRecipeProxy::new);
    public static final MachineRecipeType MACERATOR = create("macerator").withItemInputs().withItemOutputs();
    public static final MachineRecipeType MIXER = create("mixer").withItemInputs().withFluidInputs().withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType PACKER = create("packer").withItemInputs().withItemOutputs();
    public static final MachineRecipeType POLARIZER = create("polarizer").withItemInputs().withItemOutputs();
    public static final MachineRecipeType UNPACKER = create("unpacker").withItemInputs().withItemOutputs();
    public static final MachineRecipeType WIREMILL = create("wiremill").withItemInputs().withItemOutputs();
    // Multi block
    public static final MachineRecipeType BLAST_FURNACE = create("blast_furnace").withItemInputs().withItemOutputs().withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType COKE_OVEN = create("coke_oven").withItemInputs().withItemOutputs().withFluidOutputs();
    public static final MachineRecipeType DISTILLATION_TOWER = create("distillation_tower").withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType FUSION_REACTOR = create("fusion_reactor").withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType HEAT_EXCHANGER = create("heat_exchanger").withFluidInputs().withFluidOutputs().withItemOutputs().withItemInputs();
    public static final MachineRecipeType IMPLOSION_COMPRESSOR = create("implosion_compressor").withItemInputs().withItemOutputs();
    public static final MachineRecipeType OIL_DRILLING_RIG = create("oil_drilling_rig").withItemInputs().withFluidOutputs();
    public static final MachineRecipeType PRESSURIZER = create("pressurizer").withItemInputs().withFluidInputs().withFluidOutputs();
    public static final MachineRecipeType QUARRY = create("quarry").withItemInputs().withItemOutputs();
    public static final MachineRecipeType VACUUM_FREEZER = create("vacuum_freezer").withItemInputs().withItemOutputs().withFluidInputs().withFluidOutputs();
    // @formatter:on

    static {
        KubeJSProxy.instance.fireRegisterRecipeTypesEvent();
    }

    public static void init() {
        // init static
    }

    public static List<MachineRecipeType> getRecipeTypes() {
        return Collections.unmodifiableList(recipeTypes);
    }

    public static MachineRecipeType create(String name) {
        return create(name, MachineRecipeType::new);
    }

    private static MachineRecipeType create(String name, Function<ResourceLocation, MachineRecipeType> ctor) {
        MachineRecipeType type = ctor.apply(MI.id(name));
        MIRegistries.RECIPE_SERIALIZERS.register(name, () -> type);
        MIRegistries.RECIPE_TYPES.register(name, () -> type);
        recipeTypes.add(type);
        return type;
    }
}
