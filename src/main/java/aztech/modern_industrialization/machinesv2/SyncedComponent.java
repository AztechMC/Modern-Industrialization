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
package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.machinesv2.gui.ClientComponentRenderer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class SyncedComponent {
    /**
     * Client part of a synced component.
     */
    public interface Client {
        /**
         * Read the current data, written by {@link Server#writeCurrentData}.
         */
        void read(PacketByteBuf buf);

        /**
         * @return A new renderer linked to this client-side component.
         */
        ClientComponentRenderer createRenderer();
    }

    @FunctionalInterface
    public interface ClientFactory {
        /**
         * Create a new client synced component from the initial data, written by
         * {@link Server#writeInitialData}.
         */
        Client createFromInitialData(PacketByteBuf buf);
    }

    /**
     * Server part of a synced component.
     * 
     * @param <D> Synced data type.
     */
    public interface Server<D> {
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
        void writeInitialData(PacketByteBuf buf);

        /**
         * Write the current data to the packet byte buf, used when syncing after the
         * screen was opened.
         */
        void writeCurrentData(PacketByteBuf buf);

        /**
         * Return the id of the component. Must match that of the {@link Client}
         * registered with {@link SyncedComponents.Client#register}.
         */
        Identifier getId();
    }
}
