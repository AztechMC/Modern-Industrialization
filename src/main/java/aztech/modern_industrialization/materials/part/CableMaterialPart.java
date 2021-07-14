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

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.pipes.MIPipes;
import java.util.function.Function;

public class CableMaterialPart implements MaterialPart {
    protected final String materialName;
    private final CableTier tier;
    private final String itemId;
    protected final int color;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(CableTier tier) {
        return ctx -> new CableMaterialPart(ctx.getMaterialName(), tier, ctx.getColoramp().getMeanRGB());
    }

    protected CableMaterialPart(String material, CableTier tier, int color) {
        this.materialName = material;
        this.tier = tier;
        this.itemId = "modern_industrialization:" + material + "_cable";
        this.color = color;
    }

    @Override
    public String getPart() {
        return MIParts.CABLE;
    }

    @Override
    public String getTaggedItemId() {
        return itemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        MIPipes.INSTANCE.registerCableType(materialName, color | 0xff000000, tier);
    }

}
