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

import static aztech.modern_industrialization.materials.property.MaterialProperty.MAIN_PART;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.datagen.dynreg.DynamicRegistryDatagen;
import aztech.modern_industrialization.datagen.loot.MIBlockLoot;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class OrePart implements PartKeyProvider {

    public final boolean deepslate;
    public final PartKey key;

    @Override
    public PartKey key() {
        return key;
    }

    public PartTemplate of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(UniformInt.of(0, 2), set, veinsPerChunk, veinSize, maxYLevel));
    }

    public PartTemplate of(UniformInt xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public PartTemplate of(UniformInt xpProvider, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set));
    }

    public PartTemplate of(MaterialOreSet set) {
        return of(new OrePartParams(UniformInt.of(0, 0), set));
    }

    public OrePart(boolean deepslate) {
        key = new PartKey(deepslate ? "ore_deepslate" : "ore");
        this.deepslate = deepslate;
    }

    public PartTemplate of(OrePartParams oreParams) {
        return new PartTemplate(deepslate ? "Deepslate %s Ore" : "Ore", key)
                .withRegister((partContext, part, itemPath, itemId, itemTag, englishName) -> {

                    PartKey mainPartKey = partContext.get(MAIN_PART).key();
                    String loot;
                    if (mainPartKey.equals(MIParts.INGOT.key())) {
                        loot = partContext.getMaterialPart(MIParts.RAW_METAL).getItemId();
                    } else if (mainPartKey.equals(MIParts.DUST.key())) {
                        loot = partContext.getMaterialPart(MIParts.DUST).getItemId();
                    } else if (mainPartKey.equals(MIParts.GEM.key())) {
                        loot = partContext.getMaterialPart(MIParts.GEM).getItemId();
                    } else {
                        throw new UnsupportedOperationException("Could not find matching main part.");
                    }

                    BlockDefinition<OreBlock> oreBlockBlockDefinition;
                    oreBlockBlockDefinition = MIBlock.block(
                            englishName,
                            itemPath,
                            MIBlock.BlockDefinitionParams.defaultStone()
                                    .withBlockConstructor(s -> new OreBlock(s, oreParams, partContext.getMaterialName()))
                                    .withLoot(new MIBlockLoot.Ore(loot))
                                    .addMoreTags(List.of(Tags.Blocks.ORES))
                                    .sortOrder(SortOrder.ORES.and(partContext.getMaterialName()))
                                    .destroyTime(deepslate ? 4.5f : 3.0f).explosionResistance(3.0f)
                                    .sound(deepslate ? SoundType.DEEPSLATE : SoundType.STONE));

                    // Sanity check: Ensure that ores don't drop xp, iff the main part is an ingot
                    // (i.e. the drop is raw ore).
                    if (mainPartKey.equals(MIParts.INGOT.key()) != (oreParams.xpDropped.getMaxValue() == 0)) {
                        throw new IllegalArgumentException("Mismatch between raw ore and xp drops for material: " + partContext.getMaterialName());
                    }

                    String tag = "forge:ores/" + partContext.getMaterialName();

                    TagsToGenerate.generateTag(tag, oreBlockBlockDefinition, partContext.getMaterialEnglishName() + " Ores");
                    TagsToGenerate.addTagToTag(tag, Tags.Items.ORES.location().toString(), "Ores");

                    if (oreParams.generate) {
                        ResourceLocation oreGenId = new MIIdentifier(
                                (deepslate ? "deepslate_" : "") + "ore_generator_" + partContext.getMaterialName());

                        var featureKey = ResourceKey.create(Registries.CONFIGURED_FEATURE, oreGenId);
                        var placedFeatureKey = ResourceKey.create(Registries.PLACED_FEATURE, oreGenId);
                        var modifierKey = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, oreGenId);

                        DynamicRegistryDatagen.addAction(() -> {
                            var ruleTest = new TagMatchTest(deepslate ? BlockTags.DEEPSLATE_ORE_REPLACEABLES : BlockTags.STONE_ORE_REPLACEABLES);

                            var target = List.of(
                                    OreConfiguration.target(ruleTest, oreBlockBlockDefinition.asBlock().defaultBlockState()));

                            DynamicRegistryDatagen.add(Registries.CONFIGURED_FEATURE, context -> {
                                FeatureUtils.register(context, featureKey, Feature.ORE, new OreConfiguration(target, oreParams.veinSize));
                            });

                            DynamicRegistryDatagen.add(Registries.PLACED_FEATURE, context -> {
                                var holder = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(featureKey);
                                var placement = OrePlacements.commonOrePlacement(
                                        oreParams.veinsPerChunk,
                                        HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(oreParams.maxYLevel)));
                                PlacementUtils.register(context, placedFeatureKey, holder, placement);
                            });

                            DynamicRegistryDatagen.add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, context -> {
                                var modifier = new BiomeModifiers.AddFeaturesBiomeModifier(
                                        context.lookup(Registries.BIOME).getOrThrow(BiomeTags.IS_OVERWORLD),
                                        HolderSet.direct(context.lookup(Registries.PLACED_FEATURE).getOrThrow(placedFeatureKey)),
                                        GenerationStep.Decoration.UNDERGROUND_ORES);
                                context.register(modifierKey, modifier);
                            });
                        });
                    }

                })
                .withTexture(new TextureGenParams.Ore(deepslate, oreParams.set))
                .withCustomPath((deepslate ? "deepslate_" : "") + "%s_ore", "ores/%s");
    }

    public List<PartTemplate> ofAll(OrePartParams params) {
        return List.of(MIParts.ORE_DEEPSLATE.of(params), MIParts.ORE.of(params));
    }

    public List<PartTemplate> ofAll(UniformInt xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return ofAll(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public List<PartTemplate> ofAll(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return ofAll(new OrePartParams(UniformInt.of(0, 0), set, veinsPerChunk, veinSize, maxYLevel));
    }

    public static class OrePartParams {

        public final UniformInt xpDropped;
        public final MaterialOreSet set;
        public final boolean generate;

        public final int veinsPerChunk;
        public final int veinSize;
        public final int maxYLevel;

        private OrePartParams(UniformInt xpDropped, MaterialOreSet set, boolean generate, int veinsPerChunk, int veinSize, int maxYLevel) {
            this.xpDropped = xpDropped;
            this.set = set;
            this.generate = generate;

            this.veinsPerChunk = veinsPerChunk;
            this.veinSize = veinSize;
            this.maxYLevel = maxYLevel;
        }

        public OrePartParams(UniformInt xpDropped, MaterialOreSet set) {
            this(xpDropped, set, false, 0, 0, 0);
        }

        public OrePartParams(UniformInt xpDropped, MaterialOreSet set, int veinsPerChunk, int veinSize, int maxYLevel) {
            this(xpDropped, set, true, veinsPerChunk, veinSize, maxYLevel);
        }
    }

}
