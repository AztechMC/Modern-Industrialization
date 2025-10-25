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

package aztech.modern_industrialization.stats;

import aztech.modern_industrialization.util.TickHelper;
import net.minecraft.nbt.CompoundTag;

public class StatisticValue {
    private long allTime = 0;
    private final long[] pastRates = new long[StatisticsRate.COUNT];
    private final long[] currentRates = new long[StatisticsRate.COUNT];
    private final int[] remainingTicks = new int[StatisticsRate.COUNT];
    private long lastTick = 0;

    public StatisticValue() {}

    public StatisticValue(CompoundTag nbt) {
        allTime = nbt.getLong("at");
        for (var rate : StatisticsRate.values()) {
            pastRates[rate.id] = nbt.getLong("p" + rate.id);
            currentRates[rate.id] = nbt.getLong("c" + rate.id);
            remainingTicks[rate.id] = nbt.getInt("r" + rate.id);
        }
        lastTick = TickHelper.getCurrentTick();
    }

    public CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("at", allTime);
        for (var rate : StatisticsRate.values()) {
            nbt.putLong("p" + rate.id, pastRates[rate.id]);
            nbt.putLong("c" + rate.id, currentRates[rate.id]);
            nbt.putInt("r" + rate.id, remainingTicks[rate.id]);
        }
        return nbt;
    }

    public void add(long toAdd) {
        updateRates();

        allTime += toAdd;
        for (var rate : StatisticsRate.values()) {
            currentRates[rate.id] += toAdd;
        }
    }

    public void updateRates() {
        long tickDiff = TickHelper.getCurrentTick() - lastTick;
        if (tickDiff > 0) {
            for (var rate : StatisticsRate.values()) {
                int i = rate.id;
                if (tickDiff < remainingTicks[i]) {
                    remainingTicks[i] -= tickDiff;
                } else if (tickDiff < rate.ticks + remainingTicks[i]) {
                    pastRates[i] = currentRates[i];
                    currentRates[i] = 0;
                    remainingTicks[i] = rate.ticks - ((int) tickDiff - remainingTicks[i]);
                } else {
                    pastRates[i] = 0;
                    currentRates[i] = 0;
                    remainingTicks[i] = rate.ticks;
                }
            }
        }
    }
}
