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

import aztech.modern_industrialization.MIStartup;
import aztech.modern_industrialization.compat.kubejs.machine.MIMachineKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.material.MIMaterialKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.recipe.MIRecipeKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.recipe.MachineRecipeSchema;
import aztech.modern_industrialization.compat.kubejs.registration.MIRegistrationKubeJSEvents;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.condition.CustomProcessCondition;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;

public class MIKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
    }

    @Override
    public void registerEvents() {
        MIMachineKubeJSEvents.EVENT_GROUP.register();
        MIMaterialKubeJSEvents.EVENT_GROUP.register();
        MIRecipeKubeJSEvents.EVENT_GROUP.register();
        MIRegistrationKubeJSEvents.EVENT_GROUP.register();
    }

    @Override
    public void initStartup() {
        KubeJSProxy.instance = new LoadedKubeJSProxy();
        MIStartup.onKubejsPluginLoaded();
    }

    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        for (var mrt : MIMachineRecipeTypes.getRecipeTypes()) {
            event.register(mrt.getId(),
                    mrt == MIMachineRecipeTypes.FORGE_HAMMER ? MachineRecipeSchema.FORGE_HAMMER_SCHEMA : MachineRecipeSchema.SCHEMA);
        }
    }

    @Override
    public void onServerReload() {
        CustomProcessCondition.onReload();
    }
}
