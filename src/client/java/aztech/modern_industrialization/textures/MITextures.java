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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIFluids;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.materials.Material;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MaterialItemPart;
import aztech.modern_industrialization.materials.property.MaterialProperty;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.textures.coloramp.IColoramp;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public final class MITextures {

    public static CompletableFuture<?> offerTextures(BiConsumer<NativeImage, String> textureWriter, BiConsumer<JsonElement, String> mcMetaWriter,
            ResourceProvider manager, ExistingFileHelper fileHelper) {
        TextureManager mtm = new TextureManager(manager, textureWriter, mcMetaWriter);

        // Texture generation runs in two phases:
        // 1. all textures that don't depend on other generated textures are submitted to {@code defer.accept} and generated in parallel.
        // 2. textures that depend on other generated textures are submitted to {@code mtm.runAtEnd} are generated in parallel once phase 1. is
        // complete.

        // Futures for the first work phase
        List<CompletableFuture<?>> futures = new ArrayList<>();
        Consumer<IORunnable> defer = r -> futures.add(CompletableFuture.runAsync(r::safeRun, Util.backgroundExecutor()));

        for (Material material : MaterialRegistry.getMaterials().values()) {
            var meanRgb = material.get(MaterialProperty.MEAN_RGB);

            if (meanRgb == 0) {
                MI.LOGGER.error("Missing mean RGB for material {}", material.name);
                continue;
            }

            IColoramp coloramp = new Coloramp(mtm, meanRgb, material.name);

            for (MaterialItemPart part : material.getParts().values()) {
                defer.accept(() -> PartTextureGenerator.processPart(coloramp, mtm, material, part));
            }
        }

        for (FluidDefinition fluid : MIFluids.FLUID_DEFINITIONS.values()) {
            defer.accept(() -> registerFluidTextures(mtm, fluid));
        }

        mtm.runAtEnd(() -> casingFromTextureBricked(mtm, MachineCasings.BRICKED_BRONZE,
                MI.id("textures/block/bronze_machine_casing.png")));
        mtm.runAtEnd(() -> casingFromTextureBricked(mtm, MachineCasings.BRICKED_STEEL,
                MI.id("textures/block/steel_machine_casing.png")));

        mtm.runAtEnd(() -> mtm.addTexture("modern_industrialization:textures/item/mixed_ingot_blastproof.png",
                TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/item/stainless_steel_ingot.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/titanium_ingot.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/tungsten_ingot.png"))));

        mtm.runAtEnd(() -> mtm.addTexture("modern_industrialization:textures/item/mixed_plate_nuclear.png",
                TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/item/cadmium_plate.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/beryllium_plate.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/blastproof_alloy_plate.png"), 1, 2)));

        mtm.runAtEnd(() -> mtm.addTexture("modern_industrialization:textures/item/mixed_ingot_iridium.png",
                TextureHelper.tripleTexture(mtm.getAssetAsTexture("modern_industrialization:textures/item/blastproof_alloy_ingot.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/iridium_ingot.png"),
                        mtm.getAssetAsTexture("modern_industrialization:textures/item/blastproof_alloy_ingot.png"))));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenComposeAsync(v -> {
                    // Do second phase work
                    return mtm.doEndWork();
                }, Util.backgroundExecutor())
                .thenRun(() -> {
                    mtm.markTexturesAsGenerated(fileHelper);
                })
                .thenRun(() -> MI.LOGGER.info("I used the png to destroy the png."));
    }

    private static String getTemplate(String materialSet, String part, String suffix) {
        return String.format("modern_industrialization:textures/materialsets/%s/%s%s.png", materialSet, part, suffix);
    }

    private static final String[] LAYERS = new String[] { "_underlay", "", "_overlay" };

    /**
     * Colorize layer if necessary.
     */
    private static void colorizeLayer(NativeImage image, String layer, IColoramp coloramp) {
        if (layer.equals("")) {
            TextureHelper.colorize(image, coloramp);
        }
    }

    public static void generateItemPartTexture(TextureManager mtm, String partTemplate, @Nullable String overlay, String materialSet, String path,
            boolean isBlock, IColoramp coloramp) {
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
            IColoramp coloramp) {
        generateItemPartTexture(mtm, partTemplate, null, materialSet, path, isBlock, coloramp);
    }

    public static void logTextureGenerationError(Throwable throwable, String path, String materialSet, String part) {
        MI.LOGGER.warn(
                String.format("Failed to generate item part texture for path %s, material set %s, partTemplate %s", path, materialSet, part),
                throwable);
    }

    public static NativeImage generateTexture(TextureManager mtm, String partTemplate, String materialSet, IColoramp coloramp) throws IOException {
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
                texturePath = String.format("modern_industrialization:textures/block/%s.png", path);
            } else {
                texturePath = String.format("modern_industrialization:textures/item/%s.png", path);
            }
            mtm.addTexture(texturePath, texture);
            texture.close();
        } else {
            throw new RuntimeException("Could not find any texture!");
        }
    }

    public static void generateDoubleIngot(TextureManager mtm, String materialName) throws IOException {
        String ingotTexture = String.format("modern_industrialization:textures/item/%s_ingot.png", materialName);
        if (materialName.equals("gold") || materialName.equals("iron") || materialName.equals("copper")) {
            ingotTexture = String.format("minecraft:textures/item/%s_ingot.png", materialName);
        }
        NativeImage image = mtm.getAssetAsTexture(ingotTexture);
        TextureHelper.doubleIngot(image);
        String itemPath = materialName + "_double_ingot";

        mtm.addTexture(String.format("modern_industrialization:textures/item/%s.png", itemPath), image);
        image.close();
    }

    public static void casingFromTexture(TextureManager tm, String casing, NativeImage texture) {
        for (String side : new String[] { "top", "side", "bottom" }) {

            try {
                String s = String.format("modern_industrialization:textures/block/casings/%s/%s.png", casing, side);
                tm.addTexture(s, TextureHelper.copy(texture));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void casingFromTextureBricked(TextureManager tm, MachineCasing casing, ResourceLocation topTexturePath) {
        try (
                var topTexture = tm.getAssetAsTexture(topTexturePath.toString());
                var brickTexture = tm.getAssetAsTexture("modern_industrialization:textures/block/fire_clay_bricks.png")) {
            if (topTexture.getWidth() != brickTexture.getWidth() || topTexture.getHeight() != brickTexture.getHeight()) {
                throw new IllegalArgumentException("Texture and Brick must have same dimension");
            }
            // Copy image
            try (NativeImage copy = TextureHelper.copy(topTexture)) {
                for (int i = 0; i < copy.getWidth(); ++i) {
                    for (int j = copy.getHeight() / 2; j < copy.getHeight(); j++) {
                        copy.setPixelRGBA(i, j, brickTexture.getPixelRGBA(i, j));
                    }
                }
                String s = String.format("modern_industrialization:textures/block/casings/%s.png", casing.name);
                tm.addTexture(s, copy);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate bricked casing texture", e);
        }
    }

    private static void registerFluidTextures(TextureManager tm, FluidDefinition fluid) {

        String path = "modern_industrialization:textures/fluid/";
        String bucket = path + "bucket.png";
        String bucket_content = path + "bucket_content.png";

        IColoramp fluidColoramp = new Coloramp(fluid.color);

        // Bucket
        try {
            NativeImage bucket_image = tm.getAssetAsTexture(bucket);
            NativeImage bucket_content_image = tm.getAssetAsTexture(bucket_content);
            TextureHelper.colorize(bucket_content_image, fluidColoramp);
            NativeImage oldBucketImage = bucket_image;
            bucket_image = TextureHelper.blend(oldBucketImage, bucket_content_image);
            oldBucketImage.close();
            if (fluid.isGas) {
                TextureHelper.flip(bucket_image);
            }
            tm.addTexture(String.format("modern_industrialization:textures/item/%s_bucket.png", fluid.path()), bucket_image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Flowing Textures

        String pathFluid = path + String.format("template/%s.png", fluid.fluidTexture.path);

        try {
            NativeImage fluidAnim = tm.getAssetAsTexture(pathFluid);
            TextureHelper.colorize(fluidAnim, fluidColoramp);
            TextureHelper.setAlpha(fluidAnim, fluid.opacity);
            tm.addTexture(String.format("modern_industrialization:textures/fluid/%s_still.png", fluid.path()), fluidAnim, true);
            tm.addMcMeta(String.format("modern_industrialization:textures/fluid/%s_still.png.mcmeta", fluid.path()), fluid.fluidTexture.mcMetaInfo);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
