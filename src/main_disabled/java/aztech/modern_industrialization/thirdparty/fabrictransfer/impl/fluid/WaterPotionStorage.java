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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.ExtractionOnlyStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.base.SingleSlotStorage;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.TransactionContext;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of the storage for a water potion.
 */
public class WaterPotionStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
	private static final FluidVariant CONTAINED_FLUID = FluidVariant.of(Fluids.WATER);
	private static final long CONTAINED_AMOUNT = FluidConstants.BOTTLE;

	@Nullable
	public static WaterPotionStorage find(ContainerItemContext context) {
		return isWaterPotion(context) ? new WaterPotionStorage(context) : null;
	}

	private static boolean isWaterPotion(ContainerItemContext context) {
		ItemVariant variant = context.getItemVariant();

		return variant.isOf(Items.POTION) && PotionUtil.getPotion(variant.getNbt()) == Potions.WATER;
	}

	private final ContainerItemContext context;

	private WaterPotionStorage(ContainerItemContext context) {
		this.context = context;
	}

	private boolean isWaterPotion() {
		return isWaterPotion(context);
	}

	private ItemVariant mapToGlassBottle() {
		ItemStack newStack = context.getItemVariant().toStack();
		PotionUtil.setPotion(newStack, Potions.EMPTY);
		return ItemVariant.of(Items.GLASS_BOTTLE, newStack.getNbt());
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		// Not a water potion anymore
		if (!isWaterPotion()) return 0;

		// Make sure that the fluid and the amount match.
		if (resource.equals(CONTAINED_FLUID) && maxAmount >= CONTAINED_AMOUNT) {
			if (context.exchange(mapToGlassBottle(), 1, transaction) == 1) {
				// Conversion ok!
				return CONTAINED_AMOUNT;
			}
		}

		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		// Only contains a resource if this is still a water potion.
		if (isWaterPotion()) {
			return CONTAINED_FLUID;
		} else {
			return FluidVariant.blank();
		}
	}

	@Override
	public long getAmount() {
		if (isWaterPotion()) {
			return CONTAINED_AMOUNT;
		} else {
			return 0;
		}
	}

	@Override
	public long getCapacity() {
		// Capacity is the same as the amount.
		return getAmount();
	}

	@Override
	public String toString() {
		return "WaterPotionStorage[" + context + "]";
	}
}
