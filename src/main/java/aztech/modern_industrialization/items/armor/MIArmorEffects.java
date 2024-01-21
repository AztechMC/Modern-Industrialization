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

import aztech.modern_industrialization.MIItem;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

public class MIArmorEffects {
    private MIArmorEffects() {
    }

    public static boolean quantumArmorPreventsDamage(LivingEntity entity) {
        int parts = 0;
        for (QuantumArmorItem item : QuantumArmorItem.ITEMS) {
            if (entity.getItemBySlot(item.getType().getSlot()).getItem() == item) {
                parts++;
            }
        }
        return ThreadLocalRandom.current().nextDouble() < parts / 4d;
    }

    public static boolean allowFlight(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        return allowFlight(chest);
    }

    public static boolean allowFlight(ItemStack chest) {
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

    public static void init() {
        NeoForge.EVENT_BUS.addListener(TickEvent.PlayerTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.START && event.side.isServer()) {
                if (allowFlight(event.player)) {
                    event.player.getAbilities().mayfly = true;
                    event.player.onUpdateAbilities();
                }
            }
        });

        NeoForge.EVENT_BUS.addListener(LivingEquipmentChangeEvent.class, event -> {
            if (event.getSlot() == EquipmentSlot.CHEST && allowFlight(event.getFrom()) && !allowFlight(event.getTo())) {
                // Remove ability :P
                if (event.getEntity() instanceof Player player) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                    player.onUpdateAbilities();
                }
            }
        });

        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.class, event -> {
            var entity = event.getEntity();
            var source = event.getSource();
            float amount = event.getAmount();

            if (quantumArmorPreventsDamage(entity)) {
                event.setCanceled(true);
                return;
            }

            // Find a suitable stack that can "tank" the damage
            ItemStack tankingStack = null;
            EquipmentSlot es = null;
            if (source.is(DamageTypes.FLY_INTO_WALL)) {
                es = EquipmentSlot.HEAD;
                ItemStack head = entity.getItemBySlot(es);
                if (MIArmorEffects.canTankFlyIntoWall(head)) {
                    tankingStack = head;
                }
            } else if (source.is(DamageTypes.FALL)) {
                es = EquipmentSlot.FEET;
                ItemStack head = entity.getItemBySlot(es);
                if (MIArmorEffects.canTankFall(head)) {
                    tankingStack = head;
                }
            }
            // Have the stack tank the damage
            if (tankingStack != null) {
                int intAmount = (int) Math.ceil(amount);
                final EquipmentSlot equipmentSlot = es;
                tankingStack.hurtAndBreak(intAmount, entity, p -> entity.broadcastBreakEvent(equipmentSlot));
                event.setCanceled(true);
            }
        });
    }
}
