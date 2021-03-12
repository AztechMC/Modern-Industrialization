package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.material.OreBlock;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
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

import java.util.function.Function;

/**
 * A subclass of {@link RegularMaterialPart} that additionally registers an ore configured feature.
 * Use a regular material part if you don't need the ore to be generated in the world.
 */
public class OreMaterialPart extends RegularMaterialPart {
    private final int veinsPerChunk;
    private final int veinSize;
    private final int maxYLevel;

    private OreMaterialPart(String materialName, String part, String materialSet, Coloramp coloramp, int veinsPerChunk, int veinSize, int maxYLevel) {
        super(materialName, part, materialSet, coloramp);
        this.veinsPerChunk = veinsPerChunk;
        this.veinSize = veinSize;
        this.maxYLevel = maxYLevel;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(int veinsPerChunk, int veinSize, int maxYLevel) {
        return ctx -> new OreMaterialPart(ctx.getMaterialName(), MIParts.ORE, ctx.getMaterialSet(), ctx.getColoramp(), veinsPerChunk, veinSize, maxYLevel);
    }

    @Override
    public void register() {
        super.register();
        MIConfig config = MIConfig.getConfig();
        if (config.generateOres && !config.blacklistedOres.contains(materialName)) {
            ConfiguredFeature<?, ?> oreGenerator = Feature.ORE
                    .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, block.getDefaultState(), veinSize))
                    .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(0, 0, maxYLevel))).spreadHorizontally()
                    .repeat(veinsPerChunk);
            Identifier oregenId = new MIIdentifier("ore_generator_" + materialName);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oregenId, oreGenerator);
            RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, oregenId);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
        }
    }
}
