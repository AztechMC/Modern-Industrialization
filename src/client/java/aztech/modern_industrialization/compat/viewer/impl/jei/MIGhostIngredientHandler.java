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
package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.client.screen.MIHandledScreen;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.network.machines.DoSlotDraggingPacket;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

class MIGhostIngredientHandler implements IGhostIngredientHandler<MIHandledScreen<?>> {
    @Override
    public <I> List<Target<I>> getTargetsTyped(MIHandledScreen<?> gui, ITypedIngredient<I> typedIngredient, boolean doStart) {
        var ingredient = typedIngredient.getIngredient();
        List<Target<I>> bounds = new ArrayList<>();

        FluidVariant fk = ingredient instanceof FluidStack fs ? FluidVariant.of(fs) : null;
        ItemVariant ik = ingredient instanceof ItemStack is ? ItemVariant.of(is) : null;
        for (GuiEventListener element : gui.children()) {
            if (element instanceof AbstractWidget cw && element instanceof ReiDraggable dw) {
                if (ik != null && dw.dragItem(ik, Simulation.SIMULATE)) {
                    bounds.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            return getWidgetBounds(cw);
                        }

                        @Override
                        public void accept(I ingredient) {
                            dw.dragItem(ik, Simulation.ACT);
                        }
                    });
                }
                if (fk != null && dw.dragFluid(fk, Simulation.SIMULATE)) {
                    bounds.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            return getWidgetBounds(cw);
                        }

                        @Override
                        public void accept(I ingredient) {
                            dw.dragFluid(fk, Simulation.ACT);
                        }
                    });
                }
            }
        }

        AbstractContainerMenu handler = gui.getMenu();

        for (Slot slot : handler.slots) {
            if (slot instanceof ReiDraggable dw) {
                if (ik != null && dw.dragItem(ik, Simulation.SIMULATE)) {
                    bounds.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            return getSlotTarget(slot, gui);
                        }

                        @Override
                        public void accept(I ingredient) {
                            if (dw.dragItem(ik, Simulation.ACT)) {
                                int slotId = handler.slots.indexOf(slot);
                                new DoSlotDraggingPacket(handler.containerId, slotId, ik).sendToServer();
                            }
                        }
                    });
                }
                if (fk != null && dw.dragFluid(fk, Simulation.SIMULATE)) {
                    bounds.add(new Target<>() {
                        @Override
                        public Rect2i getArea() {
                            return getSlotTarget(slot, gui);
                        }

                        @Override
                        public void accept(I ingredient) {
                            if (dw.dragFluid(fk, Simulation.ACT)) {
                                int slotId = handler.slots.indexOf(slot);
                                new DoSlotDraggingPacket(handler.containerId, slotId, fk).sendToServer();
                            }
                        }
                    });
                }
            }
        }

        return bounds;
    }

    @Override
    public void onComplete() {
    }

    private static Rect2i getWidgetBounds(AbstractWidget cw) {
        return new Rect2i(cw.getX(), cw.getY(), cw.getWidth(), cw.getHeight());
    }

    private static Rect2i getSlotTarget(Slot slot, MIHandledScreen<?> screen) {
        return new Rect2i(slot.x + screen.getX(), slot.y + screen.getY(), 16, 16);
    }
}
