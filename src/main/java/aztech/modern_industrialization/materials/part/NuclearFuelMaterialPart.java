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

import static aztech.modern_industrialization.nuclear.NuclearFuel.NuclearFuelParams;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.nuclear.INeutronBehaviour;
import aztech.modern_industrialization.nuclear.NuclearConstant;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.ColorampDepleted;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class NuclearFuelMaterialPart implements MaterialPart {

    public enum Type {
        DEPLETED(0),
        SIMPLE(1),
        DOUBLE(2),
        QUAD(4);

        public final int size;

        Type(int size) {
            this.size = size;
        }

        public static final Type[] NOT_DEPLETED = new Type[] { SIMPLE, DOUBLE, QUAD };
    }

    private final Type type;
    private final String id;
    private final String partSimple;
    private final String part;
    private final String itemPath;
    private final Coloramp coloramp;
    private final String material;

    private final NuclearFuelParams params;
    private final INeutronBehaviour neutronBehaviour;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(Type type, NuclearFuelParams params, INeutronBehaviour neutronBehaviour) {

        return ctx -> new NuclearFuelMaterialPart(ctx.getMaterialName(), type, ctx.getColoramp(), params, neutronBehaviour);
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> ofDepleted() {

        return ctx -> new NuclearFuelMaterialPart(ctx.getMaterialName(), Type.DEPLETED, ctx.getColoramp(), null, null);
    }

    public NuclearFuelMaterialPart(String material, Type type, Coloramp coloramp, NuclearFuelParams params, INeutronBehaviour neutronBehaviour) {

        this.material = material;

        if (type == Type.DEPLETED) {
            partSimple = MIParts.FUEL_ROD;
            part = MIParts.FUEL_ROD_DEPLETED;
        } else {
            partSimple = (type == Type.SIMPLE ? MIParts.FUEL_ROD : (type == Type.DOUBLE ? MIParts.FUEL_ROD_DOUBLE : MIParts.FUEL_ROD_QUAD));
            part = partSimple;
        }

        itemPath = material + "_" + part;
        id = "modern_industrialization:" + itemPath;

        if (type != Type.DEPLETED) {
            this.coloramp = coloramp;
        } else {
            this.coloramp = new ColorampDepleted(coloramp);
        }

        this.type = type;
        this.params = params;
        this.neutronBehaviour = neutronBehaviour;

    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(int desintegrationMax, NuclearConstant.IsotopeFuelParams params) {

        List<Function<MaterialBuilder.PartContext, MaterialPart>> result = new ArrayList<>();
        result.add(ofDepleted());

        for (Type type : Type.NOT_DEPLETED) {
            NuclearFuelParams fuelParams = new NuclearFuelParams(desintegrationMax * type.size, params.maxTemp, params.neutronsMultiplication,
                    params.directEnergyFactor, type.size);

            INeutronBehaviour neutronBehaviour = INeutronBehaviour.of(NuclearConstant.ScatteringType.HEAVY, params, type.size);

            result.add(of(type, fuelParams, neutronBehaviour));
        }

        return result.toArray(new Function[0]);

    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(NuclearConstant.IsotopeFuelParams params) {
        return of(NuclearConstant.DESINTEGRATION_BY_ROD, params);
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return id;
    }

    @Override
    public String getItemId() {
        return id;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        if (Type.DEPLETED == type) {
            MIItem.of(itemPath, 64);
        } else {
            NuclearFuel.of(itemPath, params, neutronBehaviour, material + "_fuel_rod_depleted");
        }

    }

    @Override
    public void registerTextures(TextureManager textureManager) {
        MITextures.generateItemPartTexture(textureManager, partSimple, "common", itemPath, false, coloramp);
    }
}
