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
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.network.machines.SetAutoExtractPacket;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class AutoExtractClient extends GuiComponentClient<AutoExtract.Params, AutoExtract.Data> {
    public AutoExtractClient(AutoExtract.Params params, AutoExtract.Data data) {
        super(params, data);
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new Renderer();
    }

    private class Renderer implements ClientComponentRenderer {
        @Override
        public void addButtons(ButtonContainer container) {
            if (params.hasExtractFluids()) {
                addExtractButton(container, false);
            }
            if (params.hasExtractItems()) {
                addExtractButton(container, true);
            }
        }

        private void addExtractButton(ButtonContainer container, boolean isItem) {
            int u = isItem ? 20 : 0;
            boolean displayAsInsert = params.displayAsInsert();
            container.addButton(u, syncId -> {
                boolean newExtract;
                if (isItem) {
                    newExtract = !data.extractItems();
                    data = new AutoExtract.Data(newExtract, data.extractFluids());
                } else {
                    newExtract = !data.extractFluids();
                    data = new AutoExtract.Data(data.extractItems(), newExtract);
                }
                new SetAutoExtractPacket(syncId, isItem, newExtract).sendToServer();
            }, () -> {
                List<Component> lines = new ArrayList<>();
                if (isItem ? data.extractItems() : data.extractFluids()) {
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
            }, () -> isItem ? data.extractItems() : data.extractFluids());
        }

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int x, int y) {}
    }
}
