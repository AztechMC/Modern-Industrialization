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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.MITextures;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.util.TagHelper;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.world.item.Item;

public class RegularPart extends Part implements BuildablePart {

    private final Register register;
    private final Register clientRegister;
    private final TextureRegister textureRegister;

    private final String englishNameFormatter;

    public static String getEnglishName(String englishNameFormatter, String partEnglishName) {
        String englishName;

        if (englishNameFormatter.equals("")) {
            return partEnglishName;
        }

        if (englishNameFormatter.endsWith("!")) {
            englishName = englishNameFormatter.subSequence(0, englishNameFormatter.length() - 1).toString();
        } else {
            if (!englishNameFormatter.contains("%s")) {
                englishName = partEnglishName + " " + englishNameFormatter;
            } else {
                englishName = String.format(englishNameFormatter, partEnglishName);
            }
        }

        return englishName;

    }

    public RegularPart(String englishNameFormatter, String key) {
        this(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag) -> {
            var item = createSimpleItem(getEnglishName(englishNameFormatter, partContext.getEnglishName()), itemPath, partContext, part);
            setupTag(part, itemTag, item);
        }, (partContext, part, itemPath, itemId, itemTag) -> {
        }, (mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, part.key, partContext.getMaterialSet(), itemPath, false,
                partContext.getColoramp()));

    }

    private RegularPart(String englishNameFormatter, String key, Register register, Register clientRegister, TextureRegister textureRegister) {
        super(key);
        this.englishNameFormatter = englishNameFormatter;
        this.register = register;
        this.clientRegister = clientRegister;
        this.textureRegister = textureRegister;
    }

    public static Item createSimpleItem(String englishName, String itemPath, MaterialBuilder.PartContext partContext, Part part) {
        return MIItem.item(englishName, itemPath, SortOrder.MATERIALS.and(partContext.getMaterialName()).and(part)).asItem();
    }

    private static void setupTag(Part part, String itemTag, Item item) {
        // item tag
        // items whose path are overridden (such as fire clay ingot -> brick) are not
        // added to the tags
        for (Part partTagged : MIParts.TAGGED_PARTS) {
            if (partTagged.equals(part)) {
                TagsToGenerate.generateTag(itemTag.replaceFirst("#", ""), item);
            }
        }
    }

    public RegularPart asBlock(SortOrder sortOrder, float hardness, float resistance, int miningLevel) {
        return new RegularPart(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag) -> {

            var blockDefinition = MIBlock.block(
                    getEnglishName(englishNameFormatter, partContext.getEnglishName()),
                    itemPath,
                    MIBlock.BlockDefinitionParams.of()
                            .clearTags()
                            .addMoreTags(TagHelper.getMiningLevelTag(miningLevel))
                            .sortOrder(sortOrder.and(partContext.getMaterialName()))
                            .destroyTime(hardness)
                            .explosionResistance(resistance));

            setupTag(part, itemTag, blockDefinition.asItem());

        }, clientRegister, (mtm, partContext, part, itemPath) -> MITextures.generateItemPartTexture(mtm, part.key, partContext.getMaterialSet(),
                itemPath, true, partContext.getColoramp()));
    }

    public RegularPart asColumnBlock(SortOrder sortOrder) {
        return new RegularPart(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag) -> {

            var blockDefinition = MIBlock.block(
                    getEnglishName(englishNameFormatter, partContext.getEnglishName()),
                    itemPath,
                    MIBlock.BlockDefinitionParams.of()
                            .clearTags()
                            .addMoreTags(TagHelper.getMiningLevelTag(1))
                            .sortOrder(sortOrder.and(partContext.getMaterialName()))
                            .withModel(TexturedModel.COLUMN)
                            .destroyTime(5.0f)
                            .explosionResistance(6.0f)

            );

            setupTag(part, itemTag, blockDefinition.asItem());

        }, clientRegister, (mtm, partContext, part, itemPath) -> {
            for (String suffix : new String[] { "_side", "_top" }) {
                String template = String.format("modern_industrialization:textures/materialsets/common/%s%s.png", part, suffix);
                try {
                    NativeImage image = mtm.getAssetAsTexture(template);
                    TextureHelper.colorize(image, partContext.getColoramp());
                    String texturePath;
                    texturePath = String.format("modern_industrialization:textures/block/%s%s.png", itemPath, suffix);
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

    public RegularPart asBlock(SortOrder sortOrder) {
        return asBlock(sortOrder, 5, 6, 1);
    }

    public RegularPart withRegister(Register register) {
        return new RegularPart(englishNameFormatter, key, register, clientRegister, textureRegister);
    }

    public RegularPart withClientRegister(Register clientRegister) {
        return new RegularPart(englishNameFormatter, key, register, clientRegister, textureRegister);
    }

    public RegularPart withTextureRegister(TextureRegister textureRegister) {
        return new RegularPart(englishNameFormatter, key, register, clientRegister, textureRegister);
    }

    public RegularPart appendRegister(Register register) {
        return new RegularPart(englishNameFormatter, key,

                (partContext, part, itemPath, itemId, itemTag) -> {
                    RegularPart.this.register.register(partContext, part, itemPath, itemId, itemTag);
                    register.register(partContext, part, itemPath, itemId, itemTag);
                }, clientRegister, this.textureRegister);
    }

    public RegularPart appendTextureRegister(TextureRegister textureRegister) {
        return new RegularPart(englishNameFormatter, key, register, clientRegister, (mtm, partContext, part, itemPath) -> {
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
            public void register(MaterialBuilder.PartContext context) {
                register.register(partContext, part, itemPath, itemId, itemTag);

            }

            public void registerClient() {
                clientRegister.register(partContext, part, itemPath, itemId, itemTag);
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

        void register(MaterialBuilder.PartContext partContext, Part part, String itemPath, String itemId, String itemTag);

    }

    @FunctionalInterface
    public interface TextureRegister {

        void register(TextureManager mtm, MaterialBuilder.PartContext partContext, Part part, String itemPath);

    }
}
