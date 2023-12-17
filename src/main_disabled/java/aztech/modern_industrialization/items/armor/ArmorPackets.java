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
package aztech.modern_industrialization.items.armor;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.util.UnsidedPacketHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorPackets {
    public static final ResourceLocation UPDATE_KEYS = new MIIdentifier("update_keys");
    public static final UnsidedPacketHandler ON_UPDATE_KEYS = (player, buffer) -> {
        boolean up = buffer.readBoolean();
        return () -> MIKeyMap.update(player, up);
    };
    public static final ResourceLocation ACTIVATE_CHEST = new MIIdentifier("activate_chest");
    public static final UnsidedPacketHandler ON_ACTIVATE_CHEST = (player, buffer) -> {
        boolean activated = buffer.readBoolean();
        return () -> activateChest(player, activated);
    };

    static void activateChest(Player player, boolean activated) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof ActivatableChestItem activatable) {
            activatable.setActivated(chest, activated);
        }
    }
}
