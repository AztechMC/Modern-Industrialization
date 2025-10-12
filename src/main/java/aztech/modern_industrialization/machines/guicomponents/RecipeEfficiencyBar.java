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

package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RecipeEfficiencyBar {
    public static class Server implements GuiComponent.Server<Data> {
        private final Parameters params;
        private final CrafterComponent crafter;

        public Server(Parameters params, CrafterComponent crafter) {
            this.params = params;
            this.crafter = crafter;
        }

        @Override
        public Data copyData() {
            if (crafter.hasActiveRecipe()) {
                return new Data(crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(), crafter.getCurrentRecipeEu(),
                        crafter.getBaseRecipeEu(), crafter.getBehavior().getMaxRecipeEu());
            } else {
                return new Data();
            }
        }

        @Override
        public boolean needsSync(Data cachedData) {
            if (!crafter.hasActiveRecipe()) {
                return cachedData.hasActiveRecipe || crafter.getBehavior().getMaxRecipeEu() != cachedData.maxRecipeEu;
            } else {
                return crafter.getEfficiencyTicks() != cachedData.efficiencyTicks || crafter.getMaxEfficiencyTicks() != cachedData.maxEfficiencyTicks
                        || crafter.getCurrentRecipeEu() != cachedData.currentRecipeEu || crafter.getBaseRecipeEu() != cachedData.baseRecipeEu
                        || crafter.getBehavior().getMaxRecipeEu() != cachedData.maxRecipeEu;
            }
        }

        @Override
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
            if (crafter.hasActiveRecipe()) {
                buf.writeBoolean(true);
                buf.writeInt(crafter.getEfficiencyTicks());
                buf.writeInt(crafter.getMaxEfficiencyTicks());
                buf.writeLong(crafter.getCurrentRecipeEu());
                buf.writeLong(crafter.getBaseRecipeEu());
            } else {
                buf.writeBoolean(false);
            }
            buf.writeLong(crafter.getBehavior().getMaxRecipeEu());
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.RECIPE_EFFICIENCY_BAR;
        }
    }

    private static class Data {
        final boolean hasActiveRecipe;
        final int efficiencyTicks;
        final int maxEfficiencyTicks;
        final long currentRecipeEu;
        final long baseRecipeEu;
        final long maxRecipeEu;

        private Data() {
            this.hasActiveRecipe = false;
            this.efficiencyTicks = 0;
            this.maxEfficiencyTicks = 0;
            this.currentRecipeEu = 0;
            this.baseRecipeEu = 0;
            this.maxRecipeEu = 0;
        }

        private Data(int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu, long maxRecipeEu) {
            this.efficiencyTicks = efficiencyTicks;
            this.maxEfficiencyTicks = maxEfficiencyTicks;
            this.hasActiveRecipe = true;
            this.currentRecipeEu = currentRecipeEu;
            this.baseRecipeEu = baseRecipeEu;
            this.maxRecipeEu = maxRecipeEu;
        }
    }

    public static class Parameters {
        public final int renderX, renderY;

        public Parameters(int renderX, int renderY) {
            this.renderX = renderX;
            this.renderY = renderY;
        }
    }
}
