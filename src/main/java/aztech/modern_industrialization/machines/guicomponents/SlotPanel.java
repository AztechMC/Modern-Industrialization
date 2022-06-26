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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.inventory.BackgroundRenderedSlot;
import aztech.modern_industrialization.inventory.HackySlot;
import aztech.modern_industrialization.inventory.SlotGroup;
import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CasingComponent;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.util.Rectangle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class SlotPanel {
    private static int getSlotX(MachineGuiParameters guiParameters) {
        return guiParameters.backgroundWidth + 6;
    }

    private static int getSlotY(int slotIndex) {
        return 19 + slotIndex * 20;
    }

    public static class Server implements GuiComponent.Server<Unit> {
        private final MachineBlockEntity machine;
        private final List<Consumer<GuiComponent.MenuFacade>> slotFactories = new ArrayList<>();
        private final List<SlotType> slotTypes = new ArrayList<>();

        public Server(MachineBlockEntity machine) {
            this.machine = machine;
        }

        public Server withUpgrades(UpgradeComponent upgradeComponent) {
            return addSlot(SlotType.UPGRADES, () -> upgradeComponent.getDrop().copy(), upgradeComponent::setStackServer);
        }

        public Server withCasing(CasingComponent casingComponent) {
            return addSlot(SlotType.CASINGS, () -> casingComponent.getDrop().copy(), casingComponent::setCasingServer);
        }

        private Server addSlot(SlotType type, Supplier<ItemStack> getStack, BiConsumer<MachineBlockEntity, ItemStack> setStack) {
            int slotIndex = slotTypes.size();
            slotFactories.add(facade -> facade.addSlotToMenu(new HackySlot(getSlotX(machine.guiParams), getSlotY(slotIndex)) {
                @Override
                protected ItemStack getStack() {
                    return getStack.get();
                }

                @Override
                protected void setStack(ItemStack stack) {
                    setStack.accept(machine, stack);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return type.mayPlace(stack);
                }

                @Override
                public int getMaxStackSize() {
                    return type.slotLimit;
                }
            }, type.group));
            slotTypes.add(type);
            return this;
        }

        @Override
        public Unit copyData() {
            return Unit.INSTANCE;
        }

        @Override
        public boolean needsSync(Unit cachedData) {
            return false;
        }

        @Override
        public void writeInitialData(FriendlyByteBuf buf) {
            buf.writeVarInt(slotFactories.size());
            for (var type : slotTypes) {
                buf.writeEnum(type);
            }
        }

        @Override
        public void writeCurrentData(FriendlyByteBuf buf) {
        }

        @Override
        public ResourceLocation getId() {
            return GuiComponents.SLOT_PANEL;
        }

        @Override
        public void setupMenu(GuiComponent.MenuFacade menu) {
            for (var factory : slotFactories) {
                factory.accept(menu);
            }
        }
    }

    public static class Client implements GuiComponent.Client {
        private final List<SlotType> slotTypes = new ArrayList<>();
        private MachineGuiParameters guiParams;

        public Client(FriendlyByteBuf buf) {
            int slotCount = buf.readVarInt();
            for (int i = 0; i < slotCount; ++i) {
                slotTypes.add(buf.readEnum(SlotType.class));
            }
        }

        @Override
        public void readCurrentData(FriendlyByteBuf buf) {
        }

        @Override
        public void setupMenu(GuiComponent.MenuFacade menu) {
            guiParams = menu.getGuiParams();

            for (int i = 0; i < slotTypes.size(); ++i) {
                var type = slotTypes.get(i);

                class ClientSlot extends SlotWithBackground implements SlotTooltip {
                    public ClientSlot(int i) {
                        super(new SimpleContainer(1), 0, getSlotX(menu.getGuiParams()), getSlotY(i));
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
        public ClientComponentRenderer createRenderer() {
            return new ClientComponentRenderer() {
                private Rectangle getBox(int leftPos, int topPos) {
                    return new Rectangle(leftPos + guiParams.backgroundWidth, topPos + 10, 31, 14 + 20 * slotTypes.size());
                }

                @Override
                public void addExtraBoxes(List<Rectangle> rectangles, int leftPos, int topPos) {
                    rectangles.add(getBox(leftPos, topPos));
                }

                @Override
                public void renderBackground(net.minecraft.client.gui.GuiComponent helper, PoseStack matrices, int x, int y) {
                    RenderSystem.setShaderTexture(0, MachineScreen.BACKGROUND);
                    var box = getBox(x, y);

                    int textureX = box.x() - x - box.w();
                    helper.blit(matrices, box.x(), box.y(), textureX, 0, box.w(), box.h() - 4);
                    helper.blit(matrices, box.x(), box.y() + box.h() - 4, textureX, 252, box.w(), 4);
                }

                @Override
                public void renderTooltip(MachineScreen screen, PoseStack matrices, int x, int y, int cursorX, int cursorY) {
                    if (screen.getFocusedSlot() instanceof SlotTooltip st && !screen.getFocusedSlot().hasItem()) {
                        screen.renderTooltip(matrices, st.getTooltip(), cursorX, cursorY);
                    }
                }
            };
        }

        interface SlotTooltip {
            Component getTooltip();
        }
    }

    private enum SlotType {
        UPGRADES(SlotGroup.UPGRADES, 64, stack -> UpgradeComponent.UPGRADES.containsKey(stack.getItem()), 0, 80,
                MIText.AcceptsUpgrades),
        // Assumes that the default casing is always the LV casing for now
        CASINGS(SlotGroup.CASING, 1, stack -> {
            if (stack.getItem() instanceof BlockItem block && block != MIBlock.BASIC_MACHINE_HULL.asItem()) {
                return CasingComponent.blockCasing.containsKey(block.getBlock());
            }
            return false;
        }, 18, 80, MIText.AcceptsCasings),
        ;

        private final SlotGroup group;
        public final int slotLimit;
        private final Predicate<ItemStack> insertionChecker;
        private final int u, v;
        private final MIText tooltip;

        SlotType(SlotGroup group, int slotLimit, Predicate<ItemStack> insertionChecker, int u, int v, MIText tooltip) {
            this.group = group;
            this.slotLimit = slotLimit;
            this.insertionChecker = insertionChecker;
            this.u = u;
            this.v = v;
            this.tooltip = tooltip;
        }

        public boolean mayPlace(ItemStack stack) {
            return insertionChecker.test(stack);
        }
    }

    private static class SlotWithBackground extends Slot implements BackgroundRenderedSlot {
        public SlotWithBackground(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }
    }
}
