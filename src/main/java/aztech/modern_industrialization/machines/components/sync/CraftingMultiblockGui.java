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
package aztech.modern_industrialization.machines.components.sync;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class CraftingMultiblockGui {
    public static class Server implements SyncedComponent.Server<Data> {

        private final CrafterComponent crafter;
        private final Supplier<Boolean> isShapeValid;
        private final Supplier<Float> progressSupplier;

        public Server(Supplier<Boolean> isShapeValid, Supplier<Float> progressSupplier, CrafterComponent crafter) {
            this.isShapeValid = isShapeValid;
            this.crafter = crafter;
            this.progressSupplier = progressSupplier;
        }

        @Override
        public Data copyData() {
            if (isShapeValid.get()) {
                if (crafter.hasActiveRecipe()) {
                    return new Data(progressSupplier.get(), crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(),
                            crafter.getCurrentRecipeEu(), crafter.getBaseRecipeEu());
                } else {
                    return new Data(true);
                }
            } else {
                return new Data();
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
                    || crafter.getMaxEfficiencyTicks() != cachedData.maxEfficiencyTicks || recipe;

        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
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

        }

        @Override
        public ResourceLocation getId() {
            return SyncedComponents.CRAFTING_MULTIBLOCK_GUI;
        }
    }

    public static class Client implements SyncedComponent.Client {
        public boolean isShapeValid;
        boolean hasActiveRecipe;
        float progress;
        int efficiencyTicks;
        int maxEfficiencyTicks;
        long currentRecipeEu;
        long baseRecipeEu;

        public Client(FriendlyByteBuf buf) {
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            isShapeValid = buf.readBoolean();
            if (isShapeValid) {
                hasActiveRecipe = buf.readBoolean();
                if (hasActiveRecipe) {
                    progress = buf.readFloat();
                    efficiencyTicks = buf.readInt();
                    maxEfficiencyTicks = buf.readInt();
                    currentRecipeEu = buf.readLong();
                    baseRecipeEu = buf.readLong();
                }
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {

            private final MIIdentifier texture = new MIIdentifier("textures/gui/container/multiblock_info.png");

            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {

                Minecraft minecraftClient = Minecraft.getInstance();
                RenderSystem.setShaderTexture(0, texture);
                GuiComponent.blit(matrices, x + X, y + Y, 0, 0, W, H, W, H);
                Font textRenderer = minecraftClient.font;

                textRenderer
                        .draw(matrices,
                                isShapeValid ? MIText.MultiblockShapeValid.text() : MIText.MultiblockShapeInvalid.text(),
                                x + 9, y + 23, isShapeValid ? 0xFFFFFF : 0xFF0000);
                if (isShapeValid) {
                    textRenderer.draw(matrices,

                            hasActiveRecipe ? MIText.MultiblockStatusActive.text() : MIText.MultiblockStatusActive.text(), x + 9, y + 34, 0xFFFFFF);
                    if (hasActiveRecipe) {
                        textRenderer.draw(matrices,
                                MIText.Progress.text(String.format("%.1f", progress * 100) + " %"),
                                x + 9,
                                y + 45, 0xFFFFFF);

                        textRenderer.draw(matrices,
                                MIText.EfficiencyTicks.text(efficiencyTicks, maxEfficiencyTicks),
                                x + 9,
                                y + 56, 0xFFFFFF);

                        textRenderer.draw(matrices,
                                MIText.BaseEuRecipe.text(
                                        TextHelper.getEuTextTick(baseRecipeEu)),
                                x + 9, y + 67, 0xFFFFFF);

                        textRenderer.draw(matrices,
                                MIText.CurrentEuRecipe.text(
                                        TextHelper.getEuTextTick(currentRecipeEu)),
                                x + 9, y + 78, 0xFFFFFF);
                    }
                }
            }

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

        private Data() {
            this(false);
        }

        private Data(boolean isShapeValid) {
            this.isShapeValid = isShapeValid;
            this.hasActiveRecipe = false;
            this.efficiencyTicks = 0;
            this.progress = 0;
            this.maxEfficiencyTicks = 0;
            this.currentRecipeEu = 0;
            this.baseRecipeEu = 0;
        }

        private Data(float progress, int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu) {
            this.efficiencyTicks = efficiencyTicks;
            this.progress = progress;
            this.maxEfficiencyTicks = maxEfficiencyTicks;
            this.isShapeValid = true;
            this.hasActiveRecipe = true;
            this.currentRecipeEu = currentRecipeEu;
            this.baseRecipeEu = baseRecipeEu;
        }
    }

    public static final int X = 4;
    public static final int Y = 16;
    public static final int W = 166;
    public static final int H = 80;
}
