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

import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import org.jetbrains.annotations.Nullable;

public final class NuclearGrid {
    private final int sizeX;
    private final int sizeY;
    private final @Nullable NuclearHatch[][] hatchesGrid;

    public NuclearGrid(int sizeX, int sizeY, @Nullable NuclearHatch[][] hatchesGrid) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.hatchesGrid = hatchesGrid;
    }

    public int getSizeX() {
        return sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    @Nullable
    public INuclearTile getNuclearTile(int x, int y) {
        return hatchesGrid[x][y];
    }

    public void registerNeutronFate(int neutronNumber, NeutronType type, NeutronFate escape) {
    }

    public void registerNeutronCreation(int neutronNumber, NeutronType type) {
    }
}
