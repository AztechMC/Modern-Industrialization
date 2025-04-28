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
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.property.MaterialProperty;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.IsotopeFuelParams;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.nuclear.NuclearFuel.NuclearFuelParams;
import aztech.modern_industrialization.nuclear.NuclearOrder;
import java.util.List;

public class NuclearFuelPart implements PartKeyProvider {

    public enum Type {
        SIMPLE(1, "fuel_rod"),
        DOUBLE(2, "fuel_rod_double"),
        QUAD(4, "fuel_rod_quad"),
        DEPLETED(0, "fuel_rod_depleted");

        public final int size;
        public final String key;

        Type(int size, String key) {
            this.size = size;
            this.key = key;
        }
    }

    public final Type type;
    public final PartKey key;

    public NuclearFuelPart(Type type) {
        this.key = new PartKey(type.key);
        this.type = type;
    }

    @Override
    public PartKey key() {
        return key;
    }

    private PartTemplate of() {
        String englishNameFormatter = switch (type) {
        case SIMPLE -> "Fuel Rod";
        case DOUBLE -> "Double Fuel Rod";
        case QUAD -> "Quad Fuel Rod";
        case DEPLETED -> "Depleted %s Fuel Rod";
        };

        var out = new PartTemplate(englishNameFormatter, key)
                .withRegister((partContext, part, itemPath, itemId, itemTag, englishName) -> {
                    var sortOrder = SortOrder.NUCLEAR.create(NuclearOrder.FUEL_ROD).and(partContext.getMaterialName()).and(type);
                    if (Type.DEPLETED == type) {
                        MIItem.item(englishName, itemPath, sortOrder);
                    } else {
                        IsotopeFuelParams params = partContext.get(MaterialProperty.ISOTOPE);
                        if (params == null) {
                            throw new IllegalArgumentException("Material %s must be a fuel isotope".formatted(partContext.getMaterialName()));
                        }

                        NuclearFuelParams fuelParams = new NuclearFuelParams(NuclearConstant.DESINTEGRATION_BY_ROD * type.size, params.maxTemp,
                                params.tempLimitLow,
                                params.tempLimitHigh, params.neutronsMultiplication, params.directEnergyFactor, type.size);

                        INeutronBehaviour neutronBehaviour = INeutronBehaviour.of(NuclearConstant.ScatteringType.HEAVY, params, type.size);

                        NuclearFuel.of(englishName, itemPath, fuelParams,
                                neutronBehaviour, partContext.getMaterialName() + "_fuel_rod_depleted", sortOrder);
                    }
                });
        if (type == Type.DEPLETED) {
            out = out.withTexture(new TextureGenParams.DepletedNuclear());
        }
        return out;
    }

    public static List<PartTemplate> ofAll() {
        return List.of(
                MIParts.FUEL_ROD.of(),
                MIParts.FUEL_ROD_DOUBLE.of(),
                MIParts.FUEL_ROD_QUAD.of(),
                MIParts.FUEL_ROD_DEPLETED.of());
    }
}
