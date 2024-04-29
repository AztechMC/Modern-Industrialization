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
package aztech.modern_industrialization.compat.viewer.impl.rei;

import aztech.modern_industrialization.client.screen.MIHandledScreen;
import aztech.modern_industrialization.compat.viewer.ReiDraggable;
import aztech.modern_industrialization.network.machines.DoSlotDraggingPacket;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.Simulation;
import dev.architectury.fluid.FluidStack;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.drag.DraggableStack;
import me.shedaniel.rei.api.client.gui.drag.DraggableStackVisitor;
import me.shedaniel.rei.api.client.gui.drag.DraggedAcceptorResult;
import me.shedaniel.rei.api.client.gui.drag.DraggingContext;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

class MIDraggableStackVisitor implements DraggableStackVisitor<Screen> {
    @Override
    public DraggedAcceptorResult acceptDraggedStack(DraggingContext<Screen> context, DraggableStack stack) {
        return acceptsStack(context, stack) ? DraggedAcceptorResult.ACCEPTED : DraggedAcceptorResult.PASS;
    }

    private boolean acceptsStack(DraggingContext<Screen> context, DraggableStack stack) {
        FluidVariant fk = stack.getStack().getValue() instanceof FluidStack fs ? FluidVariant.of(fs.getFluid(), fs.getTag()) : null;
        ItemVariant ik = stack.getStack().getValue() instanceof ItemStack is ? ItemVariant.of(is) : null;
        @Nullable
        GuiEventListener element = context.getScreen().getChildAt(context.getCurrentPosition().x, context.getCurrentPosition().y)
                .orElse(null);
        if (element instanceof ReiDraggable dw) {
            if (ik != null) {
                return dw.dragItem(ik, Simulation.ACT);
            }
            if (fk != null) {
                return dw.dragFluid(fk, Simulation.ACT);
            }
        }
        if (context.getScreen() instanceof MIHandledScreen<?>handledScreen) {
            AbstractContainerMenu handler = handledScreen.getMenu();
            Slot slot = handledScreen.getFocusedSlot();
            if (slot instanceof ReiDraggable dw) {
                int slotId = handler.slots.indexOf(slot);
                if (ik != null && dw.dragItem(ik, Simulation.ACT)) {
                    new DoSlotDraggingPacket(handler.containerId, slotId, ik).sendToServer();
                    return true;
                }
                if (fk != null && dw.dragFluid(fk, Simulation.ACT)) {
                    new DoSlotDraggingPacket(handler.containerId, slotId, fk).sendToServer();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Stream<BoundsProvider> getDraggableAcceptingBounds(DraggingContext<Screen> context, DraggableStack stack) {
        List<BoundsProvider> bounds = new ArrayList<>();
        FluidVariant fk = stack.getStack().getValue() instanceof FluidStack fs ? FluidVariant.of(fs.getFluid(), fs.getTag()) : null;
        ItemVariant ik = stack.getStack().getValue() instanceof ItemStack is ? ItemVariant.of(is) : null;
        for (GuiEventListener element : context.getScreen().children()) {
            if (element instanceof AbstractWidget cw && element instanceof ReiDraggable dw) {
                if (ik != null && dw.dragItem(ik, Simulation.SIMULATE)) {
                    bounds.add(getWidgetBounds(cw));
                }
                if (fk != null && dw.dragFluid(fk, Simulation.SIMULATE)) {
                    bounds.add(getWidgetBounds(cw));
                }
            }
        }
        if (context.getScreen() instanceof MIHandledScreen<?>handledScreen) {
            AbstractContainerMenu handler = handledScreen.getMenu();
            for (Slot slot : handler.slots) {
                if (slot instanceof ReiDraggable dw) {
                    if (ik != null && dw.dragItem(ik, Simulation.SIMULATE)) {
                        bounds.add(getSlotBounds(slot, handledScreen));
                    }
                    if (fk != null && dw.dragFluid(fk, Simulation.SIMULATE)) {
                        bounds.add(getSlotBounds(slot, handledScreen));
                    }
                }
            }
        }
        return bounds.stream();
    }

    @Override
    public <R extends Screen> boolean isHandingScreen(R screen) {
        return screen instanceof MIHandledScreen;
    }

    private static DraggableStackVisitor.BoundsProvider getWidgetBounds(AbstractWidget cw) {
        return DraggableStackVisitor.BoundsProvider.ofRectangle(new Rectangle(cw.getX(), cw.getY(), cw.getWidth(), cw.getHeight()));
    }

    private static DraggableStackVisitor.BoundsProvider getSlotBounds(Slot slot, MIHandledScreen<?> screen) {
        return DraggableStackVisitor.BoundsProvider.ofRectangle(new Rectangle(slot.x + screen.getX(), slot.y + screen.getY(), 16, 16));
    }
}
