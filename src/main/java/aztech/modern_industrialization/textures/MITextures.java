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

import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.MIRuntimeResourcePack;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.textures.coloramp.BakableTargetColoramp;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.io.IOException;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;

public final class MITextures {

    public static MIRuntimeResourcePack buildResourcePack(ResourceManager manager) {
        MIRuntimeResourcePack pack = new MIRuntimeResourcePack("MI Generated textures");
        TextureManager mtm = new TextureManager(manager, pack);

        for (BakableTargetColoramp coloramp : BakableTargetColoramp.bakableTargetColoramps) {
            coloramp.baked(mtm);
        }

        try {
            for (Material material : MaterialRegistry.getMaterials().values()) {
                for (MaterialPart part : material.getParts().values()) {
                    part.registerTextures(mtm);
                }
            }

            for (CraftingFluid fluid : MIFluids.FLUIDS) {
                fluid.registerTextures(mtm);
            }

            casingFromTexture(mtm, "lv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/lv_machine_hull.png"));
            casingFromTexture(mtm, "mv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/advanced_machine_hull.png"));
            casingFromTexture(mtm, "hv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/turbo_machine_hull.png"));
            casingFromTexture(mtm, "ev", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/nitro_machine_hull.png"));
            casingFromTexture(mtm, "supraconductor", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/ultimate_machine_hull.png"));
            casingFromTexture(mtm, "nuclear", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/nuclear_machine_casing.png"));

            casingFromTexture(mtm, "firebricks", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            casingFromTexture(mtm, "bricks", mtm.getAssetAsTexture("minecraft:textures/block/bricks.png"));

            casingFromTextureBricked(mtm, "bricked_bronze",
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/bronze_machine_casing.png"),
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            casingFromTextureBricked(mtm, "bricked_steel", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/steel_machine_casing.png"),
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            mtm.onEnd();
        } catch (Throwable exception) {
            ModernIndustrialization.LOGGER.error("Failed to generate texture pack.", exception);
        }
        ModernIndustrialization.LOGGER.info("I used the png to destroy the png.");
        return pack;
    }

    private static String getTemplate(String materialSet, String part, String suffix) {
        return String.format("modern_industrialization:textures/materialsets/%s/%s%s.png", materialSet, part, suffix);
    }

    private static final String[] LAYERS = new String[] { "_underlay", "", "_overlay" };

    /**
     * Colorize layer if necessary.
     */
    private static void colorizeLayer(NativeImage image, String layer, Coloramp coloramp) {
        if (layer.equals("")) {
            TextureHelper.colorize(image, coloramp);
        }
    }

    public static void generateItemPartTexture(TextureManager mtm, String materialName, String materialSet, String part, Coloramp coloramp) {
        if (part.equals(MIParts.DOUBLE_INGOT)) {
            mtm.runAtEnd(() -> {
                try {
                    generateDoubleIngot(mtm, materialName);
                } catch (Throwable throwable) {
                    logTextureGenerationError(throwable, materialName, materialSet, part);
                }
            });
        } else {
            try {
                generateBlend(mtm, materialName, materialSet, part, coloramp);
            } catch (Throwable throwable) {
                logTextureGenerationError(throwable, materialName, materialSet, part);
            }
        }
    }

    public static void logTextureGenerationError(Throwable throwable, String materialName, String materialSet, String part) {
        ModernIndustrialization.LOGGER.warn(
                String.format("Failed to generate item part texture for material name %s, material set %s, part %s", materialName, materialSet, part),
                throwable);
    }

    public static void generateBlend(TextureManager mtm, String materialName, String materialSet, String part, Coloramp color) throws IOException {
        NativeImage image = null;
        for (String layer : LAYERS) {
            String template;

            if (part.equals(MIParts.GEM)) {
                template = getTemplate("specials", materialName, layer);
            } else if (part.equals(MIParts.HOT_INGOT)) {
                template = getTemplate("common", "ingot", layer);
            } else {
                template = getTemplate("common", MaterialHelper.partWithOverlay(part), layer);
                if (!mtm.hasAsset(template)) {
                    template = getTemplate(materialSet, MaterialHelper.partWithOverlay(part), layer);
                }
            }

            if (mtm.hasAsset(template)) {
                if (image == null) {
                    image = mtm.getAssetAsTexture(template);
                    colorizeLayer(image, layer, color);
                    if (part.equals(MIParts.HOT_INGOT)) {
                        TextureHelper.increaseBrightness(image, 0.85f);
                    }
                } else {
                    NativeImage topLayer = mtm.getAssetAsTexture(template);
                    colorizeLayer(topLayer, layer, color);
                    TextureHelper.blend(image, topLayer);
                    topLayer.close();
                }
            }
        }

        if (image != null) {

            String overlay = MaterialHelper.overlayWithOverlay(part);

            if (overlay != null) {
                String overlayTemplate = getTemplate("common", overlay, "");
                if (mtm.hasAsset(overlayTemplate)) {
                    NativeImage overlayImage = mtm.getAssetAsTexture(overlayTemplate);
                    TextureHelper.blend(image, overlayImage);
                    overlayImage.close();
                } else {
                    throw new RuntimeException("Could not find the overlay : " + overlay);
                }
            }

            String texturePath;
            if (MaterialHelper.hasBlock(part)) {
                String itemPath = String.format("%s_%s", materialName, part);
                itemPath = MaterialHelper.overrideItemPath(itemPath);

                texturePath = String.format("modern_industrialization:textures/blocks/%s.png", itemPath);

                if (part.equals(MIParts.MACHINE_CASING)) {
                    casingFromTexture(mtm, materialName, image);
                } else if (part.equals(MIParts.MACHINE_CASING_SPECIAL)) {
                    casingFromTexture(mtm, itemPath, image);
                }

            } else {
                if (part.equals(MIParts.GEM)) {
                    String itemPath = materialName;
                    itemPath = MaterialHelper.overrideItemPath(itemPath);
                    texturePath = String.format("modern_industrialization:textures/items/%s.png", itemPath);
                } else {
                    String itemPath = String.format("%s_%s", materialName, part);
                    itemPath = MaterialHelper.overrideItemPath(itemPath);
                    texturePath = String.format("modern_industrialization:textures/items/%s.png", itemPath);
                }
            }

            mtm.addTexture(texturePath, image);
            image.close();
        } else if (!part.equals(MIParts.GEM)) {
            throw new RuntimeException("Could not find any texture!");
        }
    }

    public static void generateDoubleIngot(TextureManager mtm, String materialName) throws IOException {
        String ingotTexture = String.format("modern_industrialization:textures/items/%s_ingot.png", materialName);
        if (materialName.equals("gold") || materialName.equals("iron")) {
            ingotTexture = String.format("minecraft:textures/item/%s_ingot.png", materialName);
        }
        NativeImage image = mtm.getAssetAsTexture(ingotTexture);
        TextureHelper.doubleIngot(image);
        String itemPath = MaterialHelper.overrideItemPath(materialName + "_double_ingot");

        mtm.addTexture(String.format("modern_industrialization:textures/items/%s.png", itemPath), image);
        image.close();
    }

    private static void casingFromTexture(TextureManager tm, String casing, NativeImage texture) {
        for (String side : new String[] { "top", "side", "bottom" }) {

            try {
                String s = String.format("modern_industrialization:textures/blocks/casings/%s/%s.png", casing, side);
                tm.addTexture(s, TextureHelper.copy(texture));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void casingFromTextureBricked(TextureManager tm, String casing, NativeImage texture, NativeImage brick) {
        for (String side : new String[] { "top", "side", "bottom" }) {
            try {

                NativeImage target;

                if (side.equals("top")) {
                    target = TextureHelper.copy(texture);
                } else if (side.equals("bottom")) {
                    target = TextureHelper.copy(brick);
                } else {
                    if (texture.getWidth() != brick.getWidth() || texture.getHeight() != brick.getHeight()) {
                        throw new IllegalArgumentException("Texture and Brick must have same dimension");
                    }

                    NativeImage copy = TextureHelper.copy(texture);
                    for (int i = 0; i < copy.getWidth(); ++i) {
                        for (int j = copy.getHeight() / 2; j < copy.getHeight(); j++) {
                            copy.setPixelColor(i, j, brick.getPixelColor(i, j));
                        }
                    }
                    target = TextureHelper.copy(texture);

                }
                String s = String.format("modern_industrialization:textures/blocks/casings/%s/%s.png", casing, side);
                tm.addTexture(s, target);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
