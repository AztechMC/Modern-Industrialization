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

import static aztech.modern_industrialization.nuclear.NeutronType.FAST;
import static aztech.modern_industrialization.nuclear.NeutronType.THERMAL;

import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.nuclear.NeutronType;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.NbtCompound;

public class NeutronHistoryComponent implements IComponent {

    public static final int TICK_HISTORY_SIZE = 100;

    private final Map<String, int[]> histories = new HashMap<>();
    private final Map<String, Integer> updatingValue = new HashMap<>();

    public static final String[] KEYS = { "fastNeutronReceived", "fastNeutronFlux", "thermalNeutronReceived", "thermalNeutronFlux",
            "neutronGeneration", "euGeneration" };

    public NeutronHistoryComponent() {
        for (String key : KEYS) {
            histories.put(key, new int[TICK_HISTORY_SIZE]);
            updatingValue.put(key, 0);
        }
    }

    @Override
    public void writeNbt(NbtCompound tag) {
        for (String key : KEYS) {
            tag.putIntArray(key, histories.get(key));
        }
    }

    @Override
    public void readNbt(NbtCompound tag) {
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

    public double getAverageReceived(NeutronType type) {
        if (type == FAST) {
            return getAverage("fastNeutronReceived");
        } else if (type == THERMAL) {
            return getAverage("thermalNeutronReceived");
        } else if (type == NeutronType.BOTH) {
            return getAverageReceived(FAST) + getAverageReceived(THERMAL);
        } else {
            return 0;
        }
    }

    public double getAverageFlux(NeutronType type) {
        if (type == FAST) {
            return getAverage("fastNeutronFlux");
        } else if (type == THERMAL) {
            return getAverage("thermalNeutronFlux");
        } else if (type == NeutronType.BOTH) {
            return getAverageFlux(FAST) + getAverageFlux(THERMAL);
        } else {
            return 0;
        }
    }

    public double getAverageGeneration() {
        return getAverage("neutronGeneration");
    }

    public double getAverageEuGeneration() {
        return getAverage("euGeneration");
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
