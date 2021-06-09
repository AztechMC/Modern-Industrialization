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
package aztech.modern_industrialization.transferapi.impl.item;

import aztech.modern_industrialization.transferapi.api.item.ItemKey;
import java.util.Objects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ItemKeyImpl implements ItemKey {
    public static ItemKey of(Item item, @Nullable NbtCompound tag) {
        Objects.requireNonNull(item, "Item may not be null.");

        // Only tag-less or empty item keys are cached for now.
        if (tag == null || item == Items.AIR) {
            return ((ItemItemKeyCache) item).fabric_getOrCreateItemKey();
        } else {
            return new ItemKeyImpl(item, tag);
        }
    }

    private static final Logger LOGGER = LogManager.getLogger("fabric-api-lookup-api-v1/item");

    private final Item item;
    private final @Nullable NbtCompound tag;
    private final int hashCode;

    public ItemKeyImpl(Item item, NbtCompound tag) {
        this.item = item;
        this.tag = tag == null ? null : tag.copy(); // defensive copy
        hashCode = Objects.hash(item, tag);
    }

    @Override
    public boolean isEmpty() {
        return item == Items.AIR;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return stack.getItem() == item && tagMatches(stack.getTag());
    }

    @Override
    public boolean tagMatches(@Nullable NbtCompound other) {
        return Objects.equals(tag, other);
    }

    @Override
    public boolean hasTag() {
        return tag != null;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public @Nullable NbtCompound copyTag() {
        return tag == null ? null : tag.copy();
    }

    @Override
    public ItemStack toStack() {
        return toStack(1);
    }

    @Override
    public ItemStack toStack(int count) {
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(item, count);
        stack.setTag(copyTag());
        return stack;
    }

    @Override
    public NbtCompound toNbt() {
        NbtCompound result = new NbtCompound();
        result.putString("item", Registry.ITEM.getId(item).toString());

        if (tag != null) {
            result.put("tag", tag.copy());
        }

        return result;
    }

    public static ItemKey fromNbt(NbtCompound tag) {
        try {
            Item item = Registry.ITEM.get(new Identifier(tag.getString("item")));
            NbtCompound aTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            return of(item, aTag);
        } catch (RuntimeException runtimeException) {
            LOGGER.debug("Tried to load an invalid ItemKey from NBT: {}", tag, runtimeException);
            return ItemKey.empty();
        }
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        if (isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(Item.getRawId(item));
            buf.writeNbt(tag);
        }
    }

    public static ItemKey fromPacket(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemKey.empty();
        } else {
            Item item = Item.byRawId(buf.readVarInt());
            NbtCompound tag = buf.readNbt();
            return of(item, tag);
        }
    }

    @Override
    public String toString() {
        return "ItemKeyImpl{item=" + item + ", tag=" + tag + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemKeyImpl itemKey = (ItemKeyImpl) o;
        // fail fast with hash code
        return hashCode == itemKey.hashCode && item == itemKey.item && tagMatches(itemKey.tag);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
