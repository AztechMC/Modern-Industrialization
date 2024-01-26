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
package aztech.modern_industrialization.thirdparty.fabrictransfer.api.item;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.impl.item.ItemVariantImpl;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable count-less ItemStack, i.e. an immutable association of an item and an optional NBT compound tag.
 *
 * <p>
 * Do not implement, use the static {@code of(...)} functions instead.
 */
@ApiStatus.NonExtendable
public interface ItemVariant extends TransferVariant<Item> {
    /**
     * Retrieve a blank ItemVariant.
     */
    static ItemVariant blank() {
        return of(Items.AIR);
    }

    /**
     * Retrieve an ItemVariant with the item and tag of a stack.
     */
    static ItemVariant of(ItemStack stack) {
        return of(stack.getItem(), stack.getTag(), stack.serializeAttachments());
    }

    /**
     * Retrieve an ItemVariant with an item and without a tag.
     */
    static ItemVariant of(ItemLike item) {
        return of(item, null, null);
    }

    /**
     * Retrieve an ItemVariant with an item and an optional tag.
     */
    static ItemVariant of(ItemLike item, @Nullable CompoundTag tag, @Nullable CompoundTag attachmentsTags) {
        return ItemVariantImpl.of(item.asItem(), tag, attachmentsTags);
    }

    /**
     * Return true if the item and tag of this variant match those of the passed stack, and false otherwise.
     */
    default boolean matches(ItemStack stack) {
        return isOf(stack.getItem()) && nbtMatches(stack.getTag()) && Objects.equals(getAttachments(), stack.serializeAttachments());
    }

    /**
     * Return the item of this variant.
     */
    default Item getItem() {
        return getObject();
    }

    @Nullable
    CompoundTag getAttachments();

    /**
     * Create a new item stack with count 1 from this variant.
     */
    default ItemStack toStack() {
        return toStack(1);
    }

    /**
     * Create a new item stack from this variant.
     *
     * @param count The count of the returned stack. It may lead to counts higher than maximum stack size.
     */
    default ItemStack toStack(int count) {
        if (isBlank())
            return ItemStack.EMPTY;
        ItemStack stack = new ItemStack(getItem(), count, getAttachments());
        stack.setTag(copyNbt());
        return stack;
    }

    /**
     * Deserialize a variant from an NBT compound tag, assuming it was serialized using
     * {@link #toNbt}. If an error occurs during deserialization, it will be logged
     * with the DEBUG level, and a blank variant will be returned.
     */
    static ItemVariant fromNbt(CompoundTag nbt) {
        return ItemVariantImpl.fromNbt(nbt);
    }

    /**
     * Write a variant from a packet byte buffer, assuming it was serialized using
     * {@link #toPacket}.
     */
    static ItemVariant fromPacket(FriendlyByteBuf buf) {
        return ItemVariantImpl.fromPacket(buf);
    }
}
