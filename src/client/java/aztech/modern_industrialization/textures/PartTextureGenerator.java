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
package aztech.modern_industrialization.textures;

import static aztech.modern_industrialization.materials.property.MaterialProperty.SET;

import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.part.*;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import aztech.modern_industrialization.textures.coloramp.DepletedColoramp;
import aztech.modern_industrialization.textures.coloramp.HotIngotColoramp;
import aztech.modern_industrialization.textures.coloramp.IColoramp;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/**
 * All the per-part texture processing logic.
 */
class PartTextureGenerator {
    static void processPart(IColoramp coloramp, TextureManager mtm, Material material, MaterialItemPart part) {
        var gen = new PartTextureGenerator(coloramp, mtm, material, part);

        try {
            gen.build(part);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final IColoramp coloramp;
    private final TextureManager mtm;
    private final Material material;
    private final String materialName;
    private final String itemPath;

    private PartTextureGenerator(IColoramp coloramp, TextureManager mtm, Material material, MaterialItemPart part) {
        this.coloramp = coloramp;
        this.mtm = mtm;
        this.material = material;
        this.materialName = material.name;
        this.itemPath = new ResourceLocation(part.getItemId()).getPath();
    }

    private void build(MaterialItemPart part) throws IOException {
        var params = part.getTextureGenParams();

        if (params instanceof TextureGenParams.Block block) {
            processBlock(block.blockSet());
        } else if (params instanceof TextureGenParams.CasingBlock) {
            processCasing(part);
        } else if (params instanceof TextureGenParams.ColumnBlock) {
            processColumn(part);
        } else if (params instanceof TextureGenParams.DepletedNuclear) {
            processDepletedNuclear();
        } else if (params instanceof TextureGenParams.DoubleIngot) {
            processDoubleIngot(part);
        } else if (params instanceof TextureGenParams.Gem) {
            processGem();
        } else if (params instanceof TextureGenParams.HotIngot) {
            processHotIngot();
        } else if (params instanceof TextureGenParams.Ore ore) {
            processOre(ore.deepslate(), ore.oreSet());
        } else if (params instanceof TextureGenParams.RawMetal rawMetal) {
            processRawMetal(rawMetal.isBlock(), rawMetal.rawSet());
        } else if (params instanceof TextureGenParams.SimpleRecoloredBlock) {
            MITextures.generateItemPartTexture(mtm, part.key().key, material.get(SET).name, itemPath, true, coloramp);
        } else if (params instanceof TextureGenParams.SimpleRecoloredItem item) {
            String partTemplate = Objects.requireNonNullElse(item.basePart(), part).key().key;
            MITextures.generateItemPartTexture(mtm, partTemplate, item.overlay(), material.get(SET).name, itemPath, false, coloramp);
        } else if (!(params instanceof TextureGenParams.NoTexture)) {
            throw new IllegalArgumentException("Unknown texture gen params class " + params.getClass().getSimpleName());
        }
    }

    private void processBlock(MaterialBlockSet set) throws IOException {
        String template = String.format("modern_industrialization:textures/materialsets/blocks/%s.png", set.name);
        try (NativeImage image = mtm.getAssetAsTexture(template)) {
            TextureHelper.colorize(image, coloramp);
            String texturePath = String.format("modern_industrialization:textures/block/%s.png", itemPath);
            mtm.addTexture(texturePath, image);
        }
    }

    private void processCasing(PartKeyProvider part) throws IOException {
        try (NativeImage image = MITextures.generateTexture(mtm, part.key().key, material.get(SET).name, coloramp)) {
            MITextures.appendTexture(mtm, image, itemPath, true);
        }
    }

    private void processColumn(PartKeyProvider part) {
        for (String suffix : new String[] { "side", "top" }) {
            MITextures.generateItemPartTexture(
                    mtm,
                    part.key().key + "_" + suffix,
                    material.get(SET).name,
                    itemPath + "_" + suffix,
                    true,
                    coloramp);
        }
    }

    private void processDepletedNuclear() {
        MITextures.generateItemPartTexture(mtm,
                NuclearFuelPart.Type.SIMPLE.key, "common", itemPath, false,
                new DepletedColoramp(coloramp));
    }

    private void processDoubleIngot(PartKeyProvider part) {
        mtm.runAtEnd(() -> {
            try {
                MITextures.generateDoubleIngot(mtm, materialName);
            } catch (Throwable throwable) {
                MITextures.logTextureGenerationError(throwable, materialName, material.get(SET).name, part.key().key);
            }
        });
    }

    private void processGem() throws IOException {
        String template = String.format("modern_industrialization:textures/materialsets/gems/%s.png", materialName);
        try (NativeImage image = mtm.getAssetAsTexture(template)) {
            TextureHelper.colorize(image, coloramp);
            String texturePath = String.format("modern_industrialization:textures/item/%s.png", itemPath);
            mtm.addTexture(texturePath, image);
        }
    }

    private void processHotIngot() {
        MITextures.generateItemPartTexture(mtm, MIParts.INGOT.key().key,
                material.get(SET).name, itemPath, false, new HotIngotColoramp(coloramp, 0.1, 0.5));
    }

    private void processOre(boolean deepslate, MaterialOreSet oreSet) throws IOException {
        String template = String.format("modern_industrialization:textures/materialsets/ores/%s.png", oreSet.name);
        String from = switch (oreSet) {
        case IRON -> deepslate ? "deepslate_iron_ore" : "iron_ore";
        case COPPER -> deepslate ? "deepslate_copper_ore" : "copper_ore";
        case LAPIS -> deepslate ? "deepslate_lapis_ore" : "lapis_ore";
        case REDSTONE -> deepslate ? "deepslate" : "redstone_ore";
        case DIAMOND -> deepslate ? "deepslate" : "diamond_ore";
        case GOLD -> deepslate ? "deepslate_gold_ore" : "gold_ore";
        case EMERALD -> deepslate ? "deepslate_emerald_ore" : "emerald_ore";
        case COAL -> deepslate ? "deepslate_coal_ore" : "coal_ore";
        default -> deepslate ? "deepslate" : "stone";
        };

        try (NativeImage image = mtm.getAssetAsTexture(String.format("minecraft:textures/block/%s.png", from));
                NativeImage top = mtm.getAssetAsTexture(template)) {

            TextureHelper.colorize(top, coloramp);
            String texturePath = String.format("modern_industrialization:textures/block/%s.png", itemPath);
            mtm.addTexture(texturePath, TextureHelper.blend(image, top), true);
        }
    }

    private void processRawMetal(boolean isBlock, MaterialRawSet rawSet) throws IOException {
        String template = String.format("modern_industrialization:textures/materialsets/raw/%s.png", rawSet.name + (isBlock ? "_block" : ""));
        try (NativeImage image = mtm.getAssetAsTexture(template)) {
            TextureHelper.colorize(image, coloramp);
            String texturePath;
            if (isBlock) {
                texturePath = String.format("modern_industrialization:textures/block/%s.png", itemPath);
            } else {
                texturePath = String.format("modern_industrialization:textures/item/%s.png", itemPath);
            }
            mtm.addTexture(texturePath, image);
        }
    }
}
