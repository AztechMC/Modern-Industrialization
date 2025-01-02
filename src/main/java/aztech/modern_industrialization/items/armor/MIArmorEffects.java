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
import aztech.modern_industrialization.MIRegistries;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class MIArmorEffects {
    private MIArmorEffects() {
    }

    public static boolean quantumArmorPreventsDamage(LivingEntity entity) {
        double parts = entity.getAttributeValue(MIRegistries.QUANTUM_ARMOR);
        return ThreadLocalRandom.current().nextDouble() < parts / 4d;
    }

    public static boolean canTankFlyIntoWall(ItemStack helmet) {
        return helmet.getItem() == MIItem.RUBBER_HELMET.asItem() || helmet.getItem() == MIItem.QUANTUM_HELMET.asItem();
    }

    public static boolean canTankFall(ItemStack boots) {
        return boots.getItem() == MIItem.RUBBER_BOOTS.asItem() || boots.getItem() == MIItem.QUANTUM_BOOTS.asItem();
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, event -> {
            if (quantumArmorPreventsDamage(event.getEntity())) {
                event.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener(LivingIncomingDamageEvent.class, event -> {
            var entity = event.getEntity();
            var source = event.getContainer().getSource();
            float amount = event.getContainer().getOriginalDamage();

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
                tankingStack.hurtAndBreak(intAmount, entity, es);
                event.setCanceled(true);
            }
        });
    }
}
