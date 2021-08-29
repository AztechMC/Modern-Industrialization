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
import net.minecraft.nbt.NbtCompound;

public class NeutronHistoryComponent implements IComponent {

    public static final int TICK_HISTORY_SIZE = 60;

    private int[] neutronReceivedHistory = new int[TICK_HISTORY_SIZE];

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putIntArray("neutronHistory", neutronReceivedHistory);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if (tag.contains("neutronReceivedHistory")) {
            neutronReceivedHistory = tag.getIntArray("neutronReceivedHistory");
        }
        if (neutronReceivedHistory.length != TICK_HISTORY_SIZE) {
            neutronReceivedHistory = new int[TICK_HISTORY_SIZE];

        }

    }

    public double getAverage() {
        double avg = 0;
        for (int i = 0; i < TICK_HISTORY_SIZE; i++) {
            avg += neutronReceivedHistory[i];
        }
        return avg / TICK_HISTORY_SIZE;
    }

    public void tick(int lastNeutronReceived) {
        for (int i = 0; i + 1 < TICK_HISTORY_SIZE; i++) {
            neutronReceivedHistory[i + 1] = neutronReceivedHistory[i];
        }
        neutronReceivedHistory[0] = lastNeutronReceived;
    }

}
