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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.Rectangle;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ShapeSelection {
    public interface Behavior {
        /**
         * @param delta +1 if clicked on right button, -1 if clicked on left button
         */
        void handleClick(int clickedLine, int delta);

        int getCurrentIndex(int line);
    }

    /**
     * @param useArrows True for arrows {@code < and >}, false for +/-.
     */
    public record LineInfo(int numValues, List<? extends Component> translations, boolean useArrows) {
        public LineInfo {
            Preconditions.checkArgument(numValues == translations.size());
        }
    }

    public static class Server implements GuiComponent.Server<int[]> {
        public final Behavior behavior;
        private final List<LineInfo> lines;

        public Server(Behavior behavior, LineInfo... lines) {
            Preconditions.checkArgument(lines.length > 0);

            this.behavior = behavior;
            this.lines = List.of(lines);
        }

        @Override
        public int[] copyData() {
            return IntStream.range(0, lines.size()).map(behavior::getCurrentIndex).toArray();
        }

        @Override
        public boolean needsSync(int[] cachedData) {
            for (int i = 0; i < lines.size(); ++i) {
                if (cachedData[i] != behavior.getCurrentIndex(i)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeVarInt(lines.size());
            for (var line : lines) {
                buf.writeVarInt(line.numValues);
                for (var component : line.translations) {
                    buf.writeComponent(component);
                }
                buf.writeBoolean(line.useArrows);
            }
            writeCurrentData(buf);
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
            for (int i = 0; i < lines.size(); ++i) {
                buf.writeVarInt(behavior.getCurrentIndex(i));
            }
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.SHAPE_SELECTION;
        }
    }

    public static class Client implements GuiComponent.Client {
        private final LineInfo[] lines;
        private final int[] currentData;

        public Client(FriendlyByteBuf buf) {
            lines = new LineInfo[buf.readVarInt()];
            for (int i = 0; i < lines.length; ++i) {
                int numValues = buf.readVarInt();
                List<Component> components = new ArrayList<>();
                for (int j = 0; j < numValues; ++j) {
                    components.add(buf.readComponent());
                }
                lines[i] = new LineInfo(numValues, components, buf.readBoolean());
            }
            currentData = new int[lines.length];

            readCurrentData(buf);
        }

        @Override
        public void readCurrentData(FriendlyByteBuf buf) {
            for (int i = 0; i < currentData.length; ++i) {
                currentData[i] = buf.readVarInt();
            }
        }

        @Override
        public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
            // Compute the max width of all the components!
            int maxWidth = 1;
            for (var line : lines) {
                for (var tooltip : line.translations) {
                    maxWidth = Math.max(maxWidth, Minecraft.getInstance().font.width(tooltip));
                }
            }
            int textMaxWidth = maxWidth;

            return new ClientComponentRenderer() {
                private boolean isPanelOpen = false;
                private final int btnSize = 12;
                private final int borderSize = 3;
                private final int outerPadding = 5;
                private final int innerPadding = 5;
                private final int panelWidth = borderSize + outerPadding + btnSize + innerPadding + textMaxWidth + innerPadding + btnSize
                        + outerPadding;

                private static int getVerticalPos(int lineId) {
                    return 46 + 16 * lineId;
                }

                @Override
                public void addButtons(ButtonContainer container) {
                    // Two buttons per line
                    for (int i = 0; i < lines.length; ++i) {
                        int iCopy = i;
                        var line = lines[i];
                        int baseU = line.useArrows ? 174 : 150;
                        int v = 58;

                        // Left button
                        container.addButton(-panelWidth + borderSize + outerPadding, getVerticalPos(i), btnSize, btnSize, syncId -> {
                            ClientPlayNetworking.send(MachinePackets.C2S.CHANGE_SHAPE, MachinePackets.C2S.encodeChangeShape(syncId, iCopy, true));
                        }, List::of, (screen, button, matrices, mouseX, mouseY, delta) -> {
                            if (currentData[iCopy] == 0) {
                                screen.blitButtonNoHighlight(button, matrices, baseU, v + 12, mouseX, mouseY);
                            } else {
                                screen.blitButtonSmall(button, matrices, baseU, v, mouseX, mouseY);
                            }
                        }, () -> isPanelOpen);

                        // Right button
                        container.addButton(-btnSize - outerPadding, getVerticalPos(i), btnSize, btnSize, syncId -> {
                            ClientPlayNetworking.send(MachinePackets.C2S.CHANGE_SHAPE, MachinePackets.C2S.encodeChangeShape(syncId, iCopy, false));
                        }, List::of, (screen, button, matrices, mouseX, mouseY, delta) -> {
                            if (currentData[iCopy] == line.numValues - 1) {
                                screen.blitButtonNoHighlight(button, matrices, baseU + 12, v + 12, mouseX, mouseY);
                            } else {
                                screen.blitButtonSmall(button, matrices, baseU + 12, v, mouseX, mouseY);
                            }
                        }, () -> isPanelOpen);
                    }

                    // Big button to open panel
                    container.addButton(-24, 17,
                            20, 20, syncId -> isPanelOpen = !isPanelOpen, () -> List.of(
                                    MIText.ShapeSelectionTitle.text(),
                                    MIText.ShapeSelectionDescription.text().setStyle(TextHelper.GRAY_TEXT)),
                            (screen, button, matrices, mouseX, mouseY, delta) -> screen.blitButton(button, matrices, 138, 38, mouseX, mouseY));
                }

                @Override
                public void renderBackground(net.minecraft.client.gui.GuiComponent helper, PoseStack matrices, int leftPos, int topPos) {
                    RenderSystem.setShaderTexture(0, MachineScreen.BACKGROUND);
                    var box = getBox(leftPos, topPos);

                    helper.blit(matrices, box.x(), box.y(), 0, 0, box.w(), box.h() - 4);
                    helper.blit(matrices, box.x(), box.y() + box.h() - 4, 0, 252, box.w(), 4);

                    if (isPanelOpen) {
                        RenderSystem.disableDepthTest();
                        for (int i = 0; i < lines.length; ++i) {
                            var line = lines[i];
                            var tooltip = line.translations.get(currentData[i]);
                            var width = Minecraft.getInstance().font.width(tooltip);
                            Minecraft.getInstance().font.draw(matrices, tooltip,
                                    box.x() + borderSize + outerPadding + btnSize + innerPadding + (textMaxWidth - width) / 2f,
                                    topPos + getVerticalPos(i) + 2, 0x404040);
                        }
                        RenderSystem.enableDepthTest();
                    }
                }

                public Rectangle getBox(int leftPos, int topPos) {
                    if (isPanelOpen) {
                        int topOffset = 10;
                        return new Rectangle(leftPos - panelWidth, topPos + topOffset, panelWidth,
                                getVerticalPos(lines.length - 1) - topOffset + btnSize + outerPadding + borderSize);
                    } else {
                        return new Rectangle(leftPos - 31, topPos + 10, 31, 34);
                    }
                }

                @Override
                public void addExtraBoxes(List<Rectangle> rectangles, int leftPos, int topPos) {
                    rectangles.add(getBox(leftPos, topPos));
                }
            };
        }
    }
}
