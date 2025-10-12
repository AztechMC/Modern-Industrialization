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

public class NuclearConstant {
    public static final int EU_FOR_FAST_NEUTRON = 8;
    public static final int DESINTEGRATION_BY_ROD = 10240000;
    public static final double BASE_HEAT_CONDUCTION = 0.01;
    public static final double BASE_NEUTRON = 0.1;
    public static final int MAX_TEMPERATURE = 3250;
    public static final int EU_PER_DEGREE = 64;
    public static final int MAX_HATCH_EU_PRODUCTION = 8192;

    public enum ScatteringType {
        ULTRA_LIGHT(0.05),
        LIGHT(0.2),
        MEDIUM(0.5),
        HEAVY(0.85);

        ScatteringType(double fastFraction) {
            this.fastFraction = fastFraction;
            this.slowFraction = 1 - this.fastFraction;
        }

        public final double fastFraction;
        public final double slowFraction;
    }

    public static final IsotopeParams HYDROGEN = new IsotopeParams(0.1, 0.05, 0.25, 0.75);
    public static final IsotopeParams DEUTERIUM = new IsotopeParams(0.02, 0.01, 0.15, 0.65);

    public static final IsotopeParams CADMIUM = new IsotopeParams(0.95, 0.9, 0.05, 0.1);
    public static final IsotopeParams CARBON = new IsotopeParams(0.01, 0.005, 0.5, 0.85);

    public static final IsotopeParams INVAR = new IsotopeParams(0.002, 0.001, 0.2, 0.5);
}
