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

package aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.base;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleVariantStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtCompound;

/**
 * A storage that can store a single item variant at any given time.
 * Implementors should at least override {@link #getCapacity(TransferVariant) getCapacity(ItemVariant)},
 * and probably {@link #onFinalCommit} as well for {@code markDirty()} and similar calls.
 *
 * <p>This is a convenient specialization of {@link SingleVariantStorage} for items that additionally offers methods
 * to read the contents of the storage from NBT.
 */
public abstract class SingleItemStorage extends SingleVariantStorage<ItemVariant> {
	@Override
	protected final ItemVariant getBlankVariant() {
		return ItemVariant.blank();
	}

	/**
	 * Simple implementation of reading from NBT, to match what is written by {@link #writeNbt}.
	 * Other formats are allowed, this is just a suggestion.
	 */
	public void readNbt(CompoundTag nbt) {
		variant = ItemVariant.fromNbt(nbt.getCompound("variant"));
		amount = nbt.getLong("amount");
	}
}
