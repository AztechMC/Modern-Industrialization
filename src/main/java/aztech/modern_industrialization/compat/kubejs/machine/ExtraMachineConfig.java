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
package aztech.modern_industrialization.compat.kubejs.machine;

import aztech.modern_industrialization.machines.components.CustomOverclockComponent;
import aztech.modern_industrialization.machines.components.GunpowderOverclockComponent;
import aztech.modern_industrialization.machines.components.IOverclockComponent;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ExtraMachineConfig {

    public static class CraftingSingleBlock {
        private final SingleBlockCraftingMachines.Config config;

        public CraftingSingleBlock(SingleBlockCraftingMachines.Config config) { this.config = config; }
        public CraftingSingleBlock steamCustomOverclock(double multiplier, JsonObject object) {
            var catalysts = new ArrayList<CustomOverclockComponent.Catalyst>();

            for (var entry : object.entrySet()) {
                catalysts.add(new CustomOverclockComponent.Catalyst(new ResourceLocation(entry.getKey()), entry.getValue().getAsInt()));
            }
            config.steamOverclockComponent = new CustomOverclockComponent(multiplier, catalysts);
            return this;
        }
    }
    public static class CraftingMultiBlock {
        public IOverclockComponent steamOverclockComponent = new GunpowderOverclockComponent();
        public Consumer<MultiblockMachines.Rei> reiConfig = rei -> { };

        public CraftingMultiBlock steamCustomOverclock(double multiplier, JsonObject object) {
            var catalysts = new ArrayList<CustomOverclockComponent.Catalyst>();

            for (var entry : object.entrySet()) {
                catalysts.add(new CustomOverclockComponent.Catalyst(new ResourceLocation(entry.getKey()), entry.getValue().getAsInt()));
            }
            steamOverclockComponent = new CustomOverclockComponent(multiplier, catalysts);
            return this;
        }

        public CraftingMultiBlock reiExtra(Consumer<MultiblockMachines.Rei> consumer) {
            reiConfig = consumer;
            return this;
        }
    }
}
