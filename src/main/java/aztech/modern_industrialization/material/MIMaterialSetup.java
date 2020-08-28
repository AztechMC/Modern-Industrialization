package aztech.modern_industrialization.material;

import aztech.modern_industrialization.MIIdentifier;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.util.Identifier;
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
        for(MIMaterial material : MIMaterial.getAllMaterials()) {
            for (String block_type : (material.hasOre() ? new String[]{"block", "ore"} : new String[]{"block"})) {
                Block block = null;
                if (block_type.equals("block")) {
                    block = new Block(FabricBlockSettings.of(METAL_MATERIAL).hardness(material.getHardness())
                            .resistance(material.getBlastResistance())
                            .breakByTool(FabricToolTags.PICKAXES, 0)
                            .requiresTool()
                    );
                } else if (block_type.equals("ore")) {
                    block = new Block(FabricBlockSettings.of(STONE_MATERIAL).hardness(material.getOreHardness())
                            .resistance(material.getOreBlastResistance())
                            .breakByTool(FabricToolTags.PICKAXES, 1)
                            .requiresTool()
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
                ORE_GENERATORS.put(new MIIdentifier("ore_generator_" + material.getId()), ore_generator);
            }
        }
    }
}
