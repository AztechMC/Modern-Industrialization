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
package aztech.modern_industrialization.transferapi.api.item;

import aztech.modern_industrialization.transferapi.impl.item.ItemKeyImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable count-less ItemStack, i.e. an immutable association of an item
 * and an optional NBT compound tag.
 *
 * <p>
 * Do not implement, use the static {@code of(...)} functions instead.
 */
@ApiStatus.NonExtendable
public interface ItemKey {
    /**
     * Retrieve an empty ItemKey.
     */
    static ItemKey empty() {
        return of(Items.AIR);
    }

    /**
     * Retrieve an ItemKey with the item and tag of a stack.
     */
    static ItemKey of(ItemStack stack) {
        return of(stack.getItem(), stack.getTag());
    }

    /**
     * Retrieve an ItemKey with an item and without a tag.
     */
    static ItemKey of(ItemConvertible item) {
        return of(item, null);
    }

    /**
     * Retrieve an ItemKey with an item and an optional tag.
     */
    static ItemKey of(ItemConvertible item, @Nullable NbtCompound tag) {
        return ItemKeyImpl.of(item.asItem(), tag);
    }

    /**
     * Return true if this key is empty, i.e. its item is Items.AIR, and false
     * otherwise.
     */
    boolean isEmpty();

    /**
     * Return true if the item and tag of this key match those of the passed stack,
     * and false otherwise.
     */
    boolean matches(ItemStack stack);

    /**
     * Return true if the tag of this key matches the passed tag, and false
     * otherwise.
     *
     * <p>
     * Note: True is returned if both tags are {@code null}.
     */
    boolean tagMatches(@Nullable NbtCompound other);

    /**
     * Return true if this key has a tag, false otherwise.
     */
    boolean hasTag();

    /**
     * Return the item of this key.
     */
    Item getItem();

    /**
     * Return a copy of the tag of this key, or {@code null} if this key doesn't
     * have a tag.
     *
     * <p>
     * Note: use {@link #tagMatches} if you only need to check for tag equality.
     */
    @Nullable
    NbtCompound copyTag();

    /**
     * Create a new item stack with count 1 from this key.
     */
    ItemStack toStack();

    /**
     * Create a new item stack from this key.
     *
     * @param count The count of the returned stack. It may lead to counts higher
     *              than maximum stack size.
     */
    ItemStack toStack(int count);

    /**
     * Save this key into an NBT compound tag. {@link #fromNbt} can be used to
     * retrieve the key later.
     *
     * <p>
     * Note: This key is safe to use for persisting data as items are saved using
     * their full Identifier.
     */
    NbtCompound toNbt();

    /**
     * Deserialize a key from an NBT compound tag, assuming it was serialized using
     * {@link #toNbt}. If an error occurs during deserialization, it will be logged
     * with the DEBUG level, and an empty key will be returned.
     */
    static ItemKey fromNbt(NbtCompound nbt) {
        return ItemKeyImpl.fromNbt(nbt);
    }

    /**
     * Save this key into a packet byte buffer. {@link #fromPacket} can be used to
     * retrieve the key later.
     *
     * <p>
     * Note: Items are saved using their raw registry integer id.
     */
    void toPacket(PacketByteBuf buf);

    /**
     * Write a key from a packet byte buffer, assuming it was serialized using
     * {@link #toPacket}.
     */
    static ItemKey fromPacket(PacketByteBuf buf) {
        return ItemKeyImpl.fromPacket(buf);
    }
}
