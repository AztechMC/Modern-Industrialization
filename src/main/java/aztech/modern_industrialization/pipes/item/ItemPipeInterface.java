package aztech.modern_industrialization.pipes.item;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Player interface to an item pipe, this is used for interacting with the player via the screen handler and the screen.
 */
public interface ItemPipeInterface {
    int SLOTS = 21;

    boolean isWhitelist();
    void setWhitelist(boolean whitelist);
    ItemStack getStack(int slot);
    void setStack(int slot, ItemStack stack);

    static ItemPipeInterface ofBuf(PacketByteBuf buf) {
        boolean[] whitelist = new boolean[] { buf.readBoolean() };
        List<ItemStack> stacks = new ArrayList<>(SLOTS);
        for(int i = 0; i < SLOTS; ++i) stacks.add(buf.readItemStack());

        return new ItemPipeInterface() {
            @Override
            public boolean isWhitelist() {
                return whitelist[0];
            }

            @Override
            public void setWhitelist(boolean newWhitelist) {
                whitelist[0] = newWhitelist;
            }

            @Override
            public ItemStack getStack(int slot) {
                return stacks.get(slot);
            }

            @Override
            public void setStack(int slot, ItemStack stack) {
                stacks.set(slot, stack);
            }
        };
    }

    default void toBuf(PacketByteBuf buf) {
        buf.writeBoolean(isWhitelist());
        for(int i = 0; i < SLOTS; ++i) buf.writeItemStack(getStack(i));
    }
}
