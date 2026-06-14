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

import aztech.modern_industrialization.nuclear.NeutronType;

public class NeutronHistoryComponent extends IntegerHistoryComponent<NeutronHistoryComponent.Type> {
    // lowerCamelCase names are used to have that as the serialized form.
    public enum Type {
        fastNeutronReceived,
        fastNeutronFlux,
        thermalNeutronReceived,
        thermalNeutronFlux,
        neutronGeneration,
        euGeneration
    }

    public NeutronHistoryComponent() {
        super(Type.class, 100);
    }

    public double getAverageReceived(NeutronType type) {
        if (type == FAST) {
            return getAverage(Type.fastNeutronReceived);
        } else if (type == THERMAL) {
            return getAverage(Type.thermalNeutronReceived);
        } else if (type == NeutronType.BOTH) {
            return getAverageReceived(FAST) + getAverageReceived(THERMAL);
        } else {
            return 0;
        }
    }

    public double getAverageFlux(NeutronType type) {
        if (type == FAST) {
            return getAverage(Type.fastNeutronFlux);
        } else if (type == THERMAL) {
            return getAverage(Type.thermalNeutronFlux);
        } else if (type == NeutronType.BOTH) {
            return getAverageFlux(FAST) + getAverageFlux(THERMAL);
        } else {
            return 0;
        }
    }

    public double getAverageGeneration() {
        return getAverage(Type.neutronGeneration);
    }

    public double getAverageEuGeneration() {
        return getAverage(Type.euGeneration);
    }
}
