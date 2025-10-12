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

import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.BackgroundRenderedSlot;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotPanelClient implements GuiComponentClient {
    private final List<SlotPanel.SlotType> slotTypes = new ArrayList<>();

    public SlotPanelClient(RegistryFriendlyByteBuf buf) {
        int slotCount = buf.readVarInt();
        for (int i = 0; i < slotCount; ++i) {
            slotTypes.add(buf.readEnum(SlotPanel.SlotType.class));
        }
    }

    @Override
    public void readCurrentData(RegistryFriendlyByteBuf buf) {
    }

    @Override
    public void setupMenu(GuiComponent.MenuFacade menu) {
        for (int i = 0; i < slotTypes.size(); ++i) {
            var type = slotTypes.get(i);

            class ClientSlot extends SlotWithBackground implements SlotTooltip {
                public ClientSlot(int i) {
                    super(new SimpleContainer(1), 0, SlotPanel.getSlotX(menu.getGuiParams()), SlotPanel.getSlotY(i));
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return type.mayPlace(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return type.slotLimit;
                }

                @Override
                public int getBackgroundU() {
                    return !hasItem() ? type.u : 0;
                }

                @Override
                public int getBackgroundV() {
                    return !hasItem() ? type.v : 0;
                }

                @Override
                public Component getTooltip() {
                    return MITooltips.line(type.tooltip).build();
                }
            }

            menu.addSlotToMenu(new ClientSlot(i), type.group);
        }
    }

    @Override
    public ClientComponentRenderer createRenderer(MachineScreen machineScreen) {
        return new ClientComponentRenderer() {
            private Rectangle getBox(int leftPos, int topPos) {
                return new Rectangle(leftPos + machineScreen.getGuiParams().backgroundWidth, topPos + 10, 31, 14 + 20 * slotTypes.size());
            }

            @Override
            public void addExtraBoxes(List<Rectangle> rectangles, int leftPos, int topPos) {
                rectangles.add(getBox(leftPos, topPos));
            }

            @Override
            public void renderBackground(GuiGraphics guiGraphics, int x, int y) {
                var box = getBox(x, y);

                int textureX = box.x() - x - box.w();
                guiGraphics.blit(MachineScreen.BACKGROUND, box.x(), box.y(), textureX, 0, box.w(), box.h() - 4);
                guiGraphics.blit(MachineScreen.BACKGROUND, box.x(), box.y() + box.h() - 4, textureX, 252, box.w(), 4);
            }

            @Override
            public void renderTooltip(MachineScreen screen, Font font, GuiGraphics guiGraphics, int x, int y, int cursorX, int cursorY) {
                if (screen.getFocusedSlot() instanceof SlotTooltip st && !screen.getFocusedSlot().hasItem()) {
                    guiGraphics.renderTooltip(font, st.getTooltip(), cursorX, cursorY);
                }
            }
        };
    }

    interface SlotTooltip {
        Component getTooltip();
    }

    public static class SlotWithBackground extends Slot implements BackgroundRenderedSlot {
        public SlotWithBackground(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }
    }
}
