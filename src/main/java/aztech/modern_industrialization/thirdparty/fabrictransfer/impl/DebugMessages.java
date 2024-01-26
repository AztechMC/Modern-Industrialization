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
package aztech.modern_industrialization.thirdparty.fabrictransfer.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

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
