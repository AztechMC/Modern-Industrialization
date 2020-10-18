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
package aztech.modern_industrialization.model;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.machines.impl.MachineModel;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.util.Identifier;

public class Models {
    public static class BlockModels {
        public static final MachineModel STEAM_BOILER = new MachineModel("steam_boiler", new MIIdentifier("blocks/casings/steam/bricked_bronze/"))
                .withFrontOverlay(new MIIdentifier("blocks/generators/boiler/coal/overlay_front"),
                        new MIIdentifier("blocks/generators/boiler/coal/overlay_front_active"));
    }

    public static Map<Identifier, UnbakedModel> getModelMap() {
        Map<Identifier, UnbakedModel> modelMap = new HashMap<>();
        Field[] declaredFields = BlockModels.class.getDeclaredFields();
        for (Field field : declaredFields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                try {
                    MachineModel model = (MachineModel) field.get(null);
                    modelMap.put(new MIIdentifier("block/" + model.model_name), model);
                    modelMap.put(new MIIdentifier("item/" + model.model_name), model);
                } catch (IllegalAccessException e) {
                    ModernIndustrialization.LOGGER.error("Exception:", e);
                }
            }
        }
        return modelMap;
    }
}
