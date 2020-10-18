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
package aztech.modern_industrialization.nuclear;

import java.util.Random;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NuclearFuel extends MINuclearItem {

    private int multiplier;
    private NuclearFuelType type;

    public NuclearFuel(String id, NuclearFuelType type, int multiplier, Item depleted) {
        super(id, type.durability, type.maxHeat, depleted);
        this.type = type;
        this.multiplier = multiplier;
    }

    @Override
    public double getNeutronPulse(ItemStack is) {
        return type.neutronPulse * multiplier;
    }

    @Override
    public double getHeatProduction(ItemStack is, double neutronReceived) {
        return type.heatProduction * multiplier * (1.0d + neutronReceived * 0.5) * (1 - (0.5 * getHeat(is)) / getMaxHeat());
    }

    @Override
    public double getNeutronReflection(ItemStack is, int angle) {
        return 0;
    }

    @Override
    public double getHeatTransferMax(ItemStack is) {
        return 0;
    }

    @Override
    public double getHeatTransferNeighbourFraction(ItemStack is) {
        return 0;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public NuclearFuelType getType() {
        return type;
    }

    @Override
    public void tick(ItemStack is, NuclearReactorBlockEntity nuclearReactor, double neutronPulse, Random rand) {
        int damage = is.getDamage();
        damage += 1;
        damage += NuclearReactorLogic.doubleToInt(neutronPulse * 0.1, rand);
        is.setDamage(Math.min(this.getDurability(), damage));

    }
}
