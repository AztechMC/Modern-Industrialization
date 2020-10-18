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

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;

public class JetpackParticleAdder {
    public static void addJetpackParticles(MinecraftClient client) {
        if (client.world != null && !client.isPaused()) {
            for (PlayerEntity player : client.world.getPlayers()) {
                ItemStack chest = player.getEquippedStack(EquipmentSlot.CHEST);
                if (chest.getItem() instanceof JetpackItem) {
                    JetpackItem jetpack = (JetpackItem) chest.getItem();
                    if (jetpack.showParticles(chest) && jetpack.getAmount(chest) > 0) {
                        Random r = ThreadLocalRandom.current();
                        for (int i = 0; i < 20; ++i) {
                            client.world.addParticle(ParticleTypes.FLAME, player.getX(), player.getY() + 1.0, player.getZ(), r.nextFloat() - 0.5, -5,
                                    r.nextFloat() - 0.5);
                        }
                    }
                }
            }
        }
    }
}
