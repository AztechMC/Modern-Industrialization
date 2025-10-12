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

package aztech.modern_industrialization.compat.kubejs;

import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.compat.kubejs.machine.MIMachineKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.material.MIMaterialKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.recipe.FluidInputComponent;
import aztech.modern_industrialization.compat.kubejs.recipe.FluidOutputComponent;
import aztech.modern_industrialization.compat.kubejs.recipe.ItemInputComponent;
import aztech.modern_industrialization.compat.kubejs.recipe.ItemOutputComponent;
import aztech.modern_industrialization.compat.kubejs.recipe.MIRecipeKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.recipe.MachineKubeRecipe;
import aztech.modern_industrialization.compat.kubejs.recipe.MachineProcessConditionComponent;
import aztech.modern_industrialization.compat.kubejs.recipe.MachineRecipeSchema;
import aztech.modern_industrialization.compat.kubejs.registration.MIRegistrationKubeJSEvents;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.condition.CustomProcessCondition;
import com.google.gson.JsonElement;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeFactoryRegistry;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class MIKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void init() {
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(MIMachineKubeJSEvents.EVENT_GROUP);
        registry.register(MIMaterialKubeJSEvents.EVENT_GROUP);
        registry.register(MIRecipeKubeJSEvents.EVENT_GROUP);
        registry.register(MIRegistrationKubeJSEvents.EVENT_GROUP);
    }

    @Override
    public void initStartup() {
        KubeJSProxy.instance = new LoadedKubeJSProxy();
    }

    @Override
    public void registerRecipeSchemas(RecipeSchemaRegistry registry) {
        for (var mrt : MIMachineRecipeTypes.getRecipeTypes()) {
            registry.register(mrt.getId(), MachineRecipeSchema.SCHEMA);
        }
        registry.register(MIRegistries.FORGE_HAMMER_RECIPE_TYPE.getId(), MachineRecipeSchema.FORGE_HAMMER_SCHEMA);
    }

    @Override
    public void registerRecipeFactories(RecipeFactoryRegistry registry) {
        registry.register(MachineKubeRecipe.FACTORY);
    }

    @Override
    public void registerRecipeComponents(RecipeComponentFactoryRegistry registry) {
        registry.register(ItemOutputComponent.ITEM_OUTPUT);
        registry.register(FluidOutputComponent.FLUID_OUTPUT);
        registry.register(ItemInputComponent.ITEM_INPUT);
        registry.register(FluidInputComponent.FLUID_INPUT);
        registry.register(MachineProcessConditionComponent.MACHINE_PROCESS_CONDITION);
    }

    @Override
    public void beforeRecipeLoading(RecipesKubeEvent event, RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> recipeJsons) {
        CustomProcessCondition.onReload();
    }
}
