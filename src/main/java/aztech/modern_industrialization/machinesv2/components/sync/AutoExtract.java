package aztech.modern_industrialization.machinesv2.components.sync;

import aztech.modern_industrialization.machinesv2.MachinePackets;
import aztech.modern_industrialization.machinesv2.SyncedComponent;
import aztech.modern_industrialization.machinesv2.SyncedComponents;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.TextHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AutoExtract {
    public static class Server implements SyncedComponent.Server<Data> {
        private final OrientationComponent orientation;

        public Server(OrientationComponent orientation) {
            this.orientation = orientation;
        }

        @Override
        public Data copyData() {
            return new Data(orientation.extractItems, orientation.extractFluids);
        }

        @Override
        public boolean needsSync(Data cachedData) {
            return cachedData.extractItems != orientation.extractItems || cachedData.extractFluids != orientation.extractFluids;
        }

        @Override
        public void writeInitialData(PacketByteBuf buf) {
            buf.writeBoolean(orientation.params.hasExtractItems);
            buf.writeBoolean(orientation.params.hasExtractFluids);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(PacketByteBuf buf) {
            buf.writeBoolean(orientation.extractItems);
            buf.writeBoolean(orientation.extractFluids);
        }

        @Override
        public Identifier getId() {
            return SyncedComponents.AUTO_EXTRACT;
        }

        public OrientationComponent getOrientation() {
            return orientation;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final boolean hasExtractItems, hasExtractFluids;
        boolean[] extractStatus = new boolean[2];

        public Client(PacketByteBuf buf) {
            hasExtractItems = buf.readBoolean();
            hasExtractFluids = buf.readBoolean();
            read(buf);
        }

        @Override
        public void read(PacketByteBuf buf) {
            extractStatus[0] = buf.readBoolean();
            extractStatus[1] = buf.readBoolean();
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new Renderer();
        }

        private class Renderer implements ClientComponentRenderer {
            @Override
            public void addButtons(ButtonContainer container) {
                if (hasExtractFluids) {
                    addExtractButton(container, false);
                }
                if (hasExtractItems) {
                    addExtractButton(container, true);
                }
            }

            private void addExtractButton(ButtonContainer container, boolean isItem) {
                int u = isItem ? 20 : 0;
                String type = isItem ? "item" : "fluid";
                int index = isItem ? 0 : 1;
                container.addButton(u, new LiteralText(type + " auto-extract"), syncId -> {
                    boolean newExtract = !extractStatus[index];
                    extractStatus[index] = newExtract;
                    PacketByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(syncId);
                    buf.writeBoolean(isItem);
                    buf.writeBoolean(newExtract);
                    ClientPlayNetworking.send(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                }, () -> {
                    List<Text> lines = new ArrayList<>();
                    if (extractStatus[index]) {
                        lines.add(new TranslatableText("text.modern_industrialization." + type + "_auto_extract_on"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_disable").setStyle(TextHelper.GRAY_TEXT));
                    } else {
                        lines.add(new TranslatableText("text.modern_industrialization." + type + "_auto_extract_off"));
                        lines.add(new TranslatableText("text.modern_industrialization.click_to_enable").setStyle(TextHelper.GRAY_TEXT));
                    }
                    return lines;
                }, () -> extractStatus[index]);
            }

            @Override
            public void renderBackground(DrawableHelper helper, MatrixStack matrices, int x, int y) {
            }
        }
    }

    private static class Data {
        public final boolean extractItems;
        public final boolean extractFluids;

        private Data(boolean extractItems, boolean extractFluids) {
            this.extractItems = extractItems;
            this.extractFluids = extractFluids;
        }
    }
}
