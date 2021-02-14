package aztech.modern_industrialization.machinesv2.components.sync;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import aztech.modern_industrialization.machinesv2.SyncedComponent;
import aztech.modern_industrialization.machinesv2.SyncedComponents;
import aztech.modern_industrialization.machinesv2.components.CrafterComponent;
import aztech.modern_industrialization.machinesv2.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

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
                return new Data(crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(), crafter.getCurrentRecipeEu(), crafter.getBaseRecipeEu());
            } else {
                return new Data();
            }
        }

        @Override
        public boolean needsSync(Data cachedData) {
            if (!cachedData.hasActiveRecipe) {
                return crafter.hasActiveRecipe();
            } else {
                return crafter.getEfficiencyTicks() != cachedData.efficiencyTicks
                        || crafter.getMaxEfficiencyTicks() != cachedData.maxEfficiencyTicks
                        || crafter.getCurrentRecipeEu() != cachedData.currentRecipeEu
                        || crafter.getBaseRecipeEu() != cachedData.baseRecipeEu;
            }
        }

        @Override
        public void writeInitialData(PacketByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            if (crafter.hasActiveRecipe()) {
                buf.writeBoolean(true);
                buf.writeInt(crafter.getEfficiencyTicks());
                buf.writeInt(crafter.getMaxEfficiencyTicks());
                buf.writeLong(crafter.getCurrentRecipeEu());
                buf.writeLong(crafter.getBaseRecipeEu());
            } else {
                buf.writeBoolean(false);
            }
        }

        @Override
        public Identifier getId() {
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

        public Client(PacketByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
            hasActiveRecipe = buf.readBoolean();
            if (hasActiveRecipe) {
                efficiencyTicks = buf.readInt();
                maxEfficiencyTicks = buf.readInt();
                currentRecipeEu = buf.readLong();
                baseRecipeEu = buf.readLong();
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        private static final Identifier TEXTURE = new MIIdentifier("textures/gui/efficiency_bar.png");
        private static final int WIDTH = 100, HEIGHT = 2;

        public class Renderer implements ClientComponentRenderer {
            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(TEXTURE);
                DrawableHelper.drawTexture(matrices, x+params.renderX-1, y+params.renderY-1, helper.getZOffset(), 0, 2, WIDTH+2, HEIGHT+2, 6, 102);
                if (hasActiveRecipe) {
                    int barPixels = (int) ((float) efficiencyTicks / maxEfficiencyTicks * WIDTH);
                    DrawableHelper.drawTexture(matrices, x+params.renderX, y+params.renderY, helper.getZOffset(), 0, 0, barPixels, HEIGHT, 6, 102);
                }
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                    List<Text> tooltip = new ArrayList<>();
                    if (hasActiveRecipe) {
                        DecimalFormat factorFormat = new DecimalFormat("#.#");
                        tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_ticks", efficiencyTicks, maxEfficiencyTicks));
                        tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_factor",
                                factorFormat.format((double) currentRecipeEu / baseRecipeEu)));
                    } else {
                        tooltip.add(new TranslatableText("text.modern_industrialization.efficiency_default_message"));
                    }
                    screen.renderTooltip(matrices, tooltip, cursorX, cursorY);
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

        private Data() {
            this.hasActiveRecipe = false;
            this.efficiencyTicks = 0;
            this.maxEfficiencyTicks = 0;
            this.currentRecipeEu = 0;
            this.baseRecipeEu = 0;
        }

        private Data(int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu) {
            this.efficiencyTicks = efficiencyTicks;
            this.maxEfficiencyTicks = maxEfficiencyTicks;
            this.hasActiveRecipe = true;
            this.currentRecipeEu = currentRecipeEu;
            this.baseRecipeEu = baseRecipeEu;
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
