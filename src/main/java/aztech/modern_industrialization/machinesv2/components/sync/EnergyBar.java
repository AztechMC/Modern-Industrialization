package aztech.modern_industrialization.machinesv2.components.sync;

import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import aztech.modern_industrialization.machinesv2.SyncedComponent;
import aztech.modern_industrialization.machinesv2.SyncedComponents;
import aztech.modern_industrialization.machinesv2.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.function.Supplier;

public class EnergyBar {
    public static class Server implements SyncedComponent.Server<Data> {
        public final Parameters params;
        public final Supplier<Long> euSupplier, maxEuSupplier;

        public Server(Parameters params, Supplier<Long> euSupplier, Supplier<Long> maxEuSupplier) {
            this.params = params;
            this.euSupplier = euSupplier;
            this.maxEuSupplier = maxEuSupplier;
        }

        @Override
        public Data copyData() {
            return new Data(euSupplier.get(), maxEuSupplier.get());
        }

        @Override
        public boolean needsSync(Data cachedData) {
            return cachedData.eu != euSupplier.get() || cachedData.maxEu != maxEuSupplier.get();
        }

        @Override
        public void writeInitialData(PacketByteBuf buf) {
            buf.writeInt(params.renderX);
            buf.writeInt(params.renderY);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            buf.writeLong(euSupplier.get());
            buf.writeLong(maxEuSupplier.get());
        }

        @Override
        public Identifier getId() {
            return SyncedComponents.ENERGY_BAR;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final Parameters params;
        long eu, maxEu;

        public Client(PacketByteBuf buf) {
            this.params = new Parameters(buf.readInt(), buf.readInt());
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
            eu = buf.readLong();
            maxEu = buf.readLong();
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        public class Renderer implements ClientComponentRenderer {
            private static final int WIDTH = 13;
            private static final int HEIGHT = 18;

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
                MinecraftClient.getInstance().getTextureManager().bindTexture(MachineScreenHandlers.SLOT_ATLAS);
                int px = x + params.renderX;
                int py = y + params.renderY;
                helper.drawTexture(matrices, px, py, 230, 0, WIDTH, HEIGHT);
                float fill = (float) eu / maxEu;
                int fillPixels = (int) (fill * HEIGHT);
                if (fill > 0.95)
                    fillPixels = HEIGHT;
                helper.drawTexture(matrices, px, py + HEIGHT - fillPixels, 243, HEIGHT - fillPixels, WIDTH, fillPixels);
            }

            @Override
            public void renderTooltip(MachineScreenHandlers.ClientScreen screen, MatrixStack matrices, int x, int y, int cursorX, int cursorY) {
                if (RenderHelper.isPointWithinRectangle(params.renderX, params.renderY, WIDTH, HEIGHT, cursorX - x, cursorY - y)) {
                    Text tooltip = new TranslatableText("text.modern_industrialization.energy_bar", eu, maxEu);
                    screen.renderTooltip(matrices, Collections.singletonList(tooltip), cursorX, cursorY);
                }
            }
        }
    }

    private static class Data {
        final long eu;
        final long maxEu;

        Data(long eu, long maxEu) {
            this.eu = eu;
            this.maxEu = maxEu;
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
