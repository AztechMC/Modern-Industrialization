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

public interface INuclearGrid {

    int getSizeX();

    int getSizeY();

    boolean isFuel(int x, int y);

    double getTemperature(int x, int y);

    boolean ok(int x, int y);

    double getHeatTransferCoeff(int x, int y);

    void setTemperature(int x, int y, double temp);

    int neutronProducedFromSimulation(int i, int j);

    double interactionTotalProbability(int i, int j, NeutronType type);

    double interactionRelativeProbability(int i, int j, NeutronType type, NeutronInteraction absorption);

    void absorbNeutrons(int i, int j, NeutronType type, int neutronNumber);

    void putHeat(int i, int j, int eu);

    void registerNeutronFate(int neutronNumber, NeutronType type, NeutronFate escape);

    void registerNeutronCreation(int neutronNumber, NeutronType type);

    int getSupplementaryHeatByNeutronGenerated(int i, int j);

    void nuclearTick(int i, int j);

    double neutronSlowingProbability(int posX, int posY);
}
