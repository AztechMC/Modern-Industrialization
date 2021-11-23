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
package aztech.modern_industrialization.datagen.recipe.factory;

import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.fluid.Fluid;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

public class MachineRecipeFactory {
    private static transient final Gson GSON = new Gson();

    private transient final MachineRecipeType type;
    private final int eu;
    private final int duration;
    @SerializedName("fluid_inputs")
    private final List<FluidInput> fluidInputs = new ArrayList<>();
    @SerializedName("fluid_outputs")
    private final List<FluidOutput> fluidOutputs = new ArrayList<>();

    private MachineRecipeFactory(MachineRecipeType type, int eu, int duration) {
        this.type = type;
        this.eu = eu;
        this.duration = duration;
    }

    public static MachineRecipeFactory create(MachineRecipeType type, int eu, int duration) {
        return new MachineRecipeFactory(type, eu, duration);
    }

    public MachineRecipeFactory fluidInput(Fluid fluid, double amount) {
        fluidInputs.add(new FluidInput(Registry.FLUID.getId(fluid).toString(), amount));
        return this;
    }

    public MachineRecipeFactory fluidOutput(Fluid fluid, double amount) {
        fluidOutputs.add(new FluidOutput(Registry.FLUID.getId(fluid).toString(), amount));
        return this;
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipeId) {
        // note that FabricRecipesProvider will set the namespace to that of the mod anyway.
        exporter.accept(new MachineRecipeJsonProvider(new Identifier(recipeId)));
    }

    private static class FluidInput {
        final String fluid;
        final double amount;

        private FluidInput(String fluid, double amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    private static class FluidOutput {
        final String fluid;
        final double amount;

        private FluidOutput(String fluid, double amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    private class MachineRecipeJsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;

        private MachineRecipeJsonProvider(Identifier recipeId) {
            this.recipeId = recipeId;
        }

        @Override
        public void serialize(JsonObject json) {
            throw new UnsupportedOperationException("We override toJson()");
        }

        @Override
        public JsonObject toJson() {
            var object = GSON.toJsonTree(MachineRecipeFactory.this).getAsJsonObject();
            object.addProperty("type", Registry.RECIPE_SERIALIZER.getId(this.getSerializer()).toString());
            return object;
        }

        @Override
        public Identifier getRecipeId() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return type;
        }

        @Nullable
        @Override
        public JsonObject toAdvancementJson() {
            return null;
        }

        @Nullable
        @Override
        public Identifier getAdvancementId() {
            return null;
        }
    }
}
