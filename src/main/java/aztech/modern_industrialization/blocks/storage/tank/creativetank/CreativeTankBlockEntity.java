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

package aztech.modern_industrialization.blocks.storage.tank.creativetank;

import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class CreativeTankBlockEntity extends AbstractTankBlockEntity {
    public CreativeTankBlockEntity(BlockPos pos, BlockState state) {
        super(MIRegistries.CREATIVE_TANK_BE.get(), pos, state);
    }

    @Override
    public boolean onPlayerUse(Player player, InteractionHand hand) {
        if (resource.isEmpty()) {
            var fluid = FluidUtil.getFirstStackContained(player.getItemInHand(hand));
            if (!fluid.isEmpty()) {
                setFluid(FluidResource.of(fluid));
                return true;
            }
            return !resource.isEmpty();
        } else {
            var fluidHandler = ItemAccess.forPlayerInteraction(player, hand).getCapability(Capabilities.Fluid.ITEM);
            if (fluidHandler != null) {
                try (var tx = Transaction.openRoot()) {
                    int inserted = fluidHandler.insert(resource, Integer.MAX_VALUE, tx);
                    if (inserted > 0) {
                        tx.commit();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void setFluid(FluidResource resource) {
        this.resource = resource;
        onChanged();
    }
}
