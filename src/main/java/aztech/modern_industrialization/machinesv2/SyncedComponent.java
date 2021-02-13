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
         * Create a new client synced component from the initial data, written by {@link Server#writeInitialData}.
         */
        Client createFromInitialData(PacketByteBuf buf);
    }

    /**
     * Server part of a synced component.
     * @param <D> Synced data type.
     */
    public interface Server<D> {
        /**
         * @return A copy of the current sync data.
         */
        D copyData();

        /**
         * @return Whether the cached data is outdated, meaning that a sync must be performed.
         */
        boolean needsSync(D cachedData);

        /**
         * Write the initial data to the packet byte buf, used only when the screen is opened.
         */
        void writeInitialData(PacketByteBuf buf);

        /**
         * Write the current data to the packet byte buf, used when syncing after the screen was opened.
         */
        void writeCurrentData(PacketByteBuf buf);

        /**
         * Return the id of the component.
         * Must match that of the {@link Client} registered with {@link SyncedComponents.Client#register}.
         */
        Identifier getId();
    }
}
