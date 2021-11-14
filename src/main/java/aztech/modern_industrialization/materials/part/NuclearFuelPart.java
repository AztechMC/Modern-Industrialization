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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.nuclear.NuclearFuel.NuclearFuelParams;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.coloramp.ColorampDepleted;
import java.util.List;

public class NuclearFuelPart extends UnbuildablePart<NuclearConstant.IsotopeFuelParams> {

    public enum Type {
        DEPLETED(0, "fuel_rod_depleted"),
        SIMPLE(1, "fuel_rod"),
        DOUBLE(2, "fuel_rod_double"),
        QUAD(4, "fuel_rod_quad");

        public final int size;
        public final String key;

        Type(int size, String key) {
            this.size = size;
            this.key = key;
        }
    }

    public final Type type;

    public NuclearFuelPart(Type type) {
        super(type.key);
        this.type = type;
    }

    @Override
    public RegularPart of(NuclearConstant.IsotopeFuelParams params) {

        NuclearFuelParams fuelParams = new NuclearFuelParams(NuclearConstant.DESINTEGRATION_BY_ROD * type.size, params.maxTemp, params.tempLimitLow,
                params.tempLimitHigh, params.neutronsMultiplication, params.directEnergyFactor, type.size);

        INeutronBehaviour neutronBehaviour = INeutronBehaviour.of(NuclearConstant.ScatteringType.HEAVY, params, type.size);

        return new RegularPart(key).withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            if (Type.DEPLETED == type) {
                MIItem.of(itemPath, 64);
            } else {
                NuclearFuel.of(itemPath, fuelParams, neutronBehaviour, partContext.getMaterialName() + "_fuel_rod_depleted");
            }
        }).withTextureRegister((mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm,
                type == Type.DEPLETED ? Type.SIMPLE.key : type.key, "common", itemPath, false,
                type == Type.DEPLETED ? new ColorampDepleted(partContext.getColoramp()) : partContext.getColoramp()));
    }

    public List<BuildablePart> ofAll(NuclearConstant.IsotopeFuelParams params) {
        return List.of(MIParts.FUEL_ROD.of(params), MIParts.FUEL_ROD_DOUBLE.of(params), MIParts.FUEL_ROD_QUAD.of(params),
                MIParts.FUEL_ROD_DEPLETED.of(params));
    }
}
