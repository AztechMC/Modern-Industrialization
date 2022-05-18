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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.SyncedComponent;
import aztech.modern_industrialization.machines.SyncedComponents;
import aztech.modern_industrialization.machines.components.OrientationComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.util.TextHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Supports both auto-extract and auto-insert. Auto-insert is just a GUI change,
 * but the logic stays the same.
 */
public class AutoExtract {
    public static class Server implements SyncedComponent.Server<Data> {
        private final OrientationComponent orientation;
        private final boolean displayAsInsert; // true for auto-insert

        public Server(OrientationComponent orientation, boolean displayAsInsert) {
            this.orientation = orientation;
            this.displayAsInsert = displayAsInsert;
        }

        public Server(OrientationComponent orientation) {
            this(orientation, false);
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
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeBoolean(displayAsInsert);
            buf.writeBoolean(orientation.params.hasExtractItems);
            buf.writeBoolean(orientation.params.hasExtractFluids);
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
            buf.writeBoolean(orientation.extractItems);
            buf.writeBoolean(orientation.extractFluids);
        }

        @Override
        public ResourceLocation getId() {
            return SyncedComponents.AUTO_EXTRACT;
        }

        public OrientationComponent getOrientation() {
            return orientation;
        }
    }

    public static class Client implements SyncedComponent.Client {
        final boolean displayAsInsert;
        final boolean hasExtractItems, hasExtractFluids;
        boolean[] extractStatus = new boolean[2];

        public Client(FriendlyByteBuf buf) {
            displayAsInsert = buf.readBoolean();
            hasExtractItems = buf.readBoolean();
            hasExtractFluids = buf.readBoolean();
            read(buf);
        }

        @Override
        public void read(FriendlyByteBuf buf) {
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
                String insertOrExtract = displayAsInsert ? "insert" : "extract";
                container.addButton(u, new TextComponent(type + " auto-extract"), syncId -> {
                    boolean newExtract = !extractStatus[index];
                    extractStatus[index] = newExtract;
                    FriendlyByteBuf buf = PacketByteBufs.create();
                    buf.writeInt(syncId);
                    buf.writeBoolean(isItem);
                    buf.writeBoolean(newExtract);
                    ClientPlayNetworking.send(MachinePackets.C2S.SET_AUTO_EXTRACT, buf);
                }, () -> {
                    List<Component> lines = new ArrayList<>();
                    if (extractStatus[index]) {
                        Component component;

                        if (isItem) {
                            if (displayAsInsert) {
                                component = MIText.ItemAutoInsertOn.text();
                            } else {
                                component = MIText.ItemAutoExtractOn.text();
                            }
                        } else {
                            if (displayAsInsert) {
                                component = MIText.FluidAutoInsertOn.text();
                            } else {
                                component = MIText.FluidAutoExtractOn.text();
                            }
                        }
                        lines.add(component);
                        lines.add(MIText.ClickToDisable.text().setStyle(TextHelper.GRAY_TEXT));
                    } else {
                        Component component;
                        if (isItem) {
                            if (displayAsInsert) {
                                component = MIText.ItemAutoInsertOff.text();
                            } else {
                                component = MIText.ItemAutoExtractOff.text();
                            }
                        } else {
                            if (displayAsInsert) {
                                component = MIText.FluidAutoInsertOff.text();
                            } else {
                                component = MIText.FluidAutoExtractOff.text();
                            }
                        }

                        lines.add(component);
                        lines.add(MIText.ClickToEnable.text().setStyle(TextHelper.GRAY_TEXT));
                    }
                    return lines;
                }, () -> extractStatus[index]);
            }

            @Override
            public void renderBackground(GuiComponent helper, PoseStack matrices, int x, int y) {
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
