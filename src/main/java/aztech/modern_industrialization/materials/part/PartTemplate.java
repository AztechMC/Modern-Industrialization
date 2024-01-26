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
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.util.TagHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class PartTemplate implements PartKeyProvider {

    private final PartEnglishNameFormatter englishNameFormatter;
    private final PartKey partKey;
    private final Register register;
    private final TextureGenParams textureGenParams;
    private final PartItemPathFormatter itemPathFormatter;

    public PartTemplate(PartEnglishNameFormatter englishNameFormatter, String key) {
        this(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag, englishName) -> {
            var item = createSimpleItem(englishName, itemPath, partContext, part);
            setupTag(partContext, part, itemTag, item);
        }, new TextureGenParams.SimpleRecoloredItem());
    }

    public PartTemplate(PartEnglishNameFormatter englishNameFormatter, PartKey key) {
        this(englishNameFormatter, key, (partContext, part, itemPath, itemId, itemTag, englishName) -> {
            var item = createSimpleItem(englishName, itemPath, partContext, part);
            setupTag(partContext, part, itemTag, item);
        }, new TextureGenParams.SimpleRecoloredItem(), new PartItemPathFormatter.Default());
    }

    public PartTemplate(String englishName, String key) {
        this(new PartEnglishNameFormatter.Default(englishName), key);
    }

    public PartTemplate(String englishName, PartKey key) {
        this(new PartEnglishNameFormatter.Default(englishName), key);
    }

    private PartTemplate(PartEnglishNameFormatter englishNameFormatter, String key, Register register, TextureGenParams textureGenParams) {
        this(englishNameFormatter, new PartKey(key), register, textureGenParams, new PartItemPathFormatter.Default());
    }

    private PartTemplate(PartEnglishNameFormatter englishNameFormatter, PartKey key, Register register, TextureGenParams textureGenParams,
            PartItemPathFormatter itemPathFormatter) {
        this.partKey = key;
        this.englishNameFormatter = englishNameFormatter;
        this.register = register;
        this.textureGenParams = textureGenParams;
        this.itemPathFormatter = itemPathFormatter;
    }

    public static ItemDefinition<Item> createSimpleItem(String englishName, String itemPath, MaterialBuilder.PartContext partContext, PartKey part) {
        return MIItem.item(englishName, itemPath, SortOrder.MATERIALS.and(partContext.getMaterialName()).and(part));
    }

    private static void setupTag(MaterialBuilder.PartContext context, PartKey part, String itemTag, ItemLike item) {
        // item tag
        // items whose path are overridden (such as fire clay ingot -> brick) are not
        // added to the tags
        for (PartKey partTagged : MIParts.TAGGED_PARTS) {
            if (partTagged.equals(part)) {
                var generatedTag = itemTag.replaceFirst("#", "");
                TagsToGenerate.generateTag(generatedTag, item, getTagEnglishName(context, itemTag));

                var categoryTag = MIParts.CATEGORY_TAGS.get(partTagged.key());
                if (categoryTag != null) {
                    TagsToGenerate.addTagToTag(generatedTag, categoryTag.tag(), categoryTag.englishName());
                }
            }
        }
    }

    private static String getTagEnglishName(MaterialBuilder.PartContext context, String tag) {
        var parts = tag.split(":")[1].split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Tag " + tag + " has more than 2 slash-separated parts");
        }

        var sb = new StringBuilder();

        var part = parts[0];
        var material = parts[1];
        boolean rawOre = false;

        if (material.startsWith("raw_")) {
            // Hacky fix for raw ores...
            sb.append("Raw ");
            material = material.substring(4);
            part = "ores";
        }

        if (!material.equals(context.getMaterialName())) {
            throw new IllegalArgumentException("Tag " + tag + " does not contain the material name after slash: " + context.getMaterialName());
        }

        part = part.replace('_', ' ');

        sb.append(context.getMaterialEnglishName());
        sb.append(' ');

        boolean capitalize = true;

        for (char c : part.toCharArray()) {
            if (c == ' ') {
                sb.append(c);
                capitalize = true;
            } else if (capitalize) {
                sb.append(Character.toUpperCase(c));
                capitalize = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public PartTemplate asBlock(SortOrder sortOrder, TextureGenParams textureGenParams, float hardness, float resistance, int miningLevel,
            boolean doAllowSpawn) {
        Register blockRegister = (partContext, part, itemPath, itemId, itemTag, itemEnglishName) -> {

            var blockParams = MIBlock.BlockDefinitionParams.defaultStone()
                    .clearTags()
                    .addMoreTags(TagHelper.getMiningLevelTag(miningLevel))
                    .sortOrder(sortOrder.and(partContext.getMaterialName()))
                    .destroyTime(hardness)
                    .explosionResistance(resistance);

            if (!doAllowSpawn) {
                blockParams.isValidSpawn((p1, p2, p3, p4) -> false);
            }

            var blockDefinition = MIBlock.block(
                    itemEnglishName, itemPath, blockParams

            );

            setupTag(partContext, part, itemTag, blockDefinition);

        };
        return new PartTemplate(englishNameFormatter, partKey, blockRegister, textureGenParams, itemPathFormatter);
    }

    public PartTemplate asBlock(SortOrder sortOrder, TextureGenParams textureGenParams, float hardness, float resistance, int miningLevel) {
        return asBlock(sortOrder, textureGenParams, hardness, resistance, miningLevel, true);
    }

    public PartTemplate asColumnBlock(SortOrder sortOrder) {
        Register columnBlockRegister = (partContext, part, itemPath, itemId, itemTag, itemEnglishName) -> {

            var blockDefinition = MIBlock.block(
                    itemEnglishName,
                    itemPath,
                    MIBlock.BlockDefinitionParams.defaultStone()
                            .clearTags()
                            .addMoreTags(TagHelper.getMiningLevelTag(1))
                            .sortOrder(sortOrder.and(partContext.getMaterialName()))
                            .withModel((block, gen) -> {
                                String name = gen.name(block);
                                gen.simpleBlockWithItem(block,
                                        gen.models().cubeColumn(name, gen.blockTexture(name + "_side"), gen.blockTexture(name + "_top")));
                            })
                            .destroyTime(5.0f)
                            .explosionResistance(6.0f)

            );

            setupTag(partContext, part, itemTag, blockDefinition);

        };
        return new PartTemplate(englishNameFormatter, partKey, columnBlockRegister, new TextureGenParams.ColumnBlock(), itemPathFormatter);
    }

    public PartTemplate withoutTextureRegister() {
        return this.withTexture(new TextureGenParams.NoTexture());
    }

    public PartTemplate asBlock(SortOrder sortOrder, TextureGenParams textureGenParams) {
        return asBlock(sortOrder, textureGenParams, 5, 6, 1);
    }

    public PartTemplate withRegister(Register register) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, itemPathFormatter);
    }

    public PartTemplate withTexture(TextureGenParams textureGenParams) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, itemPathFormatter);
    }

    public PartTemplate withOverlay(PartKey normal, String overlay) {
        return withTexture(new TextureGenParams.SimpleRecoloredItem(normal, overlay));
    }

    public PartTemplate withOverlay(PartKeyProvider normal, String overlay) {
        return withOverlay(normal.key(), overlay);
    }

    public PartTemplate withCustomPath(String itemPath, String itemTag) {
        return new PartTemplate(englishNameFormatter, partKey, register, textureGenParams, new PartItemPathFormatter.Overridden(itemPath, itemTag));
    }

    public PartTemplate withCustomPath(String itemPath) {
        return withCustomPath(itemPath, itemPath);
    }

    public TextureGenParams getTextureGenParams() {
        return textureGenParams;
    }

    public MaterialItemPart create(String material, String materialEnglishName) {

        String itemPath = this.itemPathFormatter.getPartItemPath(material, partKey);
        String itemId = this.itemPathFormatter.getPartItemId(material, partKey);
        String itemTag = this.itemPathFormatter.getPartItemTag(material, partKey);
        String itemEnglishName = englishNameFormatter.format(materialEnglishName);

        return new MaterialItemPartImpl(partKey, itemTag, itemId, ctx -> {
            register.register(ctx, partKey, itemPath, itemId, itemTag, itemEnglishName);
        }, this.textureGenParams, true);
    }

    @Override
    public PartKey key() {
        return partKey;
    }

    @FunctionalInterface
    public interface Register {

        void register(MaterialBuilder.PartContext partContext, PartKey part, String itemPath, String itemId, String itemTag, String itemEnglishName);

    }
}
