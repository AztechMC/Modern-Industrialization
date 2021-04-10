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
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.nuclear.NuclearFuel;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.ColorampDepleted;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.item.Item;

public class NuclearFuelMaterialPart implements MaterialPart {

    private static final int SIMPLE = 1, DOUBLE = 2, QUAD = 4;
    private final String id, partSimple, part, itemPath, materialName;
    private Item item;
    private final Coloramp coloramp;
    private boolean depleted;

    private final NuclearFuelParams params;

    public class NuclearFuelParams {
        public final int maxTemperature;
        public final double neutronAmpl;
        public final double neutronAbs;
        public final double heatByDesintegration;
        public final int desintegrationMax;

        public NuclearFuelParams(int maxTemperature, double neutronAmpl, double neutronAbs, double heatByDesintegration, int desintegrationMax) {
            this.maxTemperature = maxTemperature;
            this.neutronAmpl = neutronAmpl;
            this.neutronAbs = neutronAbs;
            this.heatByDesintegration = heatByDesintegration;
            this.desintegrationMax = desintegrationMax;
        }
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(int quantity, boolean depleted, NuclearFuelParams params) {
        return ctx -> new NuclearFuelMaterialPart(ctx.getMaterialName(), quantity, depleted, ctx.getColoramp(), params);
    }

    public NuclearFuelMaterialPart(String material, int quantity, boolean depleted, Coloramp coloramp, NuclearFuelParams params) {
        partSimple = "fuel_rod" + ((quantity == SIMPLE ? "" : (quantity == DOUBLE ? "_double" : "_quad")));
        part = partSimple + (depleted ? "_depleted" : "");
        itemPath = material + "_" + part;
        id = "modern_industrialization:" + itemPath;
        if (!depleted) {
            this.coloramp = coloramp;
        } else {
            this.coloramp = new ColorampDepleted(coloramp);
        }
        this.materialName = material;
        this.depleted = depleted;
        this.params = params;

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
    public void register() {
        if (depleted) {
            item = MIItem.of(MaterialHelper.overrideItemPath(itemPath), 1);
        } else {
            item = NuclearFuel.of(MaterialHelper.overrideItemPath(itemPath), params.maxTemperature, params.neutronAmpl, params.neutronAbs,
                    params.heatByDesintegration, params.desintegrationMax, partSimple + "_depleted");
        }

    }

    @Override
    public Item getItem() {
        return Objects.requireNonNull(item);
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
        if (!depleted) {
            MITextures.generateItemPartTexture(textureManager, materialName, "common", part, coloramp);
        } else {
            MITextures.generateItemPartTexture(textureManager, materialName, "common", part, partSimple, coloramp);
        }
    }
}
