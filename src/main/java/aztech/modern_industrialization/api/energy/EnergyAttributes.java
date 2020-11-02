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

import alexiil.mc.lib.attributes.Attribute;
import alexiil.mc.lib.attributes.Attributes;
import net.minecraft.block.entity.BlockEntity;
import team.reborn.energy.Energy;
import team.reborn.energy.EnergyHandler;

public class EnergyAttributes {
    public static final Attribute<EnergyInsertable> INSERTABLE;
    public static final Attribute<EnergyExtractable> EXTRACTABLE;

    static {
        INSERTABLE = Attributes.create(EnergyInsertable.class);
        EXTRACTABLE = Attributes.create(EnergyExtractable.class);

        INSERTABLE.appendBlockAdder(((world, pos, state, to) -> {
            BlockEntity be = world.getBlockEntity(pos);
            if (Energy.valid(be)) {
                EnergyHandler handler = Energy.of(be);
                handler.side(to.getTargetSide());
                to.add(new EnergyInsertable() {
                    @Override
                    public long insertEnergy(long amount) {
                        double maxIns = Math.min(Math.min(handler.getMaxStored() - handler.getEnergy(), amount), handler.getMaxInput());
                        long ins = Math.min(amount, (long) Math.floor(maxIns));
                        handler.insert(ins);
                        return amount - ins;
                    }

                    @Override
                    public boolean canInsert(CableTier tier) {
                        return true;
                    }
                });
            }
        }));
    }
}
