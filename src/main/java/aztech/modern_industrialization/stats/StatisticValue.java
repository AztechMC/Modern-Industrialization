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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;

public class StatisticValue {
    public static final Codec<StatisticValue> CODEC = RecordCodecBuilder.create(instance ->
            instance
                    .group(
                            Codec.LONG.fieldOf("at").forGetter(sv -> sv.allTime),
                            Codec.LONG.fieldOf("p0").forGetter(sv -> sv.pastRates[0]),
                            Codec.LONG.fieldOf("c0").forGetter(sv -> sv.currentRates[0]),
                            Codec.INT.fieldOf("r0").forGetter(sv -> sv.remainingTicks[0]),
                            Codec.LONG.fieldOf("p1").forGetter(sv -> sv.pastRates[1]),
                            Codec.LONG.fieldOf("c1").forGetter(sv -> sv.currentRates[1]),
                            Codec.INT.fieldOf("r1").forGetter(sv -> sv.remainingTicks[1]),
                            Codec.LONG.fieldOf("p2").forGetter(sv -> sv.pastRates[2]),
                            Codec.LONG.fieldOf("c2").forGetter(sv -> sv.currentRates[2]),
                            Codec.INT.fieldOf("r2").forGetter(sv -> sv.remainingTicks[2]))
                    .apply(instance, StatisticValue::new));

    private long allTime = 0;
    private final long[] pastRates = new long[StatisticsRate.COUNT];
    private final long[] currentRates = new long[StatisticsRate.COUNT];
    private final int[] remainingTicks = new int[StatisticsRate.COUNT];
    private long lastTick = 0;

    public StatisticValue() {}

    public StatisticValue(long at, long p0, long c0, int r0, long p1, long c1, int r1, long p2, long c2, int r2) {
        allTime = at;
        pastRates[0] = p0;
        currentRates[0] = c0;
        remainingTicks[0] = r0;
        pastRates[1] = p1;
        currentRates[1] = c1;
        remainingTicks[1] = r1;
        pastRates[2] = p2;
        currentRates[2] = c2;
        remainingTicks[2] = r2;
        lastTick = TickHelper.getCurrentTick();
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
