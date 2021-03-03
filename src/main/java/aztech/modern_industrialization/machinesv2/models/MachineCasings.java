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
package aztech.modern_industrialization.machinesv2.models;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.mixin_client.BakedModelManagerAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;

public class MachineCasings {
    public static final MachineCasingModel BRONZE = new MachineCasingModel("bronze");
    public static final MachineCasingModel STEEL = new MachineCasingModel("steel");
    public static final MachineCasingModel BRICKED_BRONZE = new MachineCasingModel("bricked_bronze");
    public static final MachineCasingModel BRICKED_STEEL = new MachineCasingModel("bricked_steel");
    public static final MachineCasingModel STEEL_CRATE = new MachineCasingModel("steel_crate");
    public static final MachineCasingModel LV = new MachineCasingModel("lv");
    public static final MachineCasingModel MV = new MachineCasingModel("mv");
    public static final MachineCasingModel HV = new MachineCasingModel("hv");
    public static final MachineCasingModel EV = new MachineCasingModel("ev");
    public static final MachineCasingModel SUPRACONDUCTOR = new MachineCasingModel("supraconductor");
    public static final MachineCasingModel BRICKS = new MachineCasingModel("bricks");

    public static MachineCasingModel casingFromCableTier(CableTier tier) {
        if (tier == CableTier.LV) {
            return LV;
        } else if (tier == CableTier.MV) {
            return MV;
        } else if (tier == CableTier.HV) {
            return HV;
        } else if (tier == CableTier.EV) {
            return EV;
        } else if (tier == CableTier.SUPRACONDUCTOR) {
            return SUPRACONDUCTOR;
        }
        return null;
    }

    public static MachineCasingModel get(String folder) {
        BakedModelManager bmm = MinecraftClient.getInstance().getBakedModelManager();
        BakedModel bm = ((BakedModelManagerAccessor) bmm).getModels().get(new MIIdentifier("machine_casing/" + folder));
        if (bm instanceof MachineCasingModel) {
            return (MachineCasingModel) bm;
        } else {
            throw new IllegalArgumentException("Machine casing model \"" + folder + "\" does not exist.");
        }
    }
}
