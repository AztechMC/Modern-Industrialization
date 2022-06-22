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
import aztech.modern_industrialization.inventory.BackgroundRenderedSlot;
import aztech.modern_industrialization.inventory.HackySlot;
import aztech.modern_industrialization.machines.GuiComponents;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CasingComponent;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.gui.MachineGuiParameters;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class SlotPanel {
    private static int getSlotX(MachineGuiParameters guiParameters) {
        return guiParameters.backgroundWidth + 5;
    }

    private static int getSlotY(int slotIndex) {
        return 20 + slotIndex * 20;
    }

    public static class Server implements GuiComponent.Server<Unit> {
        private final MachineBlockEntity machine;
        private final List<Supplier<Slot>> slotFactories = new ArrayList<>();
        private final List<SlotType> slotTypes = new ArrayList<>();

        public Server(MachineBlockEntity machine) {
            this.machine = machine;
        }

        public Server withUpgrades(UpgradeComponent upgradeComponent) {
            int slotIndex = slotTypes.size();
            slotFactories.add(() -> new HackySlot(getSlotX(machine.guiParams), getSlotY(slotIndex)) {
                @Override
                protected ItemStack getStack() {
                    return upgradeComponent.getDrop().copy();
                }

                @Override
                protected void setStack(ItemStack stack) {
                    upgradeComponent.setStackServer(machine, stack);
                }

                @Override
                public boolean mayPlace(ItemStack stack) {
                    return SlotType.UPGRADES.insertionChecker.test(stack);
                }
            });
            slotTypes.add(SlotType.UPGRADES);
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
                menu.addSlotToMenu(factory.get());
            }
        }
    }

    public static class Client implements GuiComponent.Client {
        private final List<SlotType> slotTypes = new ArrayList<>();

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
            for (int i = 0; i < slotTypes.size(); ++i) {
                var type = slotTypes.get(i);
                menu.addSlotToMenu(new SlotWithBackground(new SimpleContainer(1), 0, getSlotX(menu.getGuiParams()), getSlotY(i)) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return type.insertionChecker.test(stack);
                    }

                    @Override
                    public int getMaxStackSize() {
                        return type.slotLimit;
                    }
                });
            }
        }

        @Override
        public ClientComponentRenderer createRenderer() {
            return new ClientComponentRenderer() {
                @Override
                public void renderBackground(net.minecraft.client.gui.GuiComponent helper, PoseStack matrices, int x, int y) {

                }
            };
        }
    }

    private enum SlotType {
        UPGRADES(64, stack -> UpgradeComponent.UPGRADES.containsKey(stack.getItem())),
        // Assumes that the default casing is always the LV casing for now
        CASINGS(1, stack -> {
            if (stack.getItem() instanceof BlockItem block && block != MIBlock.BASIC_MACHINE_HULL.asItem()) {
                return CasingComponent.blockCasing.containsKey(block.getBlock());
            }
            return false;
        }),
        ;

        public final int slotLimit;
        public final Predicate<ItemStack> insertionChecker;

        SlotType(int slotLimit, Predicate<ItemStack> insertionChecker) {
            this.slotLimit = slotLimit;
            this.insertionChecker = insertionChecker;
        }
    }

    private static class SlotWithBackground extends Slot implements BackgroundRenderedSlot {
        public SlotWithBackground(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }
    }
}
