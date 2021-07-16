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
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.HotIngotColoramp;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

/**
 * A regular material item part, for example bronze curved plates.
 */
public class RegularMaterialPart implements MaterialPart {

    private static final Map<String, Pair<String, String>> overlays = new HashMap<>();

    static {
        overlays.put(MIParts.WIRE_MAGNETIC, new Pair<>("magnetic", "wire"));
        overlays.put(MIParts.ROD_MAGNETIC, new Pair<>("magnetic", "rod"));
        overlays.put(MIParts.P_DOPED_PLATE, new Pair<>("p_doped", "plate"));
        overlays.put(MIParts.N_DOPED_PLATE, new Pair<>("n_doped", "plate"));
    }

    protected final String materialName;
    protected final String part;
    protected final String itemPath;
    protected final String itemId;
    protected final String itemTag;
    protected final String materialSet;
    protected final Coloramp coloramp;
    private final boolean hasBlock;
    protected MIBlock block;
    protected Item item;

    public RegularMaterialPart(String materialName, String part, String materialSet, Coloramp coloramp) {
        this.materialName = materialName;
        this.part = part;
        hasBlock = MaterialHelper.hasBlock(part);
        itemPath = materialName + "_" + part;
        itemId = "modern_industrialization:" + itemPath;

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
    public void register(MaterialBuilder.RegisteringContext context) {
        // create item and block
        if (hasBlock) {
            block = new MIBlock(itemPath,
                    FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(6.0f).breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());
            item = block.blockItem;
        } else {
            block = null;
            item = MIItem.of(itemPath);
        }
        // item tag
        // items whose path are overridden (such as fire clay ingot -> brick) are not
        // added to the tags
        if (MIParts.TAGGED_PARTS.contains(part)) {
            MaterialHelper.registerItemTag(MaterialHelper.getPartTag(materialName, part), JTag.tag().add(new Identifier(getItemId())));
        }
    }

    @Override
    public void registerTextures(TextureManager mtm) {
        if (part.equals(MIParts.DOUBLE_INGOT)) {
            mtm.runAtEnd(() -> {
                try {
                    MITextures.generateDoubleIngot(mtm, materialName);
                } catch (Throwable throwable) {
                    MITextures.logTextureGenerationError(throwable, materialName, materialSet, part);
                }
            });
        } else if (part.equals(MIParts.HOT_INGOT)) {
            MITextures.generateItemPartTexture(mtm, MIParts.INGOT, materialSet, itemPath, hasBlock, new HotIngotColoramp(coloramp, 0.1, 0.5));
        } else if (overlays.containsKey(part)) {
            Pair<String, String> overlay_part = overlays.get(part);
            MITextures.generateItemPartTexture(mtm, overlay_part.getRight(), overlay_part.getLeft(), materialSet, itemPath, hasBlock, coloramp);
        } else {
            MITextures.generateItemPartTexture(mtm, part, materialSet, itemPath, hasBlock, coloramp);

            if (part.equals(MIParts.DRILL)) {
                String template = "modern_industrialization:textures/materialsets/common/drill.png";
                String templateOverlay = "modern_industrialization:textures/materialsets/common/mining_drill_overlay.png";
                String texturePath = String.format("modern_industrialization:textures/items/%s.png", materialName + "_mining_drill");
                try {
                    NativeImage image = mtm.getAssetAsTexture(template);
                    NativeImage overlay = mtm.getAssetAsTexture(templateOverlay);
                    TextureHelper.colorize(image, coloramp);
                    TextureHelper.blend(image, overlay);
                    mtm.addTexture(texturePath, image);
                    image.close();
                    overlay.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
