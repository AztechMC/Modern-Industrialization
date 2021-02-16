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
package aztech.modern_industrialization.machinesv2.init;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import java.util.function.Function;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
