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

import aztech.modern_industrialization.MIBlockEntityTypes;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeTankBlockEntity extends AbstractTankBlockEntity {

    public CreativeTankBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntityTypes.CREATIVE_TANK, pos, state);
    }

    @Override
    public boolean onPlayerUse(Player player) {
        Storage<FluidVariant> handIo = ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND).find(FluidStorage.ITEM);
        if (handIo != null) {
            if (isResourceBlank()) {
                for (StorageView<FluidVariant> view : handIo) {
                    if (!view.isResourceBlank()) {
                        resource = view.getResource();
                        onChanged();
                        break;
                    }
                }
                return !isResourceBlank();
            } else {
                try (Transaction tx = Transaction.openOuter()) {
                    long inserted = handIo.insert(resource, Long.MAX_VALUE, tx);
                    tx.commit();
                    return inserted > 0;
                }
            }
        }
        return false;
    }

}
