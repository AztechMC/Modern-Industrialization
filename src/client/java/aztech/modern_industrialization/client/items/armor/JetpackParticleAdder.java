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

package aztech.modern_industrialization.client.items.armor;

import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.armor.JetpackItem;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class JetpackParticleAdder {
    public static void addJetpackParticles(Minecraft client) {
        if (client.level != null && !client.isPaused()) {
            for (Player player : client.level.players()) {
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem jetpack) {
                    if (jetpack.isActivated(chest) && FluidFuelItemHelper.getAmount(chest) > 0) {
                        Random r = ThreadLocalRandom.current();
                        if (player.isFallFlying()) {
                            Vec3 velocity = player.getDeltaMovement();
                            client.level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY(), player.getZ(), -velocity.x, -velocity.y,
                                    -velocity.z);
                        } else {
                            client.level.addParticle(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), 0, -1, 0);
                        }
                    }
                }
            }
        }
    }
}
