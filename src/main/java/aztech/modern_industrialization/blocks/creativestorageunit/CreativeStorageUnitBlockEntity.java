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
package aztech.modern_industrialization.blocks.creativestorageunit;

import aztech.modern_industrialization.MIBlockEntityTypes;
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.MIEnergyStorage;
import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.util.Tickable;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeStorageUnitBlockEntity extends FastBlockEntity implements Tickable {
    @SuppressWarnings("unchecked")
    private final BlockApiCache<MIEnergyStorage, Direction>[] caches = new BlockApiCache[6];

    public CreativeStorageUnitBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntityTypes.CREATIVE_STORAGE_UNIT, pos, state);
    }

    @Override
    public void tick() {
        if (!level.isClientSide) {
            var serverWorld = (ServerLevel) level;

            for (Direction direction : Direction.values()) {
                if (caches[direction.ordinal()] == null) {
                    caches[direction.ordinal()] = BlockApiCache.create(EnergyApi.SIDED, serverWorld, worldPosition.relative(direction));
                }

                var target = caches[direction.ordinal()].find(direction.getOpposite());
                if (target != null) {
                    try (var tx = Transaction.openOuter()) {
                        target.insert(Long.MAX_VALUE, tx);
                        tx.commit();
                    }
                }
            }
        }
    }
}
