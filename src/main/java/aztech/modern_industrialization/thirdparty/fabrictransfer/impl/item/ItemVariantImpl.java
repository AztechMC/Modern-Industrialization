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
package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.item;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemVariantImpl implements ItemVariant {
    private static final Map<Item, ItemVariant> noTagCache = new ConcurrentHashMap<>();

    public static ItemVariant of(Item item, @Nullable CompoundTag tag, @Nullable CompoundTag attachmentsTag) {
        Objects.requireNonNull(item, "Item may not be null.");

        // Only tag-less or empty item variants are cached for now.
        if ((tag == null && attachmentsTag == null) || item == Items.AIR) {
            return noTagCache.computeIfAbsent(item, i -> new ItemVariantImpl(i, null, null));
        } else {
            return new ItemVariantImpl(item, tag, attachmentsTag);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/item");

    private final Item item;
    private final @Nullable CompoundTag nbt;
    private final @Nullable CompoundTag attachments;
    private final int hashCode;

    private ItemVariantImpl(Item item, CompoundTag nbt, CompoundTag attachmentsNbt) {
        this.item = item;
        this.nbt = nbt == null ? null : nbt.copy(); // defensive copy
        this.attachments = attachmentsNbt == null ? null : attachmentsNbt.copy(); // defensive copy
        hashCode = Objects.hash(item, nbt, attachmentsNbt);
    }

    @Override
    public Item getObject() {
        return item;
    }

    @Nullable
    @Override
    public CompoundTag getNbt() {
        return nbt;
    }

    @Nullable
    @Override
    public CompoundTag getAttachments() {
        return attachments;
    }

    @Override
    public boolean isBlank() {
        return item == Items.AIR;
    }

    @Override
    public CompoundTag toNbt() {
        CompoundTag result = new CompoundTag();
        result.putString("item", BuiltInRegistries.ITEM.getKey(item).toString());

        if (nbt != null) {
            result.put("tag", nbt.copy());
        }

        if (attachments != null) {
            result.put("attachments", attachments.copy());
        }

        return result;
    }

    public static ItemVariant fromNbt(CompoundTag tag) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("item")));
            CompoundTag aTag = tag.contains("tag") ? tag.getCompound("tag") : null;
            CompoundTag attachmentsTag = tag.contains("attachments") ? tag.getCompound("attachments") : null;
            return of(item, aTag, attachmentsTag);
        } catch (RuntimeException runtimeException) {
            LOGGER.debug("Tried to load an invalid ItemVariant from NBT: {}", tag, runtimeException);
            return ItemVariant.blank();
        }
    }

    @Override
    public void toPacket(FriendlyByteBuf buf) {
        if (isBlank()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(Item.getId(item));
            buf.writeNbt(nbt);
            buf.writeNbt(attachments);
        }
    }

    public static ItemVariant fromPacket(FriendlyByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemVariant.blank();
        } else {
            Item item = Item.byId(buf.readVarInt());
            CompoundTag nbt = buf.readNbt();
            CompoundTag attachments = buf.readNbt();
            return of(item, nbt, attachments);
        }
    }

    @Override
    public String toString() {
        return "ItemVariant{item=" + item + ", tag=" + nbt + ", attachments=" + attachments + '}';
    }

    @Override
    public boolean equals(Object o) {
        // succeed fast with == check
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemVariantImpl ItemVariant = (ItemVariantImpl) o;
        // fail fast with hash code
        return hashCode == ItemVariant.hashCode && item == ItemVariant.item && nbtMatches(ItemVariant.nbt)
                && Objects.equals(attachments, ItemVariant.attachments);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
