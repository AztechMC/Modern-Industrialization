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

package aztech.modern_industrialization.machines.gui;

import aztech.modern_industrialization.inventory.SlotGroup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.Slot;

public final class GuiComponent {
    public interface MenuFacade {
        void addSlotToMenu(Slot slot, SlotGroup slotGroup);

        MachineGuiParameters getGuiParams();
    }

    public interface Common {
        default void setupMenu(MenuFacade menu) {
        }
    }

    /**
     * Server part of a synced component.
     *
     * @param <D> Synced data type.
     */
    public interface Server<D> extends Common {
        /**
         * @return A copy of the current sync data.
         */
        D copyData();

        /**
         * @return Whether the cached data is outdated, meaning that a sync must be
         *         performed.
         */
        boolean needsSync(D cachedData);

        /**
         * Write the initial data to the packet byte buf, used only when the screen is
         * opened.
         */
        void writeInitialData(RegistryFriendlyByteBuf buf);

        /**
         * Write the current data to the packet byte buf, used when syncing after the
         * screen was opened.
         */
        void writeCurrentData(RegistryFriendlyByteBuf buf);

        /**
         * Return the id of the component. Must match that of the {@code GuiComponentClient}
         * registered with {@code GuiComponentsClient#register}.
         */
        ResourceLocation getId();
    }

    /**
     * Convenience override when no data needs to be synced.
     */
    public interface ServerNoData extends Server<Unit> {
        @Override
        default Unit copyData() {
            return Unit.INSTANCE;
        }

        @Override
        default boolean needsSync(Unit cachedData) {
            return false;
        }

        @Override
        default void writeCurrentData(RegistryFriendlyByteBuf buf) {
        }
    }
}
