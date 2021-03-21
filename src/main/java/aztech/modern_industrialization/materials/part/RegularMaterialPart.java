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
import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.util.Objects;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

/**
 * A regular material item part, for example bronze curved plates.
 */
public class RegularMaterialPart implements MaterialPart {
    protected final String materialName;
    private final String part;
    private final String itemPath;
    private final String itemId;
    private final String itemTag;
    private final String materialSet;
    private final Coloramp coloramp;
    protected MIBlock block;
    private Item item;

    public RegularMaterialPart(String materialName, String part, String materialSet, Coloramp coloramp) {
        this.materialName = materialName;
        this.part = part;

        String path = materialName;

        if (!part.equals(MIParts.GEM)) {
            path += "_" + part;
        }

        itemPath = path;

        this.itemId = "modern_industrialization:" + itemPath;
        if (MIParts.TAGGED_PARTS.contains(part)) {
            this.itemTag = "#c:" + materialName + "_" + part + "s";
        } else {
            this.itemTag = itemId;
        }

        this.materialSet = materialSet;
        this.coloramp = coloramp;

    }

    @Override
    public String getPart() {
        return part;
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
    @SuppressWarnings("deprecation")
    public void register() {
        // create item and block
        if (MaterialHelper.hasBlock(part)) {
            if (MaterialHelper.isOre(part)) {
                block = new OreBlock(MaterialHelper.overrideItemPath(itemPath), FabricBlockSettings.of(STONE_MATERIAL).hardness(3.0f).resistance(3.0f)
                        .breakByTool(FabricToolTags.PICKAXES, 1).requiresTool());
            } else {
                block = new MIBlock(MaterialHelper.overrideItemPath(itemPath), FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(6.0f)
                        .breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());
            }
            item = block.blockItem;
        } else {
            block = null;
            item = MIItem.of(MaterialHelper.overrideItemPath(itemPath));
        }
        // item tag
        if (MIParts.TAGGED_PARTS.contains(part)) {
            MaterialHelper.registerItemTag(MaterialHelper.getPartTag(materialName, part), JTag.tag().add(new Identifier(getItemId())));
        }
    }

    @Override
    public Item getItem() {
        return Objects.requireNonNull(item);
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
        MITextures.generateItemPartTexture(textureManager, materialName, materialSet, part, coloramp);
    }
}
