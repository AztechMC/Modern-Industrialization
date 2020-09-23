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
    int getConnectionType();
    void setConnectionType(int type);
    int getPriority();
    /**
     * Don't call this, always call {@link ItemPipeInterface#incrementPriority}.
     */
    void setPriority(int priority);
    default void incrementPriority(int delta) {
        if(delta == 1 || delta == -1 || delta == 10 || delta == -10) {
            int p = getPriority() + delta;
            if(p < -128) p = -128;
            if(p > 127) p = 127;
            setPriority(p);
        }
    }

    static ItemPipeInterface ofBuf(PacketByteBuf buf) {
        boolean[] whitelist = new boolean[] { buf.readBoolean() };
        int[] type = new int[] { buf.readInt() };
        int[] priority = new int[] { buf.readInt() };
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

            @Override
            public int getConnectionType() {
                return type[0];
            }

            @Override
            public void setConnectionType(int type_) {
                type[0] = type_;
            }

            @Override
            public int getPriority() {
                return priority[0];
            }

            @Override
            public void setPriority(int priority_) {
                priority[0] = priority_;
            }
        };
    }

    default void toBuf(PacketByteBuf buf) {
        buf.writeBoolean(isWhitelist());
        buf.writeInt(getConnectionType());
        buf.writeInt(getPriority());
        for(int i = 0; i < SLOTS; ++i) buf.writeItemStack(getStack(i));
    }
}
