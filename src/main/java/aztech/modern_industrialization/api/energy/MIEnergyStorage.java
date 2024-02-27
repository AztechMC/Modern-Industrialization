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
package aztech.modern_industrialization.api.energy;

import dev.technici4n.grandpower.api.ILongEnergyStorage;
import org.jetbrains.annotations.ApiStatus;

public interface MIEnergyStorage extends ILongEnergyStorage {
    boolean canConnect(CableTier cableTier);

    /**
     * Overload of {@link #canConnect(CableTier)} that's easier to access by reflection.
     */
    @ApiStatus.NonExtendable
    default boolean canConnect(String cableTier) {
        return switch (cableTier) {
        case "lv" -> canConnect(CableTier.LV);
        case "mv" -> canConnect(CableTier.MV);
        case "hv" -> canConnect(CableTier.HV);
        case "ev" -> canConnect(CableTier.EV);
        case "superconductor" -> canConnect(CableTier.SUPERCONDUCTOR);
        default -> false;
        };
    }

    interface NoExtract extends MIEnergyStorage {
        @Override
        default boolean canExtract() {
            return false;
        }

        @Override
        default long extract(long maxExtract, boolean simulate) {
            return 0;
        }
    }

    interface NoInsert extends MIEnergyStorage {
        @Override
        default boolean canReceive() {
            return false;
        }

        @Override
        default long receive(long amount, boolean simulate) {
            return 0;
        }
    }
}
