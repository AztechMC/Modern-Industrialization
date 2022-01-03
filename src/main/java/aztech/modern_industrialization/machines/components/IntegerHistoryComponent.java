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
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;

public class IntegerHistoryComponent implements IComponent {

    protected final Map<String, int[]> histories = new HashMap<>();
    protected final Map<String, Integer> updatingValue = new HashMap<>();

    public final String[] KEYS;
    public final int TICK_HISTORY_SIZE;

    public IntegerHistoryComponent(String[] keys, int tick_history_size) {
        KEYS = keys;
        TICK_HISTORY_SIZE = tick_history_size;
        clear();
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        for (String key : KEYS) {
            tag.putIntArray(key, histories.get(key));
        }
    }

    @Override
    public void readNbt(CompoundTag tag) {
        for (String key : KEYS) {
            if (tag.contains(key)) {
                int[] array = tag.getIntArray(key);
                if (array.length == TICK_HISTORY_SIZE) {
                    histories.put(key, array);
                    continue;
                }
            }
            histories.put(key, new int[TICK_HISTORY_SIZE]);
        }
    }

    public double getAverage(String key) {
        double avg = 0;
        int[] values = histories.get(key);
        for (int value : values) {
            avg += value;
        }
        return avg / TICK_HISTORY_SIZE;

    }

    public void clear() {
        for (String key : KEYS) {
            histories.put(key, new int[TICK_HISTORY_SIZE]);
            updatingValue.put(key, 0);
        }
    }

    public void tick() {
        for (String key : KEYS) {
            int[] valuesArray = histories.get(key);
            int[] newValues = new int[TICK_HISTORY_SIZE];
            System.arraycopy(valuesArray, 0, newValues, 1, TICK_HISTORY_SIZE - 1);
            newValues[0] = updatingValue.get(key);
            histories.put(key, newValues);
            updatingValue.put(key, 0);
        }
    }

    public void addValue(String key, int delta) {
        if (!updatingValue.containsKey(key)) {
            throw new IllegalArgumentException("No key found for : " + key);
        } else {
            updatingValue.put(key, updatingValue.get(key) + delta);
        }
    }
}
