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

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import dev.architectury.event.CompoundEventResult;
import me.shedaniel.math.Point;
import me.shedaniel.rei.api.client.registry.screen.FocusedStackProvider;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.material.Fluid;

class MachineFocusedStackProvider implements FocusedStackProvider {
    @Override
    public CompoundEventResult<EntryStack<?>> provide(Screen screen, Point mouse) {
        if (screen instanceof MachineScreen) {
            Slot slot = ((MachineScreen) screen).getFocusedSlot();
            if (slot instanceof ConfigurableFluidStack.ConfigurableFluidSlot) {
                ConfigurableFluidStack stack = ((ConfigurableFluidStack.ConfigurableFluidSlot) slot).getConfStack();
                if (stack.getAmount() > 0) {
                    Fluid fluid = stack.getResource().getFluid();
                    if (fluid != null) {
                        return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                    }
                } else if (stack.getLockedInstance() != null) {
                    Fluid fluid = stack.getLockedInstance();
                    if (fluid != null) {
                        return CompoundEventResult.interruptTrue(EntryStacks.of(fluid));
                    }
                }
            } else if (slot instanceof ConfigurableItemStack.ConfigurableItemSlot) {
                ConfigurableItemStack stack = ((ConfigurableItemStack.ConfigurableItemSlot) slot).getConfStack();
                // the normal stack is already handled by REI, we just need to handle the locked
                // item!
                if (stack.getLockedInstance() != null) {
                    return CompoundEventResult.interruptTrue(EntryStacks.of(stack.getLockedInstance()));
                }
            }
        }
        return CompoundEventResult.pass();
    }
}
