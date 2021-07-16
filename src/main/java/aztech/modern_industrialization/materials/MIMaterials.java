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
package aztech.modern_industrialization.materials;

import static aztech.modern_industrialization.materials.part.MIParts.*;
import static aztech.modern_industrialization.materials.set.MaterialOreSet.COPPER;
import static aztech.modern_industrialization.materials.set.MaterialSet.*;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.part.*;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import aztech.modern_industrialization.textures.coloramp.BakableTargetColoramp;
import aztech.modern_industrialization.util.ResourceUtil;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class MIMaterials {

    public static void init() {
        addMaterials();
        addExtraTags();
    }

    public static final String commonPath = "modern_industrialization:textures/materialsets/common/";
    public static final String templatePath = "modern_industrialization:textures/template/";
    public static final String mcPath = "minecraft:textures/item/";
    public static final String miPath = "modern_industrialization:textures/items/";

    public static String common(String name) {
        return commonPath + name + ".png";
    }

    public static String mcitem(String name) {
        return mcPath + name + ".png";
    }

    public static String miitem(String name) {
        return miPath + name + ".png";
    }

    public static String template(String name) {
        return templatePath + name + ".png";
    }

    public static MaterialBuilder addVanillaMetal(boolean nugget, MaterialBuilder builder) {
        String n = builder.getMaterialName();
        MaterialBuilder res = builder.overridePart(ExternalPart.of("ingot", "#c:" + n + "_ingots", "minecraft:" + n + "_ingot"))
                .addParts(ExternalPart.of("block", "#c:" + n + "_blocks", "minecraft:" + n + "_block"))
                .addParts(ExternalPart.of("ore", "#c:" + n + "_ores", "minecraft:" + n + "_ore"))
                .addParts(ExternalPart.of("raw_metal", "#c:raw_" + n + "_ores", "minecraft:raw_" + n))
                .addParts(ExternalPart.of("raw_metal_block", "#c:raw_" + n + "_blocks", "minecraft:raw_" + n + "_block"));

        if (nugget) {
            res.overridePart(ExternalPart.of("nugget", "#c:" + n + "_nuggets", "minecraft:" + n + "_nugget"));
        }
        res.addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                .cancelRecipes("craft/block_from_ingot", "craft/ingot_from_block")
                .cancelRecipes("craft/raw_metal_block_from_raw_metal", "craft/raw_metal_from_raw_metal_block")
                .cancelRecipes("smelting/ore_to_ingot_smelting", "smelting/ore_to_ingot_blasting")
                .cancelRecipes("smelting/raw_metal_to_ingot_smelting", "smelting/raw_metal_to_ingot_blasting");
        if (nugget) {
            res.cancelRecipes("craft/ingot_from_nugget", "craft/nugget_from_ingot");
        }

        return res;
    }

    public static MaterialBuilder addVanillaGem(boolean compressor, MaterialBuilder builder) {
        return addVanillaGem(compressor, builder.getMaterialName(), builder);
    }

    public static MaterialBuilder addVanillaGem(boolean compressor, String gemPath, MaterialBuilder builder) {
        String n = builder.getMaterialName();
        MaterialBuilder res = builder.addParts(ExternalPart.of("gem", "minecraft:" + gemPath, "minecraft:" + gemPath))
                .addParts(ExternalPart.of("block", "#c:" + n + "_blocks", "minecraft:" + n + "_block"))
                .addParts(ExternalPart.of("ore", "#c:" + n + "_ores", "minecraft:" + n + "_ore"));

        res.addRecipes(SmeltingRecipes::apply, StandardRecipes::apply).cancelRecipes("craft/block_from_gem", "craft/gem_from_block")
                .cancelRecipes("smelting/ore_to_gem_smelting", "smelting/ore_to_gem_blasting");

        if (compressor) {
            res.addRecipes(
                    context -> new MIRecipeBuilder(context, "compressor", n).addTaggedPartInput("dust", 1).addOutput("minecraft:" + gemPath, 1));
        }
        return res;
    }

    private static void addMaterials() {
        MaterialRegistry.addMaterial(addVanillaMetal(true,
                new MaterialBuilder("gold", SHINY, new BakableTargetColoramp(0xFFE650, common(INGOT), mcitem("gold_ingot")))
                        .addRegularParts(BOLT, RING, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE, TINY_DUST)
                        .addRegularParts(DRILL_HEAD, DRILL)).build());

        MaterialRegistry
                .addMaterial(
                        addVanillaMetal(true,
                                new MaterialBuilder("iron", METALLIC, new BakableTargetColoramp(0xC8C8C8, common(INGOT), mcitem("iron_ingot")))
                                        .addRegularParts(BOLT, RING, GEAR, ROD, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE, TINY_DUST))
                                                .build());

        MaterialRegistry.addMaterial(addVanillaMetal(false,
                new MaterialBuilder("copper", METALLIC, new BakableTargetColoramp(0xe77c56, common(INGOT), mcitem("copper_ingot")))
                        .addRegularParts(BOLT, BLADE, RING, ROTOR, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE,
                                TINY_DUST)
                        .addRegularParts(WIRE).addRegularParts(FINE_WIRE).addParts(CableMaterialPart.of(CableTier.LV))
                        .addRegularParts(DRILL_HEAD, DRILL)).cancelRecipes("macerator/ore_to_raw").cancelRecipes("forge_hammer_hammer/raw_metal")
                                .build());

        MaterialRegistry.addMaterial(addVanillaGem(true,
                new MaterialBuilder("coal", STONE, MIParts.GEM, new BakableTargetColoramp(0x282828, common(PLATE), mcitem("coal")))
                        .addRegularParts(ITEM_PURE_NON_METAL)).addRecipes(ForgeHammerRecipes::apply).build());

        MaterialRegistry.addMaterial(addVanillaGem(false,
                new MaterialBuilder("diamond", SHINY, MIParts.GEM, new BakableTargetColoramp(0x48eeda, mcitem("diamond"), mcitem("diamond")))
                        .addRegularParts(ITEM_PURE_NON_METAL).addRegularParts(PLATE)).build());

        MaterialRegistry.addMaterial(addVanillaGem(false,
                new MaterialBuilder("emerald", SHINY, MIParts.GEM, new BakableTargetColoramp(0x3FF385, mcitem("emerald"), mcitem("emerald")))
                        .addRegularParts(ITEM_PURE_NON_METAL).addRegularParts(PLATE)).build());

        MaterialRegistry.addMaterial(addVanillaGem(true, "lapis_lazuli",
                new MaterialBuilder("lapis", DULL, MIParts.GEM, new BakableTargetColoramp(0x1A2D8D, common("dust"), mcitem("lapis_lazuli")))
                        .addRegularParts(ITEM_PURE_NON_METAL).addRegularParts(PLATE)).cancelRecipes("macerator/ore_to_crushed").build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("redstone", STONE, DUST, new BakableTargetColoramp(0xd20000, common(DUST), mcitem("redstone")))
                        .addRegularParts(TINY_DUST, CRUSHED_DUST, BATTERY)
                        .addParts(ExternalPart.of("dust", "minecraft:redstone", "minecraft:redstone"))
                        .addParts(ExternalPart.of("block", "#c:redstone_blocks", "minecraft:redstone_block"))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("quartz", STONE, MIParts.GEM, new BakableTargetColoramp(0xf0ebe4, mcitem("quartz"), mcitem("quartz")))
                        .addRegularParts(CRUSHED_DUST, DUST, TINY_DUST)
                        .addParts(OreMaterialPart.of(MaterialOreSet.QUARTZ, UniformIntProvider.create(2, 5)))
                        .addParts(ExternalPart.of(MIParts.GEM, "minecraft:quartz", "minecraft:quartz")).addRecipes(StandardRecipes::apply)
                        .cancelRecipes("macerator/ore_to_crushed").addRecipes(context -> new MIRecipeBuilder(context, "compressor", "quartz")
                                .addTaggedPartInput("dust", 1).addOutput("minecraft:quartz", 1))
                        .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("brick", STONE, new BakableTargetColoramp(0xb75a36, common("ingot"), mcitem("brick")))
                .addRegularParts(DUST, TINY_DUST).addParts(ExternalPart.of(MIParts.INGOT, "minecraft:brick", "minecraft:brick"))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("fire_clay", STONE, new BakableTargetColoramp(0xb75a36, common("ingot"), miitem("fire_clay_brick")))
                        .addRegularParts(DUST).addParts(MIItemPart.of(INGOT, "fire_clay_brick")).addRecipes(SmeltingRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("coke", STONE, MIParts.GEM, new BakableTargetColoramp(0x6d6d57, common("dust"), miitem("coke")))
                        .addRegularParts(DUST).addParts(MIItemPart.of(GEM, "coke")).addParts(BlockMaterialPart.of(MaterialBlockSet.COAL))
                        .addRecipes(context -> new MIRecipeBuilder(context, "macerator", "coke").addTaggedPartInput(MIParts.GEM, 1)
                                .addPartOutput(DUST, 1))
                        .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("bronze", SHINY, new BakableTargetColoramp(0xffcc00, common("ingot"), template("bronze_ingot")))
                        .addRegularParts(BOLT, BLADE, RING, ROTOR, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE,
                                TINY_DUST)
                        .removeRegularParts(CRUSHED_DUST).addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(TankMaterialPart.of(4))
                        .addRegularParts(DRILL_HEAD, DRILL)
                        .addParts(CasingMaterialPart.of(MACHINE_CASING), CasingMaterialPart.of(MACHINE_CASING_PIPE))
                        .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "bronze_plated_bricks"))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("tin", DULL, new BakableTargetColoramp(0xc0bcd0, common("ingot"), template("tin_ingot")))
                .addRegularParts(BOLT, BLADE, RING, ROTOR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE, TINY_DUST)
                .addParts(OreGenMaterialPart.of(8, 9, 64, MaterialOreSet.IRON)).addRegularParts(WIRE).addParts(RawMetalPart.of(MaterialRawSet.GOLD))
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(CableMaterialPart.of(CableTier.LV))
                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("steel", METALLIC, new BakableTargetColoramp(0x3f3f3f, common("ingot"), template("steel_ingot")))
                        .addRegularParts(BOLT, RING, ROD, GEAR, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE, TINY_DUST)
                        .addRegularParts(ROD_MAGNETIC).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRegularParts(DRILL_HEAD, DRILL)
                        .addParts(CasingMaterialPart.of(MACHINE_CASING), CasingMaterialPart.of(MACHINE_CASING_PIPE)).addParts(TankMaterialPart.of(8))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("lignite_coal", STONE, MIParts.GEM, 0x644646).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COAL)).addParts(GemMaterialPart.of())
                .addParts(OreGenMaterialPart.of(10, 17, 128, MaterialOreSet.COAL, UniformIntProvider.create(0, 2)))
                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).cancelRecipes("macerator/crushed_dust")
                .addRecipes(context -> new MIRecipeBuilder(context, "compressor", "lignite_coal").addTaggedPartInput("dust", 1)
                        .addPartOutput(MIParts.GEM, 1))
                .build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("aluminum", METALLIC, new BakableTargetColoramp(0x3fcaff, common("ingot"), template("aluminum_ingot")))
                        .addRegularParts(BOLT, BLADE, RING, ROTOR, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE,
                                TINY_DUST)
                        .addRegularParts(WIRE).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                        .addParts(CasingMaterialPart.of(MACHINE_CASING, "advanced_machine_casing")).addRegularParts(DRILL_HEAD, DRILL)
                        .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "frostproof_machine_casing")).addParts(TankMaterialPart.of(16))
                        .addParts(CableMaterialPart.of(CableTier.HV)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("bauxite", DULL, DUST, 0xC86400).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.LAPIS))
                .addParts(OreGenMaterialPart.of(8, 7, 32, MaterialOreSet.REDSTONE, UniformIntProvider.create(1, 4)))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("lead", DULL, new BakableTargetColoramp(0x6a76bc, common("ingot"), template("lead_ingot")))
                .addRegularParts(DOUBLE_INGOT, DUST, INGOT, NUGGET, PLATE, TINY_DUST).addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER))
                .addParts(OreGenMaterialPart.of(4, 8, 64, MaterialOreSet.IRON)).addParts(RawMetalPart.of(MaterialRawSet.IRON))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).cancelRecipes("macerator/raw_metal").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("battery_alloy", DULL, 0x9C7CA0)
                .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, CURVED_PLATE, NUGGET, LARGE_PLATE)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("invar", METALLIC, 0xDCDC96).addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "heatproof_machine_casing"))
                        .addRegularParts(TINY_DUST, DUST, INGOT, ROD, DOUBLE_INGOT, RING, BOLT, PLATE, LARGE_PLATE, NUGGET, GEAR)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("cupronickel", METALLIC, 0xE39681)
                .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, WIRE, NUGGET, COIL, WIRE_MAGNETIC)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(CableMaterialPart.of(CableTier.MV))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("antimony", SHINY, 0xDCDCF0).addRegularParts(ITEM_PURE_METAL).addParts(RawMetalPart.of(MaterialRawSet.COPPER))
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addParts(OreGenMaterialPart.of(4, 6, 64, MaterialOreSet.REDSTONE))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("nickel", METALLIC, 0xFAFAC8)
                .addRegularParts(DOUBLE_INGOT, DUST, INGOT, NUGGET, PLATE, TINY_DUST).addParts(RawMetalPart.of(MaterialRawSet.IRON))
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addParts(OreGenMaterialPart.of(7, 6, 64, MaterialOreSet.IRON))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("silver", SHINY, new BakableTargetColoramp(0xDCDCFF, common("ingot"), template("silver_ingot")))
                        .addParts(RawMetalPart.of(MaterialRawSet.GOLD)).addParts(CableMaterialPart.of(CableTier.LV))
                        .addRegularParts(DOUBLE_INGOT, DUST, INGOT, NUGGET, PLATE, TINY_DUST, WIRE)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("sodium", STONE, DUST, 0x071CB8).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.LAPIS)).addRegularParts(BATTERY).removeRegularParts(CRUSHED_DUST)
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("salt", STONE, DUST, 0xc7d6c5).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE))
                .addParts(OreGenMaterialPart.of(4, 8, 64, MaterialOreSet.COAL, UniformIntProvider.create(1, 3))).addRecipes(StandardRecipes::apply)
                .build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("titanium", METALLIC, new BakableTargetColoramp(0xDCA0F0, common("ingot"), template("titanium_ingot")))
                        .addRegularParts(BOLT, BLADE, RING, ROTOR, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE,
                                TINY_DUST)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE)).addParts(RawMetalPart.of(MaterialRawSet.COPPER))
                        .addRegularParts(HOT_INGOT).addParts(CasingMaterialPart.of(MACHINE_CASING, "highly_advanced_machine_casing"))
                        .addRegularParts(DRILL_HEAD, DRILL).addParts(CasingMaterialPart.of(MACHINE_CASING_PIPE))
                        .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "solid_titanium_machine_casing"))
                        .addParts(OreGenMaterialPart.of(3, 6, 20, MaterialOreSet.IRON)).addParts(TankMaterialPart.of(64))
                        .addRecipes(StandardRecipes::apply).cancelRecipes("macerator/raw_metal").build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("electrum", SHINY, new BakableTargetColoramp(0xFFFF64, common("ingot"), template("electrum_ingot")))
                        .addRegularParts(DOUBLE_INGOT, DUST, INGOT, NUGGET, PLATE, TINY_DUST).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                        .addRegularParts(WIRE, FINE_WIRE).addParts(CableMaterialPart.of(CableTier.MV))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("silicon", METALLIC, 0x3C3C50).addRegularParts(ITEM_PURE_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRegularParts(N_DOPED_PLATE, P_DOPED_PLATE)
                .addRegularParts(PLATE, DOUBLE_INGOT, BATTERY).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("stainless_steel", SHINY, new BakableTargetColoramp(0xC8C8DC, common("ingot"), template("stainless_steel_ingot")))
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                        .addRegularParts(BOLT, BLADE, RING, ROTOR, GEAR, ROD, CURVED_PLATE, DOUBLE_INGOT, DUST, INGOT, LARGE_PLATE, NUGGET, PLATE,
                                TINY_DUST)
                        .addRegularParts(HOT_INGOT).addRegularParts(DRILL_HEAD, DRILL)
                        .addParts(CasingMaterialPart.of(MACHINE_CASING, "turbo_machine_casing")).addParts(CasingMaterialPart.of(MACHINE_CASING_PIPE))
                        .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "clean_stainless_steel_machine_casing")).addRegularParts(ROD_MAGNETIC)
                        .addParts(TankMaterialPart.of(32)).addRecipes(StandardRecipes::apply).cancelRecipes("polarizer/rod_magnetic").build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("ruby", SHINY, 0xd1001f).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("carbon", DULL, 0x222222).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("chrome", SHINY, new BakableTargetColoramp(0xFFE6E6, common("ingot"), template("chrome_ingot")))
                        .addRegularParts(CRUSHED_DUST).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(HOT_INGOT).addRegularParts(PLATE, LARGE_PLATE, DOUBLE_INGOT).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("manganese", DULL, 0xC1C1C1).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                .addRegularParts(ITEM_PURE_METAL).addRegularParts(CRUSHED_DUST).addRecipes(StandardRecipes::apply)
                .cancelRecipes("macerator/crushed_dust").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("fluorite", SHINY, DUST, 0xAF69CF).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE))
                .addParts(OreGenMaterialPart.of(3, 8, 32, MaterialOreSet.REDSTONE, UniformIntProvider.create(1, 4)))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("beryllium", SHINY, 0x64B464).addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE))
                .addRegularParts(DOUBLE_INGOT, DUST, INGOT, NUGGET, PLATE, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("annealed_copper", SHINY, 0xff924f).addRegularParts(ITEM_PURE_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addRegularParts(PLATE, WIRE, DOUBLE_INGOT, HOT_INGOT)
                .addParts(CableMaterialPart.of(CableTier.EV)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("uranium", DULL, 0x39e600).addParts(NuclearFuelMaterialPart.of(2800, 0.05, 0.5, 1, 256000))
                .addRegularParts(ITEM_PURE_METAL).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addParts(OreGenMaterialPart.of(3, 6, 20, COPPER)).addParts(RawMetalPart.ofItemOnly(MaterialRawSet.URANIUM))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("uranium_235", SHINY, 0xe60045).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addRegularParts(ITEM_PURE_METAL).addParts(NuclearFuelMaterialPart.of(2500, 2.0, 0.25, 25, 256000)).addRecipes(StandardRecipes::apply)
                .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("uranium_238", DULL, 0x55bd33).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addRegularParts(ITEM_PURE_METAL).addParts(NuclearFuelMaterialPart.of(3200, 0, 0.55, 0, 256000)).addRecipes(StandardRecipes::apply)
                .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("plutonium", SHINY, 0xd701e7).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addRegularParts(ITEM_PURE_METAL).addParts(NuclearFuelMaterialPart.of(1700, 2.5, 0.2, 40, 256000))

                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("mox", SHINY, 0x00e7e5).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addParts(NuclearFuelMaterialPart.of(2800, 1.5, 0.4, 35, 256000)).addParts().addRegularParts(ITEM_PURE_METAL)
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("platinum", SHINY, new BakableTargetColoramp(0xffe5ba, common("ingot"), template("platinum_ingot")))
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addParts(RawMetalPart.of(MaterialRawSet.GOLD))
                        .addParts(OreGenMaterialPart.of(2, 6, 32, MaterialOreSet.GOLD)).addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(PLATE, DOUBLE_INGOT, WIRE, FINE_WIRE, HOT_INGOT).addParts(CableMaterialPart.of(CableTier.EV))
                        .addRecipes(StandardRecipes::apply).cancelRecipes("macerator/raw_metal").build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("kanthal", METALLIC, new BakableTargetColoramp(0xcfcb00, common("ingot"), template("kanthal_ingot")))
                        .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, COIL, HOT_INGOT)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(CableMaterialPart.of(CableTier.HV))
                        .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("iridium", SHINY, new BakableTargetColoramp(0xe1e6f5, common("ingot"), template("iridium_ingot")))
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.DIAMOND)).addRegularParts(ITEM_PURE_METAL)
                        .addParts(OreGenMaterialPart.of(2, 1, 16, MaterialOreSet.DIAMOND)).addParts(RawMetalPart.ofItemOnly(MaterialRawSet.IRIDIUM))
                        .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("mozanite", STONE, DUST, 0x96248e).addRegularParts(CRUSHED_DUST, DUST, TINY_DUST)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE))
                .addParts(OreGenMaterialPart.of(6, 3, 24, MaterialOreSet.LAPIS, UniformIntProvider.create(1, 4))).addRecipes(StandardRecipes::apply)
                .build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("cadmium", DULL, 0x967224).addRegularParts(DUST, TINY_DUST, INGOT, PLATE, ROD, DOUBLE_INGOT, BATTERY)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("neodymium", STONE, DUST, 0x1d4506).addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE))
                .addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("yttrium", STONE, DUST, 0x135166).addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE))
                .addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("supraconductor", SHINY, 0xa3d9ff)
                .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, COIL)
                .addParts(CableMaterialPart.of(CableTier.SUPRACONDUCTOR)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("tungsten", METALLIC, new BakableTargetColoramp(0x8760ad, common("ingot"), template("tungsten_ingot")))
                        .addParts(RawMetalPart.of(MaterialRawSet.COPPER))
                        .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, LARGE_PLATE, DOUBLE_INGOT, ROD)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE)).addParts(OreGenMaterialPart.of(3, 4, 20, MaterialOreSet.IRON))
                        .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("blastproof_alloy", METALLIC, 0x524c3a).addRegularParts(PLATE, LARGE_PLATE)
                .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "blastproof_casing", 6000f)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("nuclear_alloy", METALLIC, 0x3d4d32).addRegularParts(PLATE, LARGE_PLATE)
                .addParts(CasingMaterialPart.of(MACHINE_CASING_SPECIAL, "nuclear_casing", 6000f))
                .addParts(CasingMaterialPart.of(MACHINE_CASING_PIPE, 6000f)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("soldering_alloy", DULL, DUST, 0xffabc4bf).addRegularParts(DUST, TINY_DUST)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("sulfur", DULL, DUST, 0xddb614).addRegularParts(DUST, TINY_DUST)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE)).addRecipes(StandardRecipes::apply).build());

    }

    /**
     * Add material tags for special parts, like vanilla stuff
     */
    private static void addExtraTags() {
        MaterialHelper.registerItemTag("c:iron_blocks", JTag.tag().add(new Identifier("minecraft:iron_block")));
        MaterialHelper.registerItemTag("c:iron_ingots", JTag.tag().add(new Identifier("minecraft:iron_ingot")));
        MaterialHelper.registerItemTag("c:iron_nuggets", JTag.tag().add(new Identifier("minecraft:iron_nugget")));
        MaterialHelper.registerItemTag("c:iron_ores", JTag.tag().tag(new Identifier("minecraft:iron_ores")));
        MaterialHelper.registerItemTag("c:raw_iron_ores", JTag.tag().add(new Identifier("minecraft:raw_iron")));
        MaterialHelper.registerItemTag("c:raw_iron_blocks", JTag.tag().add(new Identifier("minecraft:raw_iron_block")));

        JTag copperBlocks = JTag.tag().add(new Identifier("minecraft:copper_block")).add(new Identifier("minecraft:waxed_copper_block"));

        MaterialHelper.registerItemTag("c:copper_blocks", copperBlocks);
        MaterialHelper.registerItemTag("c:copper_ingots", JTag.tag().add(new Identifier("minecraft:copper_ingot")));
        MaterialHelper.registerItemTag("c:copper_ores", JTag.tag().tag(new Identifier("minecraft:copper_ores")));
        MaterialHelper.registerItemTag("c:raw_copper_ores", JTag.tag().add(new Identifier("minecraft:raw_copper")));
        MaterialHelper.registerItemTag("c:raw_copper_blocks", JTag.tag().add(new Identifier("minecraft:raw_copper_block")));

        JTag goldOres = JTag.tag().tag(new Identifier("minecraft:gold_ores")).add(new Identifier("minecraft:gilded_blackstone"));

        MaterialHelper.registerItemTag("c:gold_blocks", JTag.tag().add(new Identifier("minecraft:gold_block")));
        MaterialHelper.registerItemTag("c:gold_ingots", JTag.tag().add(new Identifier("minecraft:gold_ingot")));
        MaterialHelper.registerItemTag("c:gold_nuggets", JTag.tag().add(new Identifier("minecraft:gold_nugget")));
        MaterialHelper.registerItemTag("c:gold_ores", goldOres);
        MaterialHelper.registerItemTag("c:raw_gold_ores", JTag.tag().add(new Identifier("minecraft:raw_gold")));
        MaterialHelper.registerItemTag("c:raw_gold_blocks", JTag.tag().add(new Identifier("minecraft:raw_gold_block")));

        MaterialHelper.registerItemTag("c:coal_ores", JTag.tag().add(new Identifier("minecraft:deepslate_coal_ore")));
        MaterialHelper.registerItemTag("c:coal_blocks", JTag.tag().add(new Identifier("minecraft:coal_block")));
        MaterialHelper.registerItemTag("c:coal_ores", JTag.tag().tag(new Identifier("minecraft:coal_ores")));

        MaterialHelper.registerItemTag("c:redstone_ores", JTag.tag().tag(new Identifier("minecraft:redstone_ores")));
        MaterialHelper.registerItemTag("c:redstone_blocks", JTag.tag().add(new Identifier("minecraft:redstone_block")));

        MaterialHelper.registerItemTag("c:emerald_ores", JTag.tag().tag(new Identifier("minecraft:emerald_ores")));
        MaterialHelper.registerItemTag("c:emerald_blocks", JTag.tag().add(new Identifier("minecraft:emerald_block")));

        MaterialHelper.registerItemTag("c:diamond_ores", JTag.tag().tag(new Identifier("minecraft:diamond_ores")));
        MaterialHelper.registerItemTag("c:diamond_blocks", JTag.tag().add(new Identifier("minecraft:diamond_block")));

        MaterialHelper.registerItemTag("c:lapis_ores", JTag.tag().tag(new Identifier("minecraft:lapis_ores")));
        MaterialHelper.registerItemTag("c:lapis_blocks", JTag.tag().add(new Identifier("minecraft:lapis_block")));

        ResourceUtil.appendToTag("c:items/quartz_ores", "minecraft:nether_quartz_ore");

    }
}
