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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.wrapper.PlayerMainInvWrapper;

public class LubricantHelper {

    public static final int mbPerTick = 25;

    public static InteractionResult onUse(CrafterComponent crafter, Player player, InteractionHand hand) {
        if (crafter.hasActiveRecipe()) {
            int tick = crafter.getEfficiencyTicks();
            int maxTick = crafter.getMaxEfficiencyTicks();
            int rem = maxTick - tick;
            if (rem > 0) {
                int maxAllowedLubricant = rem * mbPerTick;
                FluidTank interactionTank = new FluidTank(maxAllowedLubricant, stack -> stack.getFluid() == MIFluids.LUBRICANT.asFluid());
                var result = FluidUtil.tryEmptyContainerAndStow(
                        player.getItemInHand(hand),
                        interactionTank,
                        new PlayerMainInvWrapper(player.getInventory()),
                        maxAllowedLubricant,
                        player,
                        true);

                if (result.isSuccess() && interactionTank.getFluidAmount() >= mbPerTick) {
                    crafter.increaseEfficiencyTicks(interactionTank.getFluidAmount() / mbPerTick);
                    player.setItemInHand(hand, result.getResult());
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }
}
