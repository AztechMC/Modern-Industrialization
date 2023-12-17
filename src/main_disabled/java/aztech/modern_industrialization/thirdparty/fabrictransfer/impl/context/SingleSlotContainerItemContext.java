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

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl.context;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.context.ContainerItemContext;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SingleSlotContainerItemContext implements ContainerItemContext {
	private final SingleSlotStorage<ItemVariant> slot;

	public SingleSlotContainerItemContext(SingleSlotStorage<ItemVariant> slot) {
		this.slot = Objects.requireNonNull(slot);
	}

	@Override
	public SingleSlotStorage<ItemVariant> getMainSlot() {
		return slot;
	}

	@Override
	public long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext) {
		return 0;
	}

	@Override
	public List<SingleSlotStorage<ItemVariant>> getAdditionalSlots() {
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return "SingleSlotContainerItemContext[%d %s %s]"
				.formatted(slot.getAmount(), slot.getResource(), slot);
	}
}
