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
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.util.ResourceUtil;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.devtech.arrp.json.loot.*;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.feature.ConfiguredFeature;

public class OrePart extends UnbuildablePart<OrePart.OrePartParams> {

    public final boolean deepslate;

    public final static Set<String> GENERATED_MATERIALS = new HashSet<>();

    public BuildablePart of(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(UniformIntProvider.create(0, 2), set, veinsPerChunk, veinSize, maxYLevel));
    }

    public BuildablePart of(UniformIntProvider xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public BuildablePart of(UniformIntProvider xpProvider, MaterialOreSet set) {
        return of(new OrePartParams(xpProvider, set));
    }

    public OrePart(boolean deepslate) {
        super(deepslate ? "ore_deepslate" : "ore");
        this.deepslate = deepslate;
    }

    @Override
    public BuildablePart of(OrePartParams oreParams) {
        return new RegularPart(key).withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            MIBlock block = new OreBlock(itemPath, FabricBlockSettings.of(STONE_MATERIAL).hardness(deepslate ? 4.5f : 3.0f).resistance(3.0f)
                    .sounds(deepslate ? BlockSoundGroup.DEEPSLATE : BlockSoundGroup.STONE).breakByTool(FabricToolTags.PICKAXES, 1).requiresTool(),
                    oreParams);

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
            if (mainPart.equals(MIParts.INGOT) != (oreParams.xpDropped.getMax() == 0)) {
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

            ResourceUtil.appendToTag("c:items/" + partContext.getMaterialName() + "_ores", itemId);

            MIConfig config = MIConfig.getConfig();

            if (oreParams.generate) {
                GENERATED_MATERIALS.add(partContext.getMaterialName());
                if (config.generateOres && !config.blacklistedOres.contains(partContext.getMaterialName())) {
                    // TODO 1.18

//                    List<OreFeatureConfig.Target> targets = List
//                            .of(deepslate ? OreFeatureConfig.createTarget(OreFeatureConfig.Rules.DEEPSLATE_ORE_REPLACEABLES, block.getDefaultState())
//                                    : OreFeatureConfig.createTarget(OreFeatureConfig.Rules.STONE_ORE_REPLACEABLES, block.getDefaultState()));
//                    OreFeatureConfig oreConfig = new OreFeatureConfig(targets, oreParams.veinSize);
//                    ConfiguredFeature<?, ?> oreGenerator = Feature.ORE.configure(oreConfig)
//                            .uniformRange(YOffset.getBottom(), YOffset.fixed(oreParams.maxYLevel)).spreadHorizontally()
//                            .repeat(oreParams.veinsPerChunk);
//                    Identifier oreGenId = new MIIdentifier((deepslate ? "deepslate_" : "") + "ore_generator_" + partContext.getMaterialName());
//                    addOreGen(oreGenId, oreGenerator);
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

                NativeImage image = mtm.getAssetAsTextureLowPrio(String.format("minecraft:textures/block/%s.png", from));
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

    public static void addOreGen(Identifier oreGenId, ConfiguredFeature<?, ?> oreGenerator) {
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, oreGenId, oreGenerator);
        RegistryKey<ConfiguredFeature<?, ?>> featureKey = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, oreGenId);
        // TODO 1.18
//        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Feature.UNDERGROUND_ORES, featureKey);
    }

    public List<BuildablePart> ofAll(OrePartParams params) {
        return List.of(MIParts.ORE_DEEPLSATE.of(params), MIParts.ORE.of(params));
    }

    public List<BuildablePart> ofAll(UniformIntProvider xpProvider, int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return ofAll(new OrePartParams(xpProvider, set, veinsPerChunk, veinSize, maxYLevel));
    }

    public List<BuildablePart> ofAll(int veinsPerChunk, int veinSize, int maxYLevel, MaterialOreSet set) {
        return ofAll(new OrePartParams(UniformIntProvider.create(0, 0), set, veinsPerChunk, veinSize, maxYLevel));
    }

    public static class OrePartParams {

        public final UniformIntProvider xpDropped;
        public final MaterialOreSet set;
        public final boolean generate;

        public final int veinsPerChunk;
        public final int veinSize;
        public final int maxYLevel;

        private OrePartParams(UniformIntProvider xpDropped, MaterialOreSet set, boolean generate, int veinsPerChunk, int veinSize, int maxYLevel) {
            this.xpDropped = xpDropped;
            this.set = set;
            this.generate = generate;

            this.veinsPerChunk = veinsPerChunk;
            this.veinSize = veinSize;
            this.maxYLevel = maxYLevel;
        }

        public OrePartParams(UniformIntProvider xpDropped, MaterialOreSet set) {
            this(xpDropped, set, false, 0, 0, 0);
        }

        public OrePartParams(UniformIntProvider xpDropped, MaterialOreSet set, int veinsPerChunk, int veinSize, int maxYLevel) {
            this(xpDropped, set, true, veinsPerChunk, veinSize, maxYLevel);
        }
    }

}
