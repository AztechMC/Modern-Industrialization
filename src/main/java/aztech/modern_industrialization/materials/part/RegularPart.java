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
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.world.item.Item;

public class RegularPart extends Part implements BuildablePart {

    private final Register register;
    private final Register clientRegister;
    private final TextureRegister textureRegister;

    public RegularPart(String key) {
        this(key, (registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            var item = MIItem.of(itemPath);
            setupTag(part, itemTag, item);
        }, (registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
        }, (mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, part.key, partContext.getMaterialSet(), itemPath, false,
                partContext.getColoramp()));

    }

    private RegularPart(String key, Register register, Register clientRegister, TextureRegister textureRegister) {
        super(key);
        this.register = register;
        this.clientRegister = clientRegister;
        this.textureRegister = textureRegister;
    }

    private static void setupTag(Part part, String itemTag, Item item) {
        // item tag
        // items whose path are overridden (such as fire clay ingot -> brick) are not
        // added to the tags
        for (Part partTagged : MIParts.TAGGED_PARTS) {
            if (partTagged.equals(part)) {
                MIItemTagProvider.generateTag(itemTag.replaceFirst("#", ""), item);
            }
        }
    }

    public RegularPart asBlock(float hardness, float resistance, int miningLevel) {
        return new RegularPart(key, (registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            var block = new MIBlock(itemPath,
                    FabricBlockSettings.of(METAL_MATERIAL).destroyTime(hardness)
                            .explosionResistance(resistance)
                            .requiresCorrectToolForDrops()).setPickaxeMineable().setMiningLevel(miningLevel);
            setupTag(part, itemTag, block.blockItem);
        }, clientRegister, (mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, part.key, partContext.getMaterialSet(),
                itemPath, true, partContext.getColoramp()));
    }

    public RegularPart asColumnBlock() {
        return new RegularPart(key, (registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            MIBlock block = new MIBlock(itemPath,
                    FabricBlockSettings.of(METAL_MATERIAL).destroyTime(5.0f).explosionResistance(6.0f)
                            .requiresCorrectToolForDrops()).setPickaxeMineable();
            block.asColumn();
        }, clientRegister, (mtm, partContext, part, itemPath) -> {
            for (String suffix : new String[] { "_end", "_side" }) {
                String template = String.format("modern_industrialization:textures/materialsets/common/%s%s.png", part, suffix);
                try {
                    NativeImage image = mtm.getAssetAsTexture(template);
                    TextureHelper.colorize(image, partContext.getColoramp());
                    String texturePath;
                    texturePath = String.format("modern_industrialization:textures/blocks/%s%s.png", itemPath, suffix);
                    mtm.addTexture(texturePath, image);
                    image.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        );
    }

    public RegularPart withoutTextureRegister() {
        return this.withTextureRegister((mtm, partContext, part, itemPath) -> {
        });
    }

    public RegularPart asBlock() {
        return asBlock(5, 6, 0);
    }

    public RegularPart withRegister(Register register) {
        return new RegularPart(key, register, clientRegister, textureRegister);
    }

    public RegularPart withClientRegister(Register clientRegister) {
        return new RegularPart(key, register, clientRegister, textureRegister);
    }

    public RegularPart withTextureRegister(TextureRegister textureRegister) {
        return new RegularPart(key, register, clientRegister, textureRegister);
    }

    public RegularPart appendRegister(Register register) {
        return new RegularPart(key,

                (registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
                    RegularPart.this.register.register(registeringContext, partContext, part, itemPath, itemId, itemTag);
                    register.register(registeringContext, partContext, part, itemPath, itemId, itemTag);
                }, clientRegister, this.textureRegister);
    }

    public RegularPart appendTextureRegister(TextureRegister textureRegister) {
        return new RegularPart(key, register, clientRegister, (mtm, partContext, part, itemPath) -> {
            RegularPart.this.textureRegister.register(mtm, partContext, part, itemPath);
            textureRegister.register(mtm, partContext, part, itemPath);
        });
    }

    public RegularPart withOverlay(Part normal, String overlay) {
        return withTextureRegister((mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, normal.key, overlay,
                partContext.getMaterialSet(), itemPath, false, partContext.getColoramp()));
    }

    private static MaterialPart build(String itemPath, String itemId, String itemTag, MaterialBuilder.PartContext partContext, Part part,
            Register register, Register clientRegister, TextureRegister textureRegister) {
        return new MaterialPart() {
            @Override
            public Part getPart() {
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
            public void register(MaterialBuilder.RegisteringContext registeringContext) {
                register.register(registeringContext, partContext, part, itemPath, itemId, itemTag);

            }

            public void registerClient() {
                clientRegister.register(null, partContext, part, itemPath, itemId, itemTag);
            }

            @Override
            public void registerTextures(TextureManager mtm) {
                textureRegister.register(mtm, partContext, part, itemPath);
            }

        };
    }

    public static String idFromPath(String path) {
        return "modern_industrialization:" + path;
    }

    @Override
    public MaterialPart build(MaterialBuilder.PartContext ctx) {

        String itemPath = ctx.getMaterialName() + "_" + key;
        String itemId = idFromPath(itemPath);
        String itemTag;

        if (MIParts.TAGGED_PARTS.contains(this)) {
            itemTag = "#c:" + ctx.getMaterialName() + "_" + key + "s";
        } else {
            itemTag = itemId;
        }

        return build(itemPath, itemId, itemTag, ctx, this, this.register, this.clientRegister, this.textureRegister);

    }

    @Override
    public Part getPart() {
        return this;
    }

    public BuildablePart withCustomPath(String itemPath, String itemTag) {
        return BuildablePart.of(this, ctx -> build(itemPath, idFromPath(itemPath), itemTag, ctx, RegularPart.this, RegularPart.this.register,
                RegularPart.this.clientRegister, RegularPart.this.textureRegister));
    }

    public BuildablePart withCustomPath(String itemPath) {
        return withCustomPath(itemPath, itemPath);
    }

    public BuildablePart withCustomFormattablePath(String itemPath, String itemTag) {
        return BuildablePart.of(this, ctx -> {
            String path = String.format(itemPath, ctx.getMaterialName());
            String tag = "#c:" + String.format(itemTag, ctx.getMaterialName());

            return build(path, idFromPath(path), tag, ctx, RegularPart.this, RegularPart.this.register, RegularPart.this.clientRegister,
                    RegularPart.this.textureRegister);
        });

    }

    public BuildablePart withCustomFormattablePath(String itemPath) {
        return withCustomFormattablePath(itemPath, itemPath);
    }

    @FunctionalInterface
    public interface Register {

        void register(MaterialBuilder.RegisteringContext registeringContext, MaterialBuilder.PartContext partContext, Part part, String itemPath,
                String itemId, String itemTag);

    }

    @FunctionalInterface
    public interface TextureRegister {

        void register(TextureManager mtm, MaterialBuilder.PartContext partContext, Part part, String itemPath);

    }
}
