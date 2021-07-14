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
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.io.IOException;
import java.util.function.Function;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

public class BlockMaterialPart implements MaterialPart {

    protected final MaterialBlockSet set;
    protected final String materialName;
    protected final String itemPath;
    protected final String itemId;
    protected final String itemTag;
    protected final Coloramp coloramp;
    protected MIBlock block;
    protected Item item;

    protected BlockMaterialPart(String materialName, Coloramp coloramp, MaterialBlockSet blockSet) {
        this.materialName = materialName;
        this.coloramp = coloramp;
        this.itemPath = materialName + "_block";
        this.itemId = "modern_industrialization:" + itemPath;
        this.itemTag = "#c:" + materialName + "_blocks";
        this.set = blockSet;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(MaterialBlockSet blockSet) {
        return ctx -> new BlockMaterialPart(ctx.getMaterialName(), ctx.getColoramp(), blockSet);
    }

    @Override
    public String getPart() {
        return MIParts.BLOCK;
    }

    @Override
    public String getTaggedItemId() {
        return itemTag;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        block = new MIBlock(itemPath,
                FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());
        item = block.blockItem;

        MaterialHelper.registerItemTag("c:" + materialName + "_blocks", JTag.tag().add(new Identifier(getItemId())));
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        String template = String.format("modern_industrialization:textures/materialsets/blocks/%s.png", set.name);
        try {
            NativeImage image = mtm.getAssetAsTexture(template);
            TextureHelper.colorize(image, coloramp);
            String texturePath = String.format("modern_industrialization:textures/blocks/%s.png", itemPath);
            mtm.addTexture(texturePath, image);
            image.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
