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
package aztech.modern_industrialization.datagen.model;

import aztech.modern_industrialization.machines.models.MachineCasing;
import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class MachineModelsToGenerate {
    static final Map<String, MachineModelProperties> props = new HashMap<>();

    public static void register(String machine, MachineCasing defaultCasing, String overlay, boolean front, boolean top, boolean side,
            boolean active) {
        props.put(machine, new MachineModelProperties(defaultCasing, overlay, front, top, side, active));
    }

    record MachineModelProperties(MachineCasing defaultCasing, String overlay, boolean front, boolean top, boolean side, boolean active) {
        void addToMachineJson(JsonObject obj) {
            obj.addProperty("casing", defaultCasing.name);

            var defaultOverlays = new JsonObject();

            if (top) {
                defaultOverlays.addProperty("top", "modern_industrialization:block/machines/%s/overlay_top".formatted(overlay));
                if (active) {
                    defaultOverlays.addProperty("top_active", "modern_industrialization:block/machines/%s/overlay_top_active".formatted(overlay));
                }
            }
            if (front) {
                defaultOverlays.addProperty("front", "modern_industrialization:block/machines/%s/overlay_front".formatted(overlay));
                if (active) {
                    defaultOverlays.addProperty("front_active",
                            "modern_industrialization:block/machines/%s/overlay_front_active".formatted(overlay));
                }
            }
            if (side) {
                defaultOverlays.addProperty("side", "modern_industrialization:block/machines/%s/overlay_side".formatted(overlay));
                if (active) {
                    defaultOverlays.addProperty("side_active", "modern_industrialization:block/machines/%s/overlay_side_active".formatted(overlay));
                }
            }

            defaultOverlays.addProperty("output", "modern_industrialization:block/overlays/output");
            defaultOverlays.addProperty("item_auto", "modern_industrialization:block/overlays/item_auto");
            defaultOverlays.addProperty("fluid_auto", "modern_industrialization:block/overlays/fluid_auto");

            obj.add("default_overlays", defaultOverlays);
        }
    }
}
