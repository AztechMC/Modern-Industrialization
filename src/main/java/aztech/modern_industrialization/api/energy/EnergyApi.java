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

import net.fabricmc.fabric.api.provider.v1.ContextKey;
import net.fabricmc.fabric.api.provider.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.provider.v1.block.BlockApiLookupRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergyHandler;

public class EnergyApi {
    public static final ContextKey<@NotNull Direction> SIDED = ContextKey.of(Direction.class, new Identifier("modern_industrialization:sided"));
    public static final BlockApiLookup<EnergyMoveable, @NotNull Direction> MOVEABLE = BlockApiLookupRegistry
            .getLookup(new Identifier("modern_industrialization:energy_moveable"), SIDED);

    static {
        // Compat wrapper for tech reborn
        MOVEABLE.registerBlockEntityFallback(((blockEntity, direction) -> {
            if (Energy.valid(blockEntity)) {
                EnergyHandler handler = Energy.of(blockEntity);
                handler.side(direction);
                return new EnergyInsertable() {
                    @Override
                    public long insertEnergy(long amount) {
                        double maxIns = Math.min(Math.min(handler.getMaxStored() - handler.getEnergy(), amount), handler.getMaxInput());
                        long ins = Math.min(amount, (long) Math.floor(maxIns));
                        if (ins <= 0) {
                            return amount;
                        }

                        handler.insert(ins);
                        return amount - ins;
                    }

                    @Override
                    public boolean canInsert(CableTier tier) {
                        return true;
                    }
                };
            } else {
                return null;
            }
        }));
    }
}
