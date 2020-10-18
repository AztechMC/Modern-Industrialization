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
package aztech.modern_industrialization.pipes.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;

/**
 * Player interface to an item pipe, this is used for interacting with the
 * player via the screen handler and the screen.
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
        if (delta == 1 || delta == -1 || delta == 10 || delta == -10) {
            int p = getPriority() + delta;
            if (p < -128)
                p = -128;
            if (p > 127)
                p = 127;
            setPriority(p);
        }
    }

    static ItemPipeInterface ofBuf(PacketByteBuf buf) {
        boolean[] whitelist = new boolean[] { buf.readBoolean() };
        int[] type = new int[] { buf.readInt() };
        int[] priority = new int[] { buf.readInt() };
        List<ItemStack> stacks = new ArrayList<>(SLOTS);
        for (int i = 0; i < SLOTS; ++i)
            stacks.add(buf.readItemStack());

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
        for (int i = 0; i < SLOTS; ++i)
            buf.writeItemStack(getStack(i));
    }
}
