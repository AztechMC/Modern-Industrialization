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
package aztech.modern_industrialization.materials.part;

import static aztech.modern_industrialization.materials.part.MIParts.FUEL_ROD;

import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.IsotopeParams;
import aztech.modern_industrialization.nuclear.NuclearAbsorbable;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearOrder;

public class ControlRodPart implements PartKeyProvider {

    @Override
    public PartKey key() {
        return FUEL_ROD.key;
    }

    public PartTemplate of(int maxTemperature, double heatConduction, double thermalAbsorbProba, double fastAbsorbProba,
            double thermalScatteringProba, double fastScatteringProba, NuclearConstant.ScatteringType scatteringType, double size) {
        return new PartTemplate("Control Rod", key())
                .withRegister((partContext, part, itemPath1, itemId, itemTag, itemEnglishName) -> NuclearAbsorbable
                        .of(partContext.getMaterialEnglishName() + " Control Rod", itemPath1, maxTemperature,
                                heatConduction * NuclearConstant.BASE_HEAT_CONDUCTION,
                                INeutronBehaviour.of(scatteringType,
                                        new IsotopeParams(thermalAbsorbProba, fastAbsorbProba, thermalScatteringProba,
                                                fastScatteringProba),
                                        size),
                                NuclearConstant.DESINTEGRATION_BY_ROD,
                                SortOrder.NUCLEAR.create(NuclearOrder.CONTROL_ROD)))
                .withCustomPath("%s_control_rod");
    }

    public PartTemplate of(int maxTemperature, double heatConduction, NuclearConstant.ScatteringType scatteringType,
            IsotopeParams params, double size) {
        return new PartTemplate("Control Rod", key())
                .withRegister((partContext, part, itemPath1, itemId, itemTag, itemEnglishName) -> NuclearAbsorbable
                        .of(partContext.getMaterialEnglishName() + " Control Rod", itemPath1, maxTemperature,
                                heatConduction * NuclearConstant.BASE_HEAT_CONDUCTION,
                                INeutronBehaviour.of(scatteringType, params, size),
                                NuclearConstant.DESINTEGRATION_BY_ROD,
                                SortOrder.NUCLEAR.create(NuclearOrder.CONTROL_ROD)))
                .withCustomPath("%s_control_rod");
    }

}
