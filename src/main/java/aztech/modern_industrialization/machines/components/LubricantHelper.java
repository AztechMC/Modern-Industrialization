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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.MIFluids;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class LubricantHelper {

    public static final int mbPerTick = 25;
    private static final int dropPerTick = mbPerTick * 81;

    public static InteractionResult onUse(CrafterComponent crafter, Player player, InteractionHand hand) {
        if (crafter.hasActiveRecipe()) {
            int tick = crafter.getEfficiencyTicks();
            int maxTick = crafter.getMaxEfficiencyTicks();
            int rem = maxTick - tick;
            if (rem > 0) {
                Storage<FluidVariant> handIo = FluidStorage.ITEM.find(player.getItemInHand(hand), ContainerItemContext.ofPlayerHand(player, hand));
                if (handIo != null) {
                    try (Transaction tx = Transaction.openOuter()) {
                        long extracted = handIo.extract(MIFluids.LUBRICANT.variant(), (long) rem * dropPerTick, tx);
                        long addedTicks = (extracted + dropPerTick - 1) / dropPerTick;

                        if (addedTicks > 0) {
                            player.playNotifySound(FluidVariantAttributes.getFillSound(MIFluids.LUBRICANT.variant()), SoundSource.BLOCKS, 1, 1);
                            crafter.increaseEfficiencyTicks((int) addedTicks);
                            tx.commit();
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}
