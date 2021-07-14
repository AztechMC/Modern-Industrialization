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

import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
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

public class RawMetalPart implements MaterialPart {

    private final String materialName;
    private final String itemPath;
    private final String itemId;
    private final String itemTag;
    private final Coloramp coloramp;
    private final MaterialRawSet set;
    private Item item;

    private final boolean isBlock;

    public RawMetalPart(String materialName, Coloramp coloramp, MaterialRawSet set, boolean isBlock) {
        this.isBlock = isBlock;
        this.coloramp = coloramp;
        this.materialName = materialName;
        this.set = set;

        if (!isBlock) {
            this.itemPath = "raw_" + materialName;
            this.itemTag = "#c:raw_" + materialName + "_ores";
        } else {
            this.itemPath = "raw_" + materialName + "_block";
            this.itemTag = "#c:raw_" + materialName + "_blocks";
        }

        this.itemId = "modern_industrialization:" + itemPath;
    }

    @SuppressWarnings("unchecked")
    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(MaterialRawSet set) {
        Function<MaterialBuilder.PartContext, MaterialPart>[] array = new Function[2];
        for (int i = 0; i < 2; i++) {
            final boolean isBlock = i == 0;
            Function<MaterialBuilder.PartContext, MaterialPart> function = ctx -> new RawMetalPart(ctx.getMaterialName(), ctx.getColoramp(), set,
                    isBlock);
            array[i] = function;
        }
        return array;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> ofItemOnly(MaterialRawSet set) {
        return ctx -> new RawMetalPart(ctx.getMaterialName(), ctx.getColoramp(), set, false);
    }

    @Override
    public String getPart() {
        if (isBlock) {
            return MIParts.RAW_METAL_BLOCK;
        } else {
            return MIParts.RAW_METAL;
        }

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
        if (isBlock) {
            MIBlock block = new MIBlock(itemPath,
                    FabricBlockSettings.of(STONE_MATERIAL).hardness(5f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 1).requiresTool());
            item = block.blockItem;
            MaterialHelper.registerItemTag("c:raw_" + materialName + "_blocks", JTag.tag().add(new Identifier(getItemId())));
        } else {
            item = MIItem.of(itemPath);
            MaterialHelper.registerItemTag("c:raw_" + materialName + "_ores", JTag.tag().add(new Identifier(getItemId())));
        }
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        String template = String.format("modern_industrialization:textures/materialsets/raw/%s.png", set.name + (isBlock ? "_block" : ""));
        try {
            NativeImage image = mtm.getAssetAsTexture(template);
            TextureHelper.colorize(image, coloramp);
            String texturePath;
            if (isBlock) {
                texturePath = String.format("modern_industrialization:textures/blocks/%s.png", itemPath);
            } else {
                texturePath = String.format("modern_industrialization:textures/items/%s.png", itemPath);
            }
            mtm.addTexture(texturePath, image);
            image.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
