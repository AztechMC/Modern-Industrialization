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
package aztech.modern_industrialization.api.energy;

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.util.Simulation;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.api.EnergyStorage;

public class EnergyApi {
    public static final BlockApiLookup<EnergyMoveable, @NotNull Direction> MOVEABLE = BlockApiLookup
            .get(new Identifier("modern_industrialization:energy_moveable"), EnergyMoveable.class, Direction.class);

    public static final EnergyExtractable CREATIVE_EXTRACTABLE = new EnergyExtractable() {
        @Override
        public long extractEnergy(long maxAmount, Simulation simulation) {
            return maxAmount;
        }

        @Override
        public boolean canExtract(CableTier tier) {
            return true;
        }
    };

    static {
        // Compat wrapper for TR energy
        MOVEABLE.registerFallback((world, pos, state, blockEntity, context) -> {
            EnergyStorage storage = EnergyStorage.SIDED.find(world, pos, state, blockEntity, context);

            if (storage == null || !storage.supportsInsertion()) {
                return null;
            }
            return new EnergyInsertable() {
                @Override
                public long insertEnergy(long amount, Simulation simulation) {
                    long inserted;
                    try (Transaction tx = Transaction.openOuter()) {
                        inserted = storage.insert(amount, tx);
                        if (simulation.isActing())
                            tx.commit();
                    }

                    if (inserted < 0) {
                        ModernIndustrialization.LOGGER.warn(String.format(
                                "Tried inserting up to %d energy, but broken EnergyStorage %s inserted a negative amount of energy %d.%nWorld and position: %s %s.",
                                amount, storage, inserted, world, pos));
                        return 0;
                    } else {
                        return inserted;
                    }
                }

                @Override
                public boolean canInsert(CableTier tier) {
                    return true;
                }
            };
        });
    }
}
