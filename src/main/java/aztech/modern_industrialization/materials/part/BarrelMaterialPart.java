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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.io.IOException;
import java.util.function.Function;
import net.minecraft.client.texture.NativeImage;

public class BarrelMaterialPart implements MaterialPart {

    private final String materialName;
    private final Coloramp coloramp;
    private final int stackCapacity;
    private final String idPath;
    private final String itemId;
    private BarrelBlock block;

    public BarrelMaterialPart(String materialName, Coloramp coloramp, int stackCapacity) {
        this.materialName = materialName;
        this.coloramp = coloramp;
        this.stackCapacity = stackCapacity;
        this.idPath = materialName + "_" + getPart();
        this.itemId = ModernIndustrialization.MOD_ID + ":" + idPath;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(int stackCapacity) {
        return ctx -> new BarrelMaterialPart(ctx.getMaterialName(), ctx.getColoramp(), stackCapacity);
    }

    @Override
    public String getPart() {
        return MIParts.BARREL;
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
        block = new BarrelBlock(idPath);
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        for (String suffix : new String[] { "_end", "_side" }) {
            String template = String.format("modern_industrialization:textures/materialsets/common/barrel%s.png", suffix);
            try {
                NativeImage image = mtm.getAssetAsTexture(template);
                TextureHelper.colorize(image, coloramp);
                String texturePath;
                texturePath = String.format("modern_industrialization:textures/blocks/%s%s.png", idPath, suffix);
                mtm.addTexture(texturePath, image);
                image.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
