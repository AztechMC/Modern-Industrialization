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
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class RecipeEfficiencyBar {
    public static class Server implements SyncedComponent.Server<Data> {
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
                return cachedData.hasActiveRecipe;
            } else {
                return crafter.getEfficiencyTicks() != cachedData.efficiencyTicks || crafter.getMaxEfficiencyTicks() != cachedData.maxEfficiencyTicks
                        || crafter.getCurrentRecipeEu() != cachedData.currentRecipeEu || crafter.getBaseRecipeEu() != cachedData.baseRecipeEu
                        || crafter.getBehavior().getMaxRecipeEu() != cachedData.maxRecipeEu;
            }
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
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
            return SyncedComponents.RECIPE_EFFICIENCY_BAR;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final Parameters params;
        boolean hasActiveRecipe;
        int efficiencyTicks;
        int maxEfficiencyTicks;
        long currentRecipeEu;
        long baseRecipeEu;
        long maxRecipeEu;

        public Client(FriendlyByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
            hasActiveRecipe = buf.readBoolean();
            if (hasActiveRecipe) {
                efficiencyTicks = buf.readInt();
                maxEfficiencyTicks = buf.readInt();
                currentRecipeEu = buf.readLong();
                baseRecipeEu = buf.readLong();
            }
            maxRecipeEu = buf.readLong();
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        private static final ResourceLocation TEXTURE = new MIIdentifier("textures/gui/efficiency_bar.png");
        private static final int WIDTH = 100, HEIGHT = 2;

        public class Renderer implements ClientComponentRenderer {
            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {
                RenderSystem.setShaderTexture(0, TEXTURE);
                GuiComponent.blit(matrices, x + params.renderX - 1, y + params.renderY - 1, helper.getBlitOffset(), 0, 2, WIDTH + 2, HEIGHT + 2,
                        102, 6);
                if (hasActiveRecipe) {
                    int barPixels = (int) ((float) efficiencyTicks / maxEfficiencyTicks * WIDTH);
                    GuiComponent.blit(matrices, x + params.renderX, y + params.renderY, helper.getBlitOffset(), 0, 0, barPixels, HEIGHT, 102,
                            6);
                }
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                    List<Component> tooltip = new ArrayList<>();
                    if (hasActiveRecipe) {
                        DecimalFormat factorFormat = new DecimalFormat("#.#");

                        tooltip.add(MIText.EfficiencyTicks.text(efficiencyTicks, maxEfficiencyTicks));
                        tooltip.add(MIText.EfficiencyFactor.text(factorFormat.format((double) currentRecipeEu / baseRecipeEu)));
                        tooltip.add(MIText.EfficiencyEu.text(currentRecipeEu));

                    } else {
                        tooltip.add(MIText.EfficiencyDefaultMessage.text());
                    }

                    tooltip.add(MIText.EfficiencyMaxOverclock.text(maxRecipeEu));

                    screen.renderComponentTooltip(matrices, tooltip, cursorX, cursorY);
                }
            }
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
