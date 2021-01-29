package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.material.OreBlock;
import aztech.modern_industrialization.materials.MaterialHelper;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
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

import java.util.Objects;

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;
import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

/**
 * A regular material item part, for example bronze curved plates.
 */
public class RegularItemPart implements MaterialPart {
    private final String materialName;
    private final String part;
    private final String itemPath;
    private final String itemId;
    private final String itemTag;
    private final String materialSet;
    private final int color;
    private MIBlock block;
    private Item item;

    public RegularItemPart(String materialName, String part, String materialSet, int color) {
        this.materialName = materialName;
        this.part = part;
        this.itemPath = materialName + "_" + part;
        this.itemId = "modern_industrialization:" + itemPath;
        this.itemTag = "#c:" + materialName + "_" + part + "s";
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
                    .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(0, 0, ore.maxYLevel)))
                    .spreadHorizontally().repeat(ore.veinsPerChunk);
            Identifier oregenId = new MIIdentifier("ore_generator_" + materialName);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oregenId, oreGenerator);
            RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, oregenId);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
        }
    }

    @Override
    public Item getItem() {
        return Objects.requireNonNull(item);
    }
}
