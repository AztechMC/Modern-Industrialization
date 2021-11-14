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
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.textures.coloramp.BakableTargetColoramp;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.DefaultColoramp;
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
                registerFluidTextures(mtm, fluid);
            }

            casingFromTexture(mtm, "lv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/basic_machine_hull.png"));
            casingFromTexture(mtm, "mv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/advanced_machine_hull.png"));
            casingFromTexture(mtm, "hv", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/turbo_machine_hull.png"));
            casingFromTexture(mtm, "ev", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/highly_advanced_machine_hull.png"));
            casingFromTexture(mtm, "supraconductor", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/quantum_machine_hull.png"));
            casingFromTexture(mtm, "nuclear", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/nuclear_machine_casing.png"));

            casingFromTexture(mtm, "firebricks", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            casingFromTexture(mtm, "bricks", mtm.getAssetAsTexture("minecraft:textures/block/bricks.png"));

            casingFromTextureBricked(mtm, "bricked_bronze",
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/bronze_machine_casing.png"),
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            casingFromTextureBricked(mtm, "bricked_steel", mtm.getAssetAsTexture("modern_industrialization:textures/blocks/steel_machine_casing.png"),
                    mtm.getAssetAsTexture("modern_industrialization:textures/blocks/fire_clay_bricks.png"));

            mtm.addTexture("modern_industrialization:textures/items/mixed_ingot_blastproof.png",
                    TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/items/stainless_steel_ingot.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/titanium_ingot.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/tungsten_ingot.png")));

            mtm.addTexture("modern_industrialization:textures/items/mixed_plate_nuclear.png",
                    TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/items/cadmium_plate.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/beryllium_plate.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/blastproof_alloy_plate.png"), 1, 2));

            mtm.addTexture("modern_industrialization:textures/items/mixed_ingot_iridium.png",
                    TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/items/blastproof_alloy_ingot.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/iridium_ingot.png"),
                            mtm.getAssetAsTexture("modern_industrialization:textures/items/blastproof_alloy_ingot.png")));

            NativeImage copperDrill = mtm.getAssetAsTexture("modern_industrialization:textures/items/copper_mining_drill.png");
            NativeImage steamHandler = mtm.getAssetAsTexture("modern_industrialization:textures/items/steam_mining_drill_handler.png");
            mtm.addTexture("modern_industrialization:textures/items/steam_mining_drill.png", TextureHelper.blend(steamHandler, copperDrill), true);
            copperDrill.close();
            steamHandler.close();

            NativeImage aluminumDrill = mtm.getAssetAsTexture("modern_industrialization:textures/items/aluminum_mining_drill.png");
            NativeImage dieselHandler = mtm.getAssetAsTexture("modern_industrialization:textures/items/diesel_mining_drill_handler.png");
            mtm.addTexture("modern_industrialization:textures/items/diesel_mining_drill.png", TextureHelper.blend(dieselHandler, aluminumDrill),
                    true);
            aluminumDrill.close();
            dieselHandler.close();

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

    public static void generateItemPartTexture(TextureManager mtm, String partTemplate, String overlay, String materialSet, String path,
            boolean isBlock, Coloramp coloramp) {
        try {
            NativeImage texture = generateTexture(mtm, partTemplate, materialSet, coloramp);

            if (overlay != null) {
                String overlayTemplate = getTemplate("common", overlay, "");
                NativeImage overlayTexture = mtm.getAssetAsTexture(overlayTemplate);
                NativeImage oldTexture = texture;
                texture = TextureHelper.blend(oldTexture, overlayTexture);
                oldTexture.close();
                overlayTexture.close();
            }

            appendTexture(mtm, texture, path, isBlock);
            texture.close();
        } catch (Throwable throwable) {
            logTextureGenerationError(throwable, path, materialSet, partTemplate);
        }
    }

    public static void generateItemPartTexture(TextureManager mtm, String partTemplate, String materialSet, String path, boolean isBlock,
            Coloramp coloramp) {
        generateItemPartTexture(mtm, partTemplate, null, materialSet, path, isBlock, coloramp);
    }

    public static void logTextureGenerationError(Throwable throwable, String path, String materialSet, String part) {
        ModernIndustrialization.LOGGER.warn(
                String.format("Failed to generate item part texture for path %s, material set %s, partTemplate %s", path, materialSet, part),
                throwable);
    }

    public static NativeImage generateTexture(TextureManager mtm, String partTemplate, String materialSet, Coloramp coloramp) throws IOException {
        NativeImage image = null;
        for (String layer : LAYERS) {
            String template;
            template = getTemplate(materialSet, partTemplate, layer);
            if (!mtm.hasAsset(template)) {
                template = getTemplate("common", partTemplate, layer);
            }

            if (mtm.hasAsset(template)) {
                if (image == null) {
                    image = mtm.getAssetAsTexture(template);
                    colorizeLayer(image, layer, coloramp);
                } else {
                    NativeImage topLayer = mtm.getAssetAsTexture(template);
                    colorizeLayer(topLayer, layer, coloramp);
                    NativeImage oldImage = image;
                    image = TextureHelper.blend(oldImage, topLayer);
                    oldImage.close();
                    topLayer.close();
                }
            }
        }
        return image;
    }

    public static void appendTexture(TextureManager mtm, NativeImage texture, String path, boolean isBlock) throws IOException {

        if (texture != null) {
            String texturePath;
            if (isBlock) {
                texturePath = String.format("modern_industrialization:textures/blocks/%s.png", path);
            } else {
                texturePath = String.format("modern_industrialization:textures/items/%s.png", path);
            }
            mtm.addTexture(texturePath, texture);
            texture.close();
        } else {
            throw new RuntimeException("Could not find any texture!");
        }
    }

    public static void generateDoubleIngot(TextureManager mtm, String materialName) throws IOException {
        String ingotTexture = String.format("modern_industrialization:textures/items/%s_ingot.png", materialName);
        if (materialName.equals("gold") || materialName.equals("iron") || materialName.equals("copper")) {
            ingotTexture = String.format("minecraft:textures/item/%s_ingot.png", materialName);
        }
        NativeImage image = mtm.getAssetAsTexture(ingotTexture);
        TextureHelper.doubleIngot(image);
        String itemPath = materialName + "_double_ingot";

        mtm.addTexture(String.format("modern_industrialization:textures/items/%s.png", itemPath), image);
        image.close();
    }

    public static void casingFromTexture(TextureManager tm, String casing, NativeImage texture) {
        for (String side : new String[] { "top", "side", "bottom" }) {

            try {
                String s = String.format("modern_industrialization:textures/blocks/casings/%s/%s.png", casing, side);
                tm.addTexture(s, TextureHelper.copy(texture));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void tankFromTexture(TextureManager tm, String tank, NativeImage texture) {
        try {
            String s = String.format("modern_industrialization:textures/blocks/tanks/%s.png", tank);
            NativeImage creativeTankTexture = tm.getAssetAsTexture("modern_industrialization:textures/blocks/tanks/creative.png");
            NativeImage tankTexture = TextureHelper.copy(texture);
            if (tankTexture.getHeight() != creativeTankTexture.getHeight() || tankTexture.getWidth() != creativeTankTexture.getWidth()) {
                throw new RuntimeException("Casing and creative tank texture not the same size !");
            }
            for (int i = 1; i < tankTexture.getWidth() - 1; i++) {
                for (int j = 1; j < tankTexture.getHeight() - 1; j++) {
                    tankTexture.setColor(i, j, creativeTankTexture.getColor(i, j));
                }
            }
            tm.addTexture(s, tankTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void casingFromTextureBricked(TextureManager tm, String casing, NativeImage texture, NativeImage brick) {
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
                            copy.setColor(i, j, brick.getColor(i, j));
                        }
                    }
                    target = copy;
                }
                String s = String.format("modern_industrialization:textures/blocks/casings/%s/%s.png", casing, side);
                tm.addTexture(s, target);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void registerFluidTextures(TextureManager tm, CraftingFluid fluid) {
        String path = "modern_industrialization:textures/fluid/";
        String bucket = path + "bucket.png";
        String bucket_content = path + "bucket_content.png";

        try {
            NativeImage bucket_image = tm.getAssetAsTexture(bucket);
            NativeImage bucket_content_image = tm.getAssetAsTexture(bucket_content);
            TextureHelper.colorize(bucket_content_image, new DefaultColoramp(fluid.color));
            NativeImage oldBucketImage = bucket_image;
            bucket_image = TextureHelper.blend(oldBucketImage, bucket_content_image);
            oldBucketImage.close();
            if (fluid.isGas) {
                TextureHelper.flip(bucket_image);
            }
            tm.addTexture(String.format("modern_industrialization:textures/items/bucket/%s.png", fluid.name), bucket_image);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
