/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.item;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

// TODO: data attachment support
public class ItemVariantImpl implements ItemVariant {
	private static final Map<Item, ItemVariant> noTagCache = new ConcurrentHashMap<>();

	public static ItemVariant of(Item item, @Nullable CompoundTag tag) {
		Objects.requireNonNull(item, "Item may not be null.");

		// Only tag-less or empty item variants are cached for now.
		if (tag == null || item == Items.AIR) {
			return noTagCache.computeIfAbsent(item, i -> new ItemVariantImpl(i, null));
		} else {
			return new ItemVariantImpl(item, tag);
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-transfer-api-v1/item");

	private final Item item;
	private final @Nullable CompoundTag nbt;
	private final int hashCode;
	/**
	 * Lazily computed, equivalent to calling toStack(1). <b>MAKE SURE IT IS NEVER MODIFIED!</b>
	 */
	private volatile @Nullable ItemStack cachedStack = null;

	public ItemVariantImpl(Item item, CompoundTag nbt) {
		this.item = item;
		this.nbt = nbt == null ? null : nbt.copy(); // defensive copy
		hashCode = Objects.hash(item, nbt);
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

		return result;
	}

	public static ItemVariant fromNbt(CompoundTag tag) {
		try {
			Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(tag.getString("item")));
			CompoundTag aTag = tag.contains("tag") ? tag.getCompound("tag") : null;
			return of(item, aTag);
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
		}
	}

	public static ItemVariant fromPacket(FriendlyByteBuf buf) {
		if (!buf.readBoolean()) {
			return ItemVariant.blank();
		} else {
			Item item = Item.byId(buf.readVarInt());
			CompoundTag nbt = buf.readNbt();
			return of(item, nbt);
		}
	}

	@Override
	public String toString() {
		return "ItemVariant{item=" + item + ", tag=" + nbt + '}';
	}

	@Override
	public boolean equals(Object o) {
		// succeed fast with == check
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ItemVariantImpl ItemVariant = (ItemVariantImpl) o;
		// fail fast with hash code
		return hashCode == ItemVariant.hashCode && item == ItemVariant.item && nbtMatches(ItemVariant.nbt);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	public ItemStack getCachedStack() {
		ItemStack ret = cachedStack;

		if (ret == null) {
			// multiple stacks could be created at the same time by different threads, but that is not an issue
			cachedStack = ret = toStack();
		}

		return ret;
	}
}
