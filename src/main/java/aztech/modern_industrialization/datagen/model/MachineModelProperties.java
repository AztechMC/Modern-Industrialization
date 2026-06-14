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
import net.minecraft.resources.ResourceLocation;

public record MachineModelProperties(
        MachineCasing casing,
        Map<String, ResourceLocation> defaultOverlays,
        boolean noOverlayOnOutputSide) {
    public MachineModelProperties(MachineCasing casing, Map<String, ResourceLocation> defaultOverlays, boolean noOverlayOnOutputSide) {
        this.casing = casing;
        this.defaultOverlays = Map.copyOf(defaultOverlays);
        this.noOverlayOnOutputSide = noOverlayOnOutputSide;
    }

    public void addToMachineJson(JsonObject obj) {
        obj.addProperty("casing", casing.key.getPath());

        var defaultOverlays = new JsonObject();
        for (var entry : this.defaultOverlays.entrySet()) {
            defaultOverlays.addProperty(entry.getKey(), entry.getValue().toString());
        }
        obj.add("default_overlays", defaultOverlays);

        if (noOverlayOnOutputSide) {
            obj.addProperty("no_overlay_on_output_side", true);
        }
    }

    public static class Builder {
        private final MachineCasing casing;
        private final Map<String, ResourceLocation> defaultOverlays;
        private boolean noOverlayOnOutputSide = false;

        public Builder(MachineCasing casing) {
            this.casing = casing;
            this.defaultOverlays = new HashMap<>();
        }

        public Builder addOverlay(String name, ResourceLocation texture) {
            defaultOverlays.put(name, texture);
            return this;
        }

        public Builder noOverlayOnOutputSide() {
            noOverlayOnOutputSide = true;
            return this;
        }

        public MachineModelProperties build() {
            return new MachineModelProperties(casing, defaultOverlays, noOverlayOnOutputSide);
        }
    }
}
