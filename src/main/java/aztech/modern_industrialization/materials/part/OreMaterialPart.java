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

import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.materials.MaterialOreSet;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.util.function.Function;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.util.Identifier;

public class OreMaterialPart extends RegularMaterialPart {
    private final MaterialOreSet oreSet;

    protected OreMaterialPart(String materialName, String part, String materialSet, Coloramp coloramp, MaterialOreSet oreSet) {
        super(materialName, part, materialSet, coloramp);
        this.oreSet = oreSet;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(MaterialOreSet oreSet) {
        return ctx -> new OreMaterialPart(ctx.getMaterialName(), MIParts.ORE, ctx.getMaterialSet(), ctx.getColoramp(), oreSet);
    }

    @Override
    public void register() {
        block = new OreBlock(MaterialHelper.overrideItemPath(itemPath),
                FabricBlockSettings.of(STONE_MATERIAL).hardness(3.0f).resistance(3.0f).breakByTool(FabricToolTags.PICKAXES, 1).requiresTool());
        item = block.blockItem;

        MaterialHelper.registerItemTag(MaterialHelper.getPartTag(materialName, part), JTag.tag().add(new Identifier(getItemId())));
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
        MITextures.generateOreTexture(textureManager, itemPath, coloramp, oreSet);
    }

}
