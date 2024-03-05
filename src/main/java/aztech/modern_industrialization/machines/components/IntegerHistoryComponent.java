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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.machines.IComponent;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

/**
 * Tracks a history of values for different enum values as keys.
 * Values will be serialized using the toString() function of the enum.
 */
public class IntegerHistoryComponent<K extends Enum<K>> implements IComponent {

    protected final Map<K, int[]> histories;
    protected final int[] updatingValues; // indexed by enum ordinal
    protected final double[] averages; // indexed by enum ordinal

    private final K[] keys;
    private final int tickHistorySize;

    public IntegerHistoryComponent(Class<K> keyType, int tickHistorySize) {
        this.keys = keyType.getEnumConstants();
        this.tickHistorySize = tickHistorySize;

        this.histories = new EnumMap<>(keyType);
        this.updatingValues = new int[keys.length];
        this.averages = new double[keys.length];

        for (K key : keys) {
            histories.put(key, new int[tickHistorySize]);
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        for (K key : keys) {
            tag.putIntArray(key.toString(), histories.get(key));
        }
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        for (K key : keys) {
            String keyString = key.toString();
            if (tag.contains(keyString)) {
                int[] array = tag.getIntArray(keyString);
                if (array.length == tickHistorySize) {
                    histories.put(key, array);
                    continue;
                }
            }
            histories.put(key, new int[tickHistorySize]);
        }

        for (K key : keys) {
            double avg = 0;
            int[] values = histories.get(key);
            for (int value : values) {
                avg += value;
            }
            averages[key.ordinal()] = avg / tickHistorySize;
        }
    }

    public double getAverage(K key) {
        double ret = averages[key.ordinal()];
        // Round to zero if very small - negative values might lead to problems.
        return Math.abs(ret) < 1e-9 ? 0 : ret;
    }

    public void clear() {
        for (var array : histories.values()) {
            Arrays.fill(array, 0);
        }
        Arrays.fill(updatingValues, 0);
        Arrays.fill(averages, 0);
    }

    public void tick() {
        for (K key : keys) {
            int i = key.ordinal();
            int[] valuesArray = histories.get(key);

            // Update average
            averages[i] += (double) (updatingValues[i] - valuesArray[tickHistorySize - 1]) / tickHistorySize;

            // Shift values by 1 and add updating value at the beginning.
            System.arraycopy(valuesArray, 0, valuesArray, 1, tickHistorySize - 1);
            valuesArray[0] = updatingValues[i];
            updatingValues[i] = 0;
        }
    }

    public void addValue(K key, int delta) {
        updatingValues[key.ordinal()] += delta;
    }
}
