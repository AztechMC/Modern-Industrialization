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
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;

public class OreGenMaterialPart extends OreMaterialPart {
    private final int veinsPerChunk;
    private final int veinSize;
    private final int maxYLevel;

    private OreGenMaterialPart(String materialName, Coloramp coloramp, MaterialOreSet oreSet, int veinsPerChunk, int veinSize, int maxYLevel,
            boolean deepslate, UniformIntProvider xpDropped, String mainPart) {
        super(materialName, coloramp, oreSet, deepslate, xpDropped, mainPart);
        this.veinsPerChunk = veinsPerChunk;
        this.veinSize = veinSize;
        this.maxYLevel = maxYLevel;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet oreSet,
            UniformIntProvider xpDropped) {
        Function<MaterialBuilder.PartContext, MaterialPart>[] array = new Function[2];
        for (int i = 0; i < 2; i++) {
            final int j = i;
            Function<MaterialBuilder.PartContext, MaterialPart> function = ctx -> new OreGenMaterialPart(ctx.getMaterialName(), ctx.getColoramp(),
                    oreSet, veinsPerChunk, veinSize, maxYLevel, j == 0, xpDropped, ctx.getMainPart());
            array[i] = function;
        }
        return array;
    }

    public static Function<MaterialBuilder.PartContext, MaterialPart>[] of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet oreSet) {
        return of(veinsPerChunk, veinSize, maxYLevel, oreSet, UniformIntProvider.create(0, 0));
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        super.register(context);
        MIConfig config = MIConfig.getConfig();

        if (config.generateOres && !config.blacklistedOres.contains(materialName)) {
            // I have no idea what I'm doing
            List<OreFeatureConfig.Target> targets = List
                    .of(deepslate ? OreFeatureConfig.createTarget(OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES, block.getDefaultState())
                            : OreFeatureConfig.createTarget(OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES, block.getDefaultState()));
            OreFeatureConfig oreConfig = new OreFeatureConfig(targets, veinSize);
            ConfiguredFeature<?, ?> oreGenerator = Feature.ORE.configure(oreConfig).uniformRange(YOffset.getBottom(), YOffset.fixed(maxYLevel))
                    .spreadHorizontally().repeat(veinsPerChunk);
            Identifier oreGenId = new MIIdentifier((deepslate ? "deepslate_" : "") + "ore_generator_" + materialName);
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oreGenId, oreGenerator);
            RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, oreGenId);
            BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
        }
    }
}
