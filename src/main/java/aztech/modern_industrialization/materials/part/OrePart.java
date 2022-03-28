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

import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.textures.TextureHelper;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.devtech.arrp.json.loot.*;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OrePart extends UnbuildablePart<OrePart.OrePartParams> {

    public final boolean deepslate;

    public final static Set<String> GENERATED_MATERIALS = new HashSet<>();

    public BuildablePart of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(UniformInt.of(0, 2), set, veinsPerChunk, veinSize, maxYLevel));
    }

    public BuildablePart of(UniformInt xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public BuildablePart of(UniformInt xpProvider, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set));
    }

    public BuildablePart of(MaterialOreSet set) {
        return of(new OrePartParams(UniformInt.of(0, 0), set));
    }

    public OrePart(boolean deepslate) {
        super(deepslate ? "ore_deepslate" : "ore");
        this.deepslate = deepslate;
    }

    @Override
    public BuildablePart of(OrePartParams oreParams) {
        return new RegularPart(key).withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            MIBlock block = new OreBlock(itemPath, FabricBlockSettings.of(STONE_MATERIAL)
                    .destroyTime(deepslate ? 4.5f : 3.0f).explosionResistance(3.0f)
                    .sound(deepslate ? SoundType.DEEPSLATE : SoundType.STONE).requiresCorrectToolForDrops(),
                    oreParams, partContext.getMaterialName());
            block.setPickaxeMineable().setMiningLevel(1);

            Part mainPart = partContext.getMainPart();
            String loot;
            if (mainPart.equals(MIParts.INGOT)) {
                loot = registeringContext.getMaterialPart(MIParts.RAW_METAL).getItemId();
            } else if (mainPart.equals(MIParts.DUST)) {
                loot = registeringContext.getMaterialPart(MIParts.DUST).getItemId();
            } else if (mainPart.equals(MIParts.GEM)) {
                loot = registeringContext.getMaterialPart(MIParts.GEM).getItemId();
            } else {
                throw new UnsupportedOperationException("Could not find matching main part.");
            }

            // Sanity check: Ensure that ores don't drop xp, iff the main part is an ingot
            // (i.e. the drop is raw ore).
            if (mainPart.equals(MIParts.INGOT) != (oreParams.xpDropped.getMaxValue() == 0)) {
                throw new IllegalArgumentException("Mismatch between raw ore and xp drops for material: " + partContext.getMaterialName());
            }

            block.setLootTables(JLootTable.loot("minecraft:block")
                    .pool(new JPool().rolls(1).bonus(0).entry(new JEntry().type("minecraft:alternatives").child(new JEntry().type("minecraft:item")
                            .condition(new JCondition("minecraft:match_tool").parameter("predicate", new Gson().fromJson("""
                                    {
                                    "enchantments": [
                                      {
                                        "enchantment": "minecraft:silk_touch",
                                        "levels": {
                                          "min": 1
                                        }
                                      }
                                    ]
                                    }
                                    """, JsonElement.class))).name(itemId)

                    ).child(new JEntry().type("minecraft:item").function(new JFunction("minecraft:apply_bonus")
                            .parameter("enchantment", "minecraft:fortune").parameter("formula", "minecraft:ore_drops"))
                            .function(new JFunction("minecraft:explosion_decay")).name(loot)))));

            MIItemTagProvider.generateTag("c:" + partContext.getMaterialName() + "_ores", block.blockItem);

            MIConfig config = MIConfig.getConfig();

            if (oreParams.generate) {
                GENERATED_MATERIALS.add(partContext.getMaterialName());
                if (config.generateOres && !config.blacklistedOres.contains(partContext.getMaterialName())) {
                    // TODO 1.18

                    ResourceLocation oreGenId = new MIIdentifier((deepslate ? "deepslate_" : "") + "ore_generator_" + partContext.getMaterialName());

                    var target = ImmutableList.of(OreConfiguration.target(
                            deepslate ? OreFeatures.DEEPSLATE_ORE_REPLACEABLES : OreFeatures.STONE_ORE_REPLACEABLES,
                            block.defaultBlockState()));

                    var configuredOreGen = BuiltinRegistries.register(
                            BuiltinRegistries.CONFIGURED_FEATURE, oreGenId,
                            new ConfiguredFeature<>(Feature.ORE, new OreConfiguration(target, oreParams.veinSize)));

                    Registry.register(
                            BuiltinRegistries.PLACED_FEATURE,
                            oreGenId,
                            new PlacedFeature(configuredOreGen,
                                    OrePlacements.commonOrePlacement(
                                            oreParams.veinsPerChunk,
                                            HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.absolute(oreParams.maxYLevel)))));

                    var featureKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, oreGenId);
                    BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, featureKey);

                }
            }

        }).withTextureRegister((mtm, partContext, part, itemPath) -> {
            String template = String.format("modern_industrialization:textures/materialsets/ores/%s.png", oreParams.set.name);
            try {

                String from =

                        switch (oreParams.set) {
                case IRON -> deepslate ? "deepslate_iron_ore" : "iron_ore";
                case COPPER -> deepslate ? "deepslate_copper_ore" : "copper_ore";
                case LAPIS -> deepslate ? "deepslate_lapis_ore" : "lapis_ore";
                case REDSTONE -> deepslate ? "deepslate" : "redstone_ore";
                case DIAMOND -> deepslate ? "deepslate" : "diamond_ore";
                case GOLD -> deepslate ? "deepslate_gold_ore" : "gold_ore";
                case EMERALD -> deepslate ? "deepslate_emerald_ore" : "emerald_ore";
                case COAL -> deepslate ? "deepslate_coal_ore" : "coal_ore";
                default -> deepslate ? "deepslate" : "stone";
                };

                NativeImage image = mtm.getAssetAsTexture(String.format("minecraft:textures/block/%s.png", from));
                NativeImage top = mtm.getAssetAsTexture(template);
                TextureHelper.colorize(top, partContext.getColoramp());
                String texturePath = String.format("modern_industrialization:textures/blocks/%s.png", itemPath);
                mtm.addTexture(texturePath, TextureHelper.blend(image, top), true);
                top.close();
                image.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).withCustomFormattablePath((deepslate ? "deepslate_" : "") + "%s_ore", "%s_ores");
    }

    public List<BuildablePart> ofAll(OrePartParams params) {
        return List.of(MIParts.ORE_DEEPLSATE.of(params), MIParts.ORE.of(params));
    }

    public List<BuildablePart> ofAll(UniformInt xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return ofAll(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public List<BuildablePart> ofAll(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
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
