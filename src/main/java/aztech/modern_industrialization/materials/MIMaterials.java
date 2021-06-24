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
import static aztech.modern_industrialization.materials.set.MaterialOreSet.*;
import static aztech.modern_industrialization.materials.set.MaterialSet.*;
import static aztech.modern_industrialization.materials.set.MaterialSet.GEM;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.part.*;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;
import aztech.modern_industrialization.materials.set.MaterialBlockSet;
import aztech.modern_industrialization.materials.set.MaterialOreSet;
import aztech.modern_industrialization.materials.set.MaterialRawSet;
import aztech.modern_industrialization.textures.coloramp.BakableTargetColoramp;
import aztech.modern_industrialization.util.ResourceUtil;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;

public class MIMaterials {

    public static void init() {
        addMaterials();
        addExtraTags();
    }

    public static MaterialBuilder addVanillaMetal(boolean nugget, MaterialBuilder builder) {
        String n = builder.getMaterialName();
        MaterialBuilder res = builder.overridePart(ExternalPart.of("ingot", "#c:" + n + "_ingots", "minecraft:" + n + "_ingot"))
                .addParts(ExternalPart.of("block", "#c:" + n + "_blocks", "minecraft:" + n + "_block"))
                .addParts(ExternalPart.of("ore", "#c:" + n + "_ores", "minecraft:" + n + "_ore"))
                .addParts(ExternalPart.of("raw_metal", "#c:raw_" + n + "_ores", "minecraft:raw_" + n))
                .addParts(ExternalPart.of("raw_metal_block", "#c:raw_" + n + "_blocks", "minecraft:raw_" + n + "_block"));

        if (nugget) {
            res = res.overridePart(ExternalPart.of("nugget", "#c:" + n + "_nuggets", "minecraft:" + n + "_nugget"));
        }
        res = res.addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                .cancelRecipes("craft/block_from_ingot", "craft/ingot_from_block")
                .cancelRecipes("craft/raw_metal_block_from_raw_metal", "craft/raw_metal_from_raw_metal_block")
                .cancelRecipes("smelting/ore_smelting", "smelting/ore_blasting")
                .cancelRecipes("smelting/raw_metal_smelting", "smelting/raw_metal_blasting");
        if (nugget) {
            res = res.cancelRecipes("craft/ingot_from_nugget", "craft/nugget_from_ingot");
        }

        return res;
    }

