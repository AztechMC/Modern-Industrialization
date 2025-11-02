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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.models.MachineCasing;
import java.util.HashMap;
import java.util.Map;

public class MachineModelsToGenerate {
    public static final Map<String, MachineModelProperties> props = new HashMap<>();

    public static void register(String machine, MachineModelProperties model) {
        props.put(machine, model);
    }

    public static void register(String machine, MachineCasing defaultCasing, String overlay, boolean front, boolean top, boolean side,
            boolean active) {
        var builder = new MachineModelProperties.Builder(defaultCasing);

        if (top) {
            builder.addOverlay("top", MI.id("block/machines/%s/overlay_top".formatted(overlay)));
            if (active) {
                builder.addOverlay("top_active", MI.id("block/machines/%s/overlay_top_active".formatted(overlay)));
            }
        }
        if (front) {
            builder.addOverlay("front", MI.id("block/machines/%s/overlay_front".formatted(overlay)));
            if (active) {
                builder.addOverlay("front_active", MI.id("block/machines/%s/overlay_front_active".formatted(overlay)));
            }
        }
        if (side) {
            builder.addOverlay("side", MI.id("block/machines/%s/overlay_side".formatted(overlay)));
            if (active) {
                builder.addOverlay("side_active", MI.id("block/machines/%s/overlay_side_active".formatted(overlay)));
            }
        }

        builder.addOverlay("output", MI.id("block/overlays/output"));
        builder.addOverlay("item_auto", MI.id("block/overlays/item_auto"));
        builder.addOverlay("fluid_auto", MI.id("block/overlays/fluid_auto"));

        register(machine, builder.build());
    }
}
