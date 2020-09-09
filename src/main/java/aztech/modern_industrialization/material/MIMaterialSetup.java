package aztech.modern_industrialization.material;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.decorator.RangeDecoratorConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

import java.util.HashMap;
import java.util.Map;

public class MIMaterialSetup {
    public static final Map<Identifier, ConfiguredFeature<?, ?>> ORE_GENERATORS = new HashMap<>();
    // Materials
    public static final Material METAL_MATERIAL = new FabricMaterialBuilder(MaterialColor.IRON).build();
    public static final Material STONE_MATERIAL = new FabricMaterialBuilder(MaterialColor.STONE).build();

    static {
        MIMaterial fetcher = MIMaterials.gold; // force static load TODO : REFACTOR

        for(MIMaterial material : MIMaterial.getAllMaterials()) {

            for (String block_type : material.getBlockType()) {

                Block block = null;
                if (block_type.equals("block")) {
                    block = new MaterialBlock(FabricBlockSettings.of(METAL_MATERIAL).hardness(5.0f)
                            .resistance(6.0f)
                            .breakByTool(FabricToolTags.PICKAXES, 0)
                            .requiresTool(), material.getId(), "block"
                    );
                } else if (block_type.equals("ore")) {
                    block = new MaterialBlock(FabricBlockSettings.of(STONE_MATERIAL).hardness(3.0f)
                            .resistance(3.0f)
                            .breakByTool(FabricToolTags.PICKAXES, 1)
                            .requiresTool(), material.getId(), "ore"
                    );
                }
                material.saveBlock(block_type, block);

            }

            if(material.hasOre()) {
                ConfiguredFeature<?, ?> ore_generator = Feature.ORE
                        .configure(new OreFeatureConfig(
                                OreFeatureConfig.Rules.BASE_STONE_OVERWORLD,
                                material.getBlock("ore").getDefaultState(),
                                material.getVeinsSize())) // vein size
                        .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(
                                0, // bottom offset
                                0, // min y level
                                material.getMaxYLevel()))) // max y level
                        .spreadHorizontally()
                        .repeat(material.getVeinsPerChunk()); // number of veins per chunk
                Identifier oregen_id = new MIIdentifier("ore_generator_" + material.getId());
                ORE_GENERATORS.put(oregen_id, ore_generator);
                Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oregen_id, ore_generator);
            }
        }
    }
}
