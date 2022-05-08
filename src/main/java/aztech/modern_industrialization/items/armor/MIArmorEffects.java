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
import aztech.modern_industrialization.MIItem;
import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MIArmorEffects {
    private MIArmorEffects() {
    }

    public static boolean quantumArmorPreventsDamage(Player player) {
        int parts = 0;
        for (QuantumArmorItem item : QuantumArmorItem.ITEMS) {
            if (player.getItemBySlot(item.getSlot()).getItem() == item) {
                parts++;
            }
        }
        return ThreadLocalRandom.current().nextDouble() < parts / 4d;
    }

    public static boolean allowFlight(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);

        if (chest.getItem() instanceof GraviChestPlateItem gsp && gsp.isActivated(chest) && gsp.getEnergy(chest) > 0) {
            return true;
        }
        if (chest.getItem() == MIItem.QUANTUM_CHESTPLATE.asItem()) {
            return true;
        }
        return false;
    }

    public static boolean canTankFlyIntoWall(ItemStack helmet) {
        return helmet.getItem() == MIItem.RUBBER_HELMET.asItem() || helmet.getItem() == MIItem.QUANTUM_HELMET.asItem();
    }

    public static boolean canTankFall(ItemStack boots) {
        return boots.getItem() == MIItem.RUBBER_BOOTS.asItem() || boots.getItem() == MIItem.QUANTUM_BOOTS.asItem();
    }

    public static final AbilitySource SRC = Pal.getAbilitySource(new MIIdentifier("modernindustrialization"));

    public static void init() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            for (var player : world.players()) {
                if (allowFlight(player)) {
                    SRC.grantTo(player, VanillaAbilities.ALLOW_FLYING);
                } else {
                    SRC.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
                }
            }
        });
    }
}
