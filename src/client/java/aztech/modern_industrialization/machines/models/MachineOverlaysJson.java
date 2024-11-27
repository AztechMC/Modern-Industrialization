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
package aztech.modern_industrialization.machines.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.lang.reflect.Field;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public interface MachineOverlaysJson {
    Material[] toSpriteIds();

    int[] getOutputSpriteIndexes();

    Gson GSON = new GsonBuilder().registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).create();

    static <O extends MachineOverlaysJson> O parse(Class<O> clazz, JsonObject json, MachineOverlaysJson defaultOverlay) {
        O overlays = GSON.fromJson(json, clazz);

        if (defaultOverlay != null) {
            try {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.get(overlays) == null) {
                        field.set(overlays, field.get(defaultOverlay));
                    }
                }
            } catch (IllegalAccessException ex) {
                throw new RuntimeException("Failed to copy fields from default overlay", ex);
            }
        }

        return overlays;
    }

    default Material select(ResourceLocation... candidates) {
        for (ResourceLocation id : candidates) {
            if (id != null) {
                return new Material(InventoryMenu.BLOCK_ATLAS, id);
            }
        }
        return null;
    }
}
