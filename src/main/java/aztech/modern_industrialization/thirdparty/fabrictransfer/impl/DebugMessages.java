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

package aztech.modern_industrialization.thirdparty.fabrictransfer.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import javax.swing.text.Position;

public final class DebugMessages {
	public static String forGlobalPos(@Nullable Level world, BlockPos pos) {
		String dimension = world != null ? world.dimension().location().toString() : "<no dimension>";
		return dimension + "@" + pos.toShortString();
	}

	public static String forPlayer(Player player) {
		return player.getName() + "/" + player.getStringUUID();
	}

	public static String forInventory(@Nullable Container inventory) {
		if (inventory == null) {
			return "~~NULL~~"; // like in crash reports
		} else if (inventory instanceof Inventory playerInventory) {
			return forPlayer(playerInventory.player);
		} else {
			String result = inventory.toString();

			if (inventory instanceof BlockEntity blockEntity) {
				result += " (%s, %s)".formatted(blockEntity.getBlockState(), forGlobalPos(blockEntity.getLevel(), blockEntity.getBlockPos()));
			}

			return result;
		}
	}
}
