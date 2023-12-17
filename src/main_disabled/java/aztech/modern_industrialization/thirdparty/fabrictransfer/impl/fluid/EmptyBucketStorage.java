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

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.fluid;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.context.ContainerItemContext;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidConstants;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StoragePreconditions;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.StorageView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.BlankVariantView;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.InsertionOnlyStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Iterator;
import java.util.List;

/**
 * Storage implementation for empty buckets, accepting any fluid with a bidirectional fluid &lt;-&gt; bucket mapping.
 */
public class EmptyBucketStorage implements InsertionOnlyStorage<FluidVariant> {
	private final ContainerItemContext context;
	private final List<StorageView<FluidVariant>> blankView = List.of(new BlankVariantView<>(FluidVariant.blank(), FluidConstants.BUCKET));

	public EmptyBucketStorage(ContainerItemContext context) {
		this.context = context;
	}

	@Override
	public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		if (!context.getItemVariant().isOf(Items.BUCKET)) return 0;

		Item fullBucket = resource.getFluid().getBucketItem();

		// Make sure the resource is a correct fluid mapping: the fluid <-> bucket mapping must be bidirectional.
		if (fullBucket instanceof BucketItemAccessor accessor && resource.isOf(accessor.fabric_getFluid())) {
			if (maxAmount >= FluidConstants.BUCKET) {
				ItemVariant newVariant = ItemVariant.of(fullBucket, context.getItemVariant().getNbt());

				if (context.exchange(newVariant, 1, transaction) == 1) {
					return FluidConstants.BUCKET;
				}
			}
		}

		return 0;
	}

	@Override
	public Iterator<StorageView<FluidVariant>> iterator() {
		return blankView.iterator();
	}

	@Override
	public String toString() {
		return "EmptyBucketStorage[" + context + "]";
	}
}
