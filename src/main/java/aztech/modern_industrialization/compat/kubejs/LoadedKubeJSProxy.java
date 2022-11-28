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

import aztech.modern_industrialization.compat.kubejs.machine.MIMachineKubeJSEvents;
import aztech.modern_industrialization.compat.kubejs.machine.RegisterMachinesEventJS;
import aztech.modern_industrialization.compat.kubejs.machine.RegisterRecipeTypesEventJS;
import aztech.modern_industrialization.compat.kubejs.material.AddMaterialsEventJS;
import aztech.modern_industrialization.compat.kubejs.material.MIMaterialKubeJSEvents;

public class LoadedKubeJSProxy extends KubeJSProxy {
    @Override
    public void fireAddMaterialsEvent() {
        MIMaterialKubeJSEvents.ADD_MATERIALS.post(new AddMaterialsEventJS());
    }

    @Override
    public void fireRegisterRecipeTypesEvent() {
        MIMachineKubeJSEvents.REGISTER_RECIPE_TYPES.post(new RegisterRecipeTypesEventJS());
    }

    @Override
    public void fireRegisterMachinesEvent() {
        MIMachineKubeJSEvents.REGISTER_MACHINES.post(new RegisterMachinesEventJS());
    }
}
