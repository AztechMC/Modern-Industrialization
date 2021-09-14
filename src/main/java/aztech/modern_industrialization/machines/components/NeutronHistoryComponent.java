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
import aztech.modern_industrialization.nuclear.NeutronType;
import net.minecraft.nbt.NbtCompound;

public class NeutronHistoryComponent implements IComponent {

    public static final int TICK_HISTORY_SIZE = 60;

    private int[] fastNeutronReceivedHistory = new int[TICK_HISTORY_SIZE];
    private int[] thermalNeutronReceivedHistory = new int[TICK_HISTORY_SIZE];

    private int[] fastNeutronFluxHistory = new int[TICK_HISTORY_SIZE];
    private int[] thermalNeutronFluxHistory = new int[TICK_HISTORY_SIZE];

    private int[] neutronGenerationHistory = new int[TICK_HISTORY_SIZE];

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putIntArray("fastNeutronReceivedHistory", fastNeutronReceivedHistory);
        tag.putIntArray("thermalNeutronReceivedHistory", thermalNeutronReceivedHistory);
        tag.putIntArray("fastNeutronFluxHistory", fastNeutronFluxHistory);
        tag.putIntArray("thermalNeutronFluxHistory", thermalNeutronFluxHistory);
        tag.putIntArray("neutronGenerationHistory", neutronGenerationHistory);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        if (tag.contains("fastNeutronReceivedHistory")) {
            fastNeutronReceivedHistory = tag.getIntArray("fastNeutronReceivedHistory");
            if (fastNeutronReceivedHistory.length != TICK_HISTORY_SIZE) {
                fastNeutronReceivedHistory = new int[TICK_HISTORY_SIZE];
            }
        }
        if (tag.contains("thermalNeutronReceivedHistory")) {
            thermalNeutronReceivedHistory = tag.getIntArray("thermalNeutronReceivedHistory");
            if (thermalNeutronReceivedHistory.length != TICK_HISTORY_SIZE) {
                thermalNeutronReceivedHistory = new int[TICK_HISTORY_SIZE];
            }
        }
        if (tag.contains("fastNeutronFluxHistory")) {
            fastNeutronFluxHistory = tag.getIntArray("fastNeutronFluxHistory");
            if (fastNeutronFluxHistory.length != TICK_HISTORY_SIZE) {
                fastNeutronFluxHistory = new int[TICK_HISTORY_SIZE];
            }
        }
        if (tag.contains("thermalNeutronFluxHistory")) {
            thermalNeutronFluxHistory = tag.getIntArray("thermalNeutronFluxHistory");
            if (thermalNeutronFluxHistory.length != TICK_HISTORY_SIZE) {
                thermalNeutronFluxHistory = new int[TICK_HISTORY_SIZE];
            }
        }
        if (tag.contains("neutronGenerationHistory")) {
            neutronGenerationHistory = tag.getIntArray("neutronGenerationHistory");
            if (neutronGenerationHistory.length != TICK_HISTORY_SIZE) {
                neutronGenerationHistory = new int[TICK_HISTORY_SIZE];
            }
        }

    }

    public double getAverageReceived(NeutronType type) {
        double avg = 0;
        for (int i = 0; i < TICK_HISTORY_SIZE; i++) {
            if (type == NeutronType.FAST) {
                avg += fastNeutronReceivedHistory[i];
            } else if (type == NeutronType.THERMAL) {
                avg += thermalNeutronFluxHistory[i];
            } else {
                avg += (fastNeutronReceivedHistory[i] + thermalNeutronFluxHistory[i]);
            }
        }
        return avg / TICK_HISTORY_SIZE;
    }

    public double getAverageFlux(NeutronType type) {
        double avg = 0;
        for (int i = 0; i < TICK_HISTORY_SIZE; i++) {
            if (type == NeutronType.FAST) {
                avg += fastNeutronFluxHistory[i];
            } else if (type == NeutronType.THERMAL) {
                avg += thermalNeutronFluxHistory[i];
            } else {
                avg += (fastNeutronFluxHistory[i] + thermalNeutronFluxHistory[i]);
            }
        }
        return avg / TICK_HISTORY_SIZE;
    }

    public double getAverageGeneration() {
        double avg = 0;
        for (int i = 0; i < TICK_HISTORY_SIZE; i++) {
            avg += neutronGenerationHistory[i];
        }
        return avg / TICK_HISTORY_SIZE;
    }

    public void tick(int fastNeutronReceived, int thermalNeutronReceived, int fastNeutronFlux, int thermalNeutronFlux, int generatedNeutron) {
        for (int i = TICK_HISTORY_SIZE - 1; i > 0; i--) {
            fastNeutronReceivedHistory[i] = fastNeutronReceivedHistory[i - 1];
            thermalNeutronReceivedHistory[i] = thermalNeutronReceivedHistory[i - 1];
            fastNeutronFluxHistory[i] = fastNeutronFluxHistory[i - 1];
            thermalNeutronFluxHistory[i] = thermalNeutronFluxHistory[i - 1];
            neutronGenerationHistory[i] = neutronGenerationHistory[i - 1];
        }

        fastNeutronReceivedHistory[0] = fastNeutronReceived;
        thermalNeutronReceivedHistory[0] = thermalNeutronReceived;
        fastNeutronFluxHistory[0] = fastNeutronFlux;
        thermalNeutronFluxHistory[0] = thermalNeutronFlux;
        neutronGenerationHistory[0] = generatedNeutron;
    }

}
