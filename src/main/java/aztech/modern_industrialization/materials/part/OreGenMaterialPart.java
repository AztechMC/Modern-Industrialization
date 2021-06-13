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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.materials.MaterialOreSet;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.util.function.Function;
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

/**
 * A subclass of {@link RegularMaterialPart} that additionally registers an ore
 * configured feature. Use a regular material part if you don't need the ore to
 * be generated in the world.
 */
public class OreGenMaterialPart extends OreMaterialPart {
    private final int veinsPerChunk;
    private final int veinSize;
    private final int maxYLevel;

    private OreGenMaterialPart(String materialName, String part, String materialSet, Coloramp coloramp, MaterialOreSet oreSet, int veinsPerChunk,
            int veinSize, int maxYLevel) {
        super(materialName, part, materialSet, coloramp, oreSet);
        this.veinsPerChunk = veinsPerChunk;
        this.veinSize = veinSize;
        this.maxYLevel = maxYLevel;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet oreSet) {
        return ctx -> new OreGenMaterialPart(ctx.getMaterialName(), MIParts.ORE, ctx.getMaterialSet(), ctx.getColoramp(), oreSet, veinsPerChunk,
                veinSize, maxYLevel);
    }

    @Override
    public void register() {
        super.register();
        MIConfig config = MIConfig.getConfig();
        if (config.generateOres && !config.blacklistedOres.contains(materialName)) {
            ConfiguredFeature<?, ?> oreGenerator = Feature.ORE
                    .configure(new OreFeatureConfig(OreFeatureConfig.Rules.BASE_STONE_OVERWORLD, block.getDefaultState(), veinSize))
                    .decorate(Decorator.RANGE.configure(new RangeDecoratorConfig(0, 0, maxYLevel))).spreadHorizontally().repeat(veinsPerChunk);
            Identifier oregenId = new MIIdentifier("ore_generator_" + materialName);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oregenId, oreGenerator);
            RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_WORLDGEN, oregenId);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
        }
    }
}