    private static void addMaterials() {
        MaterialRegistry.addMaterial(addVanillaMetal(true,
                new MaterialBuilder("gold", SHINY, new BakableTargetColoramp(0xFFE650,
                        "modern_industrialization:textures/materialsets/common/ingot.png", "minecraft:textures/item/gold_ingot.png"))
                                .addRegularParts(ITEM_BASE)).build());

        MaterialRegistry.addMaterial(addVanillaMetal(true,
                new MaterialBuilder("iron", METALLIC,
                        new BakableTargetColoramp(0xC8C8C8, "minecraft:textures/item/iron_ingot.png", "minecraft:textures/item/iron_ingot.png"))
                                .addRegularParts(ITEM_ALL)).build());

        MaterialRegistry.addMaterial(addVanillaMetal(false,
                new MaterialBuilder("copper", METALLIC,
                        new BakableTargetColoramp(0xe77c56, "modern_industrialization:textures/materialsets/common/ingot.png",
                                "minecraft:textures/item/copper_ingot.png")).addRegularParts(ITEM_ALL).addRegularParts(WIRE)
                                        .addRegularParts(FINE_WIRE).addParts(CableMaterialPart.of(CableTier.LV))).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("coal", STONE, 0x282828).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(ExternalPart.of("ore", "#c:coal_ores", "minecraft:coal_ore"))
                .addParts(ExternalPart.of("block", "#c:coal_blocks", "minecraft:coal_block"))
                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).addRecipes(context -> {
                    new MIRecipeBuilder(context, "compressor", "coal").addTaggedPartInput("dust", 1).addOutput("minecraft:coal", 1);
                    new MIRecipeBuilder(context, "macerator", "dust").addItemInput("minecraft:coal", 1).addPartOutput(DUST, 1);

                }).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("redstone", GEM,
                new BakableTargetColoramp(0xd20000, "modern_industrialization:textures/materialsets/common/dust.png",
                        "minecraft:textures/item/redstone.png")).addRegularParts(DUST, TINY_DUST, CRUSHED_DUST, BATTERY)
                                .overridePart(ExternalPart.of("dust", "minecraft:redstone", "minecraft:redstone"))
                                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).cancelRecipes("macerator/crushed_dust").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("quartz", GEM,
                new BakableTargetColoramp(0xf0ebe4, "minecraft:textures/item/quartz.png", "minecraft:textures/item/quartz.png"))
                        .addRegularParts(CRUSHED_DUST, MIParts.GEM, DUST, TINY_DUST).addParts(OreMaterialPart.of(MaterialOreSet.QUARTZ))
                        .overridePart(ExternalPart.of(MIParts.GEM, "minecraft:quartz", "minecraft:quartz")).addRecipes(StandardRecipes::apply)
                        .cancelRecipes("macerator/ore_crushed").addRecipes(ctx -> SmeltingRecipeBuilder.smeltAndBlast(ctx, ORE, MIParts.GEM, 0.2))
                        .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("diamond", SHINY,
                new BakableTargetColoramp(0x48eeda, "minecraft:textures/item/diamond.png", "minecraft:textures/item/diamond.png"))
                        .addRegularParts(ITEM_PURE_NON_METAL).addRegularParts(MIParts.GEM, PLATE)
                        .overridePart(ExternalPart.of(MIParts.GEM, "minecraft:diamond", "minecraft:diamond")).addRecipes(StandardRecipes::apply)
                        .addRecipes(context -> new MIRecipeBuilder(context, "compressor", "plate").addItemInput("minecraft:diamond", 1)
                                .addPartOutput(PLATE, 1))
                        .addRecipes(context -> new MIRecipeBuilder(context, "macerator", "dust").addItemInput("#c:diamond_ores", 1)
                                .addPartOutput(CRUSHED_DUST, 2))
                        .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("emerald", SHINY,
                new BakableTargetColoramp(0x3FF385, "minecraft:textures/item/emerald.png", "minecraft:textures/item/emerald.png"))
                        .addRegularParts(ITEM_PURE_NON_METAL).addRegularParts(MIParts.GEM, PLATE)
                        .overridePart(ExternalPart.of(MIParts.GEM, "minecraft:emerald", "minecraft:emerald")).addRecipes(StandardRecipes::apply)
                        .addRecipes(context -> new MIRecipeBuilder(context, "compressor", "plate").addItemInput("minecraft:emerald", 1)
                                .addPartOutput(PLATE, 1))
                        .addRecipes(context -> new MIRecipeBuilder(context, "macerator", "dust").addItemInput("#c:emerald_ores", 1)
                                .addPartOutput(CRUSHED_DUST, 2))
                        .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("brick", STONE,
                new BakableTargetColoramp(0xb75a36, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "minecraft:textures/item/brick.png")).addRegularParts(DUST, TINY_DUST, INGOT)
                                .overridePart(ExternalPart.of(MIParts.INGOT, "minecraft:brick", "minecraft:brick"))
                                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("fire_clay", STONE,
                new BakableTargetColoramp(0xb75a36, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/fire_clay_brick.png")).addRegularParts(DUST, INGOT)
                                .addRecipes(SmeltingRecipes::apply).build());

        MaterialRegistry
                .addMaterial(
                        new MaterialBuilder("coke", STONE,
                                new BakableTargetColoramp(0x6d6d57, "modern_industrialization:textures/materialsets/common/dust.png",
                                        "modern_industrialization:textures/items/coke.png")).addRegularParts(MIParts.GEM, DUST)
                                                .addRecipes(context -> {
                                                    new MIRecipeBuilder(context, "compressor", "coke").addTaggedPartInput("dust", 1)
                                                            .addPartOutput(MIParts.GEM, 1);
                                                    new MIRecipeBuilder(context, "macerator", "coke").addTaggedPartInput(MIParts.GEM, 1)
                                                            .addPartOutput(DUST, 1);
                                                }).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("bronze", SHINY,
                new BakableTargetColoramp(0xffcc00, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/bronze_ingot.png")).addRegularParts(ITEM_ALL).removeRegularParts(CRUSHED_DUST)
                                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(TankMaterialPart.of(4))
                                .addRegularParts(MACHINE_CASING, MACHINE_CASING_PIPE, MACHINE_CASING_SPECIAL)
                                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("tin", DULL,
                new BakableTargetColoramp(0xc0bcd0, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/tin_ingot.png")).addRegularParts(ITEM_ALL)
                                .addParts(OreGenMaterialPart.of(8, 9, 64, MaterialOreSet.IRON)).addRegularParts(WIRE)
                                .addParts(RawMetalPart.of(MaterialRawSet.GOLD)).addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER))

                                .addParts(CableMaterialPart.of(CableTier.LV))
                                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("steel", METALLIC,
                new BakableTargetColoramp(0x3f3f3f, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/steel_ingot.png")).addRegularParts(ITEM_ALL).addRegularParts(ROD_MAGNETIC)
                                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRegularParts(MACHINE_CASING, MACHINE_CASING_PIPE)
                                .addParts(TankMaterialPart.of(8))
                                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("lignite_coal", STONE, 0x644646).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COAL)).addRegularParts(MIParts.GEM)
                .addParts(OreGenMaterialPart.of(10, 17, 128, MaterialOreSet.COAL))
                .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply).addRecipes(context -> {
                    new MIRecipeBuilder(context, "compressor", "lignite_coal").addTaggedPartInput("dust", 1).addPartOutput(MIParts.GEM, 1);
                    new MIRecipeBuilder(context, "macerator", "dust").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 1);
                    new SmeltingRecipeBuilder(context, ORE, MIParts.GEM, 0.7, true);
                    new SmeltingRecipeBuilder(context, ORE, MIParts.GEM, 0.7, false);
                }).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("aluminum", METALLIC,
                new BakableTargetColoramp(0x3fcaff, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/aluminum_ingot.png")).addRegularParts(ITEM_ALL).addRegularParts(WIRE, FINE_WIRE)
                                .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRegularParts(MACHINE_CASING_SPECIAL, MACHINE_CASING)

                                .addParts(TankMaterialPart.of(16)).addParts(CableMaterialPart.of(CableTier.HV)).addRecipes(StandardRecipes::apply)
                                .build());

        MaterialRegistry.addMaterial(new MaterialBuilder("bauxite", DULL, 0xC86400).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.LAPIS)).addParts(OreGenMaterialPart.of(8, 7, 32, MaterialOreSet.REDSTONE))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("lead", DULL, 0x3C286E).addRegularParts(ITEM_BASE)
                .addParts(OreGenMaterialPart.of(4, 8, 64, MaterialOreSet.GOLD)).addParts(RawMetalPart.of(MaterialRawSet.IRON))

                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).cancelRecipes("macerator/crushed_dust").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("battery_alloy", DULL, 0x9C7CA0)
                .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, CURVED_PLATE, NUGGET, LARGE_PLATE)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("invar", METALLIC, 0xDCDC96).addRegularParts(MACHINE_CASING_SPECIAL)
                .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, LARGE_PLATE, NUGGET, GEAR)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("cupronickel", METALLIC, 0xE39681)
                .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, WIRE, NUGGET, COIL, WIRE_MAGNETIC)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(CableMaterialPart.of(CableTier.MV))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("antimony", SHINY, 0xDCDCF0).addRegularParts(ITEM_PURE_METAL)
                .addParts(RawMetalPart.of(MaterialRawSet.COPPER)).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                .addParts(OreMaterialPart.of(MaterialOreSet.LAPIS)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("nickel", METALLIC, 0xFAFAC8).addRegularParts(ITEM_BASE)
                .addParts(RawMetalPart.of(MaterialRawSet.IRON)).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                .addParts(OreGenMaterialPart.of(7, 6, 64, MaterialOreSet.IRON)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("silver", SHINY, 0xDCDCFF).addParts(RawMetalPart.of(MaterialRawSet.GOLD)).addRegularParts(ITEM_BASE)
                        .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("sodium", STONE, 0x071CB8).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.LAPIS)).addRegularParts(BATTERY).removeRegularParts(CRUSHED_DUST)
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("salt", GEM, 0xc7d6c5).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE)).addParts(OreMaterialPart.of(MaterialOreSet.COAL))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("titanium", METALLIC,
                new BakableTargetColoramp(0xDCA0F0, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/titanium_ingot.png")).addRegularParts(ITEM_ALL)
                                .addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE)).addParts(RawMetalPart.of(MaterialRawSet.COPPER))
                                .addRegularParts(HOT_INGOT, MACHINE_CASING, MACHINE_CASING_PIPE, MACHINE_CASING_SPECIAL)
                                .addParts(OreMaterialPart.of(MaterialOreSet.IRON)).addParts(TankMaterialPart.of(64))
                                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("electrum", SHINY, 0xFFFF64).addRegularParts(ITEM_BASE)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRegularParts(WIRE, FINE_WIRE).addParts(CableMaterialPart.of(CableTier.MV))
                .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("silicon", METALLIC, 0x3C3C50).addRegularParts(ITEM_PURE_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.IRON)).addRegularParts(N_DOPED_PLATE, P_DOPED_PLATE)
                .addRegularParts(PLATE, DOUBLE_INGOT, BATTERY).addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("stainless_steel", SHINY,
                new BakableTargetColoramp(0xC8C8DC, "modern_industrialization:textures/materialsets/common/ingot.png",
                        "modern_industrialization:textures/template/stainless_steel_ingot.png")).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                                .addRegularParts(ITEM_ALL).addRegularParts(HOT_INGOT)
                                .addRegularParts(MACHINE_CASING_SPECIAL, MACHINE_CASING, MACHINE_CASING_PIPE, ROD_MAGNETIC)

                                .addParts(TankMaterialPart.of(32)).addRecipes(StandardRecipes::apply).cancelRecipes("polarizer/rod_magnetic")
                                .build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("ruby", SHINY, 0xffb3b3).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("carbon", DULL, 0x222222).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("chrome", SHINY, 0xFFE6E6).addRegularParts(CRUSHED_DUST)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addRegularParts(ITEM_PURE_METAL).addRegularParts(HOT_INGOT)
                .addRegularParts(PLATE, LARGE_PLATE, DOUBLE_INGOT).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("manganese", DULL, 0xC1C1C1).addParts(BlockMaterialPart.of(MaterialBlockSet.IRON))
                .addRegularParts(ITEM_PURE_METAL).addRegularParts(CRUSHED_DUST).addRecipes(StandardRecipes::apply)
                .cancelRecipes("macerator/crushed_dust").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("fluorite", SHINY, 0xAF69CF).addRegularParts(ITEM_PURE_NON_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.REDSTONE)).addParts(OreMaterialPart.of(MaterialOreSet.REDSTONE))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("beryllium", SHINY, 0x64B464).addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE))
                .addRegularParts(ITEM_ALL).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("annealed_copper", SHINY, 0xff924f).addRegularParts(ITEM_PURE_METAL)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addRegularParts(PLATE, WIRE, DOUBLE_INGOT, HOT_INGOT)
                .addParts(CableMaterialPart.of(CableTier.EV)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("uranium", DULL, 0x39e600).addParts(NuclearFuelMaterialPart.of(2800, 0.05, 0.5, 1, 256000))
                .addRegularParts(ITEM_PURE_METAL).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD)).addParts(OreMaterialPart.of(COPPER))
                .addRegularParts(MIParts.GEM).addRecipes(StandardRecipes::apply).addRecipes(context -> {
                    new MIRecipeBuilder(context, "macerator", "ore").addTaggedPartInput(ORE, 1).addPartOutput(MIParts.GEM, 2);
                    new MIRecipeBuilder(context, "macerator", "uranium").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 2);
                }).build());

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

        MaterialRegistry.addMaterial(new MaterialBuilder("platinum", SHINY, 0xffe5ba).addParts(BlockMaterialPart.of(MaterialBlockSet.GOLD))
                .addParts(RawMetalPart.of(MaterialRawSet.GOLD)).addParts(OreMaterialPart.of(MaterialOreSet.GOLD)).addRegularParts(ITEM_PURE_METAL)
                .addRegularParts(PLATE, DOUBLE_INGOT, WIRE, FINE_WIRE, HOT_INGOT).addParts(CableMaterialPart.of(CableTier.EV))
                .addRecipes(StandardRecipes::apply).cancelRecipes("macerator/crushed_dust").build());

        MaterialRegistry.addMaterial(new MaterialBuilder("kanthal", METALLIC, 0xcfcb00)
                .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, COIL, HOT_INGOT)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.COPPER)).addParts(CableMaterialPart.of(CableTier.HV))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("iridium", SHINY, 0xe1e6f5).addParts(BlockMaterialPart.of(MaterialBlockSet.DIAMOND))
                .addRegularParts(ITEM_PURE_METAL).addParts(OreMaterialPart.of(MaterialOreSet.DIAMOND)).addRegularParts(MIParts.GEM)
                .addRecipes(StandardRecipes::apply).addRecipes(context -> {
                    new MIRecipeBuilder(context, "macerator", "ore").addTaggedPartInput(ORE, 1).addPartOutput(MIParts.GEM, 2);
                    new MIRecipeBuilder(context, "macerator", "iridium").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 2);
                }).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("mozanite", STONE, 0x96248e).addRegularParts(CRUSHED_DUST, DUST, TINY_DUST)
                .addParts(OreMaterialPart.of(MaterialOreSet.LAPIS)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry
                .addMaterial(new MaterialBuilder("cadmium", DULL, 0x967224).addRegularParts(DUST, TINY_DUST, INGOT, PLATE, ROD, DOUBLE_INGOT, BATTERY)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("neodymium", STONE, 0x1d4506).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(
                new MaterialBuilder("yttrium", STONE, 0x135166).addRegularParts(DUST, TINY_DUST).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("supraconductor", SHINY, 0xa3d9ff)
                .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, COIL)
                .addParts(CableMaterialPart.of(CableTier.SUPRACONDUCTOR)).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("tungsten", METALLIC, 0x3b2817).addParts(RawMetalPart.of(MaterialRawSet.COPPER))
                .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, LARGE_PLATE, DOUBLE_INGOT, ROD)
                .addParts(BlockMaterialPart.of(MaterialBlockSet.NETHERITE)).addParts(OreMaterialPart.of(MaterialOreSet.IRON))
                .addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("blastproof_alloy", METALLIC, 0x524c3a)
                .addRegularParts(PLATE, LARGE_PLATE, MACHINE_CASING_SPECIAL).addRecipes(StandardRecipes::apply).build());

        MaterialRegistry.addMaterial(new MaterialBuilder("nuclear_alloy", METALLIC, 0x3d4d32)
                .addRegularParts(PLATE, LARGE_PLATE, MACHINE_CASING_SPECIAL, MACHINE_CASING_PIPE).addRecipes(StandardRecipes::apply).build());

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
        MaterialHelper.registerItemTag("c:raw_i ron_blocks", JTag.tag().add(new Identifier("minecraft:raw_iron_block")));

        MaterialHelper.registerItemTag("c:copper_blocks", JTag.tag().add(new Identifier("minecraft:copper_block")));
        MaterialHelper.registerItemTag("c:copper_blocks", JTag.tag().add(new Identifier("minecraft:waxed_copper_block")));
        MaterialHelper.registerItemTag("c:copper_ingots", JTag.tag().add(new Identifier("minecraft:copper_ingot")));
        MaterialHelper.registerItemTag("c:copper_ores", JTag.tag().tag(new Identifier("minecraft:copper_ores")));
        MaterialHelper.registerItemTag("c:raw_copper_ores", JTag.tag().add(new Identifier("minecraft:raw_copper")));
        MaterialHelper.registerItemTag("c:raw_copper_blocks", JTag.tag().add(new Identifier("minecraft:raw_copper_block")));

        MaterialHelper.registerItemTag("c:gold_blocks", JTag.tag().add(new Identifier("minecraft:gold_block")));
        MaterialHelper.registerItemTag("c:gold_ingots", JTag.tag().add(new Identifier("minecraft:gold_ingot")));
        MaterialHelper.registerItemTag("c:gold_nuggets", JTag.tag().add(new Identifier("minecraft:gold_nugget")));
        ResourceUtil.appendTagToTag("c:items/gold_ores", "minecraft:gold_ores");
        ResourceUtil.appendToTag("c:items/gold_ores", "minecraft:gilded_blackstone");
        MaterialHelper.registerItemTag("c:raw_gold_ores", JTag.tag().add(new Identifier("minecraft:raw_gold")));
        MaterialHelper.registerItemTag("c:raw_gold_blocks", JTag.tag().add(new Identifier("minecraft:raw_gold_block")));

        MaterialHelper.registerItemTag("c:coal_ores", JTag.tag().add(new Identifier("minecraft:deepslate_coal_ore")));
        MaterialHelper.registerItemTag("c:coal_blocks", JTag.tag().add(new Identifier("minecraft:coal_block")));
        MaterialHelper.registerItemTag("c:coal_ores", JTag.tag().tag(new Identifier("minecraft:coal_ores")));

        MaterialHelper.registerItemTag("c:redstone_ores", JTag.tag().tag(new Identifier("minecraft:redstone_ores")));

        MaterialHelper.registerItemTag("c:emerald_ores", JTag.tag().tag(new Identifier("minecraft:emerald_ores")));

        ResourceUtil.appendTagToTag("c:items/diamond_ores", "minecraft:diamond_ores");

        MaterialHelper.registerItemTag("c:lapis_ores", JTag.tag().tag(new Identifier("minecraft:lapis_ores")));

        ResourceUtil.appendToTag("c:items/quartz_ores", "minecraft:nether_quartz_ore");

    }
}
