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

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookupRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public class EnergyApi {
    public static final BlockApiLookup<EnergyMoveable, @NotNull Direction> MOVEABLE = BlockApiLookupRegistry
            .getLookup(new Identifier("modern_industrialization:energy_moveable"), EnergyMoveable.class, Direction.class);

    static {
        // Compat wrapper for FTL
        MOVEABLE.registerFallback((world, pos, state, blockEntity, direction) -> {
            EnergyIo io = dev.technici4n.fasttransferlib.api.energy.EnergyApi.SIDED.get(world, pos, state, blockEntity, direction);

            if (io != null) {
                return new EnergyInsertable() {
                    @Override
                    public long insertEnergy(long amount) {
                        return (long) Math.floor(amount - io.insert(amount, Simulation.ACT));
                    }

                    @Override
                    public boolean canInsert(CableTier tier) {
                        return io.supportsInsertion();
                    }
                };
            }

            return null;
        });
    }
}
