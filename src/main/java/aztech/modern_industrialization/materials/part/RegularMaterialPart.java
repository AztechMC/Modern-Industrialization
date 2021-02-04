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
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.material.OreBlock;
import aztech.modern_industrialization.materials.MaterialHelper;
import aztech.modern_industrialization.materials.textures.MIMaterialTextures;
import aztech.modern_industrialization.materials.textures.MaterialTextureManager;
import java.util.Objects;
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

/**
 * A regular material item part, for example bronze curved plates.
 */
public class RegularMaterialPart implements MaterialPart {
    private final String materialName;
    private final String part;
    private final String itemPath;
    private final String itemId;
    private final String itemTag;
    private final String materialSet;
    private final int color;
    @SuppressWarnings("FieldCanBeLocal")
    private MIBlock block;
    private Item item;

    public RegularMaterialPart(String materialName, String part, String materialSet, int color) {
        this.materialName = materialName;
        this.part = part;

        if (part.equals(MIParts.GEM)) {
            this.itemPath = materialName;
        } else {
            this.itemPath = materialName + "_" + part;
        }

        this.itemId = "modern_industrialization:" + itemPath;
        if (MIParts.TAGGED_PARTS.contains(part)) {
            this.itemTag = "#c:" + materialName + "_" + part + "s";
        } else {
            this.itemTag = itemId;
        }

        this.materialSet = materialSet;
        this.color = color;

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
                block = new OreBlock(itemPath, FabricBlockSettings.of(STONE_MATERIAL).hardness(3.0f).resistance(3.0f)
                        .breakByTool(FabricToolTags.PICKAXES, 1).requiresTool());
            } else {
                block = new MIBlock(itemPath, FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f).resistance(6.0f)
                        .breakByTool(FabricToolTags.PICKAXES, 0).requiresTool());
            }
            item = block.blockItem;
        } else {
            block = null;
            item = MIItem.of(itemPath);
        }
        // ore generator
        if (MaterialHelper.isOre(part)) {
            OreBlock ore = (OreBlock) block;
            ConfiguredFeature<?, ?> oreGenerator = Feature.ORE
                    .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, block.getDefaultState(), ore.veinSize))
                    .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(0, 0, ore.maxYLevel))).spreadHorizontally()
                    .repeat(ore.veinsPerChunk);
            Identifier oregenId = new MIIdentifier("ore_generator_" + materialName);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oregenId, oreGenerator);
            RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, oregenId);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
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
    public void registerTextures(MaterialTextureManager materialTextureManager) {
        MIMaterialTextures.generateItemPartTexture(materialTextureManager, materialName, materialSet, part, color);
    }
}
