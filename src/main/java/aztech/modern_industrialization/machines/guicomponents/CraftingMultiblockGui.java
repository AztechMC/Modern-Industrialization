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
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CraftingMultiblockGui {
    public static class Server implements GuiComponent.Server<Data> {
        private final CrafterComponent crafter;
        private final Supplier<Boolean> isShapeValid;
        private final Supplier<Float> progressSupplier;
        private final IntSupplier remainingOverclockTicks;

        @Deprecated(forRemoval = true)
        public Server(Supplier<Boolean> isShapeValid, Supplier<Float> progressSupplier, CrafterComponent crafter) {
            this(isShapeValid, progressSupplier, crafter, () -> 0);
        }

        public Server(Supplier<Boolean> isShapeValid, Supplier<Float> progressSupplier, CrafterComponent crafter,
                IntSupplier remainingOverclockTicks) {
            this.isShapeValid = isShapeValid;
            this.crafter = crafter;
            this.progressSupplier = progressSupplier;
            this.remainingOverclockTicks = remainingOverclockTicks;
        }

        @Override
        public Data copyData() {
            if (isShapeValid.get()) {
                if (crafter.hasActiveRecipe()) {
                    return new Data(progressSupplier.get(), crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(),
                            crafter.getCurrentRecipeEu(), crafter.getBaseRecipeEu(), remainingOverclockTicks.getAsInt());
                } else {
                    return new Data(true, remainingOverclockTicks.getAsInt());
                }
            } else {
                return new Data(remainingOverclockTicks.getAsInt());
            }
        }

        @Override
        public boolean needsSync(Data cachedData) {
            boolean recipe = false;

            if (crafter.hasActiveRecipe()) {
                recipe = crafter.getCurrentRecipeEu() != cachedData.currentRecipeEu || crafter.getBaseRecipeEu() != cachedData.baseRecipeEu;
            }
            return cachedData.isShapeValid != isShapeValid.get() || cachedData.hasActiveRecipe != crafter.hasActiveRecipe()
                    || cachedData.progress != progressSupplier.get() || crafter.getEfficiencyTicks() != cachedData.efficiencyTicks
                    || crafter.getMaxEfficiencyTicks() != cachedData.maxEfficiencyTicks || recipe
                    || cachedData.remainingOverclockTicks != remainingOverclockTicks.getAsInt();
        }

        @Override
        public void writeInitialData(RegistryFriendlyByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(RegistryFriendlyByteBuf buf) {
            if (isShapeValid.get()) {
                buf.writeBoolean(true);
                if (crafter.hasActiveRecipe()) {
                    buf.writeBoolean(true);
                    buf.writeFloat(progressSupplier.get());
                    buf.writeInt(crafter.getEfficiencyTicks());
                    buf.writeInt(crafter.getMaxEfficiencyTicks());
                    buf.writeLong(crafter.getCurrentRecipeEu());
                    buf.writeLong(crafter.getBaseRecipeEu());
                } else {
                    buf.writeBoolean(false);
                }
            } else {
                buf.writeBoolean(false);
            }
            buf.writeVarInt(remainingOverclockTicks.getAsInt());
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.CRAFTING_MULTIBLOCK_GUI;
        }
    }

    private static class Data {
        final boolean isShapeValid;
        final boolean hasActiveRecipe;
        final float progress;
        final int efficiencyTicks;
        final int maxEfficiencyTicks;
        final long currentRecipeEu;
        final long baseRecipeEu;
        final int remainingOverclockTicks;

        private Data(int remainingOverclockTicks) {
            this(false, remainingOverclockTicks);
        }

        private Data(boolean isShapeValid, int remainingOverclockTicks) {
            this.isShapeValid = isShapeValid;
            this.hasActiveRecipe = false;
            this.efficiencyTicks = 0;
            this.progress = 0;
            this.maxEfficiencyTicks = 0;
            this.currentRecipeEu = 0;
            this.baseRecipeEu = 0;
            this.remainingOverclockTicks = remainingOverclockTicks;
        }

        private Data(float progress, int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu,
                int remainingOverclockTicks) {
            this.efficiencyTicks = efficiencyTicks;
            this.progress = progress;
            this.maxEfficiencyTicks = maxEfficiencyTicks;
            this.isShapeValid = true;
            this.hasActiveRecipe = true;
            this.currentRecipeEu = currentRecipeEu;
            this.baseRecipeEu = baseRecipeEu;
            this.remainingOverclockTicks = remainingOverclockTicks;
        }
    }

    public static final int X = 5;
    public static final int Y = 16;
    public static final int W = 166;
    public static final int H = 80;
}
