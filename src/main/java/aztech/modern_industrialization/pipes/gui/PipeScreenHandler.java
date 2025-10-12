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

package aztech.modern_industrialization.pipes.gui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public abstract class PipeScreenHandler extends AbstractContainerMenu {
    protected PipeScreenHandler(MenuType<?> type, int syncId) {
        super(type, syncId);
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int height) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, i * 9 + j + 9, 8 + j * 18, PipeGuiHelper.getPlayerInvStart(height) + i * 18));
            }
        }
        for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j, 8 + j * 18, PipeGuiHelper.getPlayerInvStart(height) + 58));
        }
    }

    /**
     * Return the casted interface if possible, and crash otherwise.
     */
    @SuppressWarnings("unchecked")
    public final <T> T getInterface(Class<T> klass) {
        Object iface = getInterface();
        if (klass.isInstance(iface)) {
            return (T) iface;
        } else {
            throw new RuntimeException(
                    "Cannot convert pipe interface of type " + iface.getClass().getCanonicalName() + " to type " + klass.getCanonicalName());
        }
    }

    /**
     * @return The interface containing the real logic.
     */
    protected abstract Object getInterface();
}
