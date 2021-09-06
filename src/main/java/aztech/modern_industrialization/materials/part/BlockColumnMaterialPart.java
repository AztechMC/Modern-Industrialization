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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.function.Function;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;

public class BlockColumnMaterialPart implements MaterialPart {

    protected final Coloramp coloramp;
    protected final String idPath;
    protected final String itemId;
    protected MIBlock block;
    protected String part;

    public BlockColumnMaterialPart(String part, String materialName, Coloramp coloramp) {
        this.part = part;
        Preconditions.checkArgument(MaterialHelper.hasBlock(part), String.format("Part %s is not registered as Block Part", part));
        this.coloramp = coloramp;
        this.idPath = materialName + "_" + getPart();
        this.itemId = ModernIndustrialization.MOD_ID + ":" + idPath;

    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part) {
        return ctx -> new BlockColumnMaterialPart(part, ctx.getMaterialName(), ctx.getColoramp());
    }

    @Override
    public String getPart() {
        return part;
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

        block = new MIBlock(idPath,
                FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());
        block.setBlockModel(JModel.model().parent("block/cube_column")
                .textures(new JTextures().var("end", ModernIndustrialization.MOD_ID + ":blocks/" + idPath + "_end").var("side",
                        ModernIndustrialization.MOD_ID + ":blocks/" + idPath + "_side")));
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        for (String suffix : new String[] { "_end", "_side" }) {
            String template = String.format("modern_industrialization:textures/materialsets/common/%s%s.png", part, suffix);
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
