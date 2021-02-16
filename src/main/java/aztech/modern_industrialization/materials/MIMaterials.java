package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.part.*;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;
import aztech.modern_industrialization.textures.BakableTargetColoramp;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;

import static aztech.modern_industrialization.materials.MaterialSet.*;
import static aztech.modern_industrialization.materials.MaterialSet.GEM;
import static aztech.modern_industrialization.materials.part.MIParts.*;

public class MIMaterials {
    public static void init() {
        addMaterials();
        addExtraTags();
    }

    private static void addMaterials() {
        MaterialRegistry.addMaterial(
                new MaterialBuilder("gold", SHINY,
                        new BakableTargetColoramp(0xFFE650, "modern_industrialization:textures/materialsets/shiny/ingot.png" ,
                        "minecraft:textures/item/gold_ingot.png"))
                        .addRegularParts(ITEM_BASE)
                        .overridePart(ExternalPart.of("ingot", "#c:gold_ingots", "minecraft:gold_ingot"))
                        .overridePart(ExternalPart.of("nugget", "#c:gold_nuggets", "minecraft:gold_nugget"))
                        .overridePart(ExternalPart.of("block", "#c:gold_blocks", "minecraft:gold_block"))
                        .addParts(ExternalPart.of("ore", "#c:gold_ores", "minecraft:gold_ore"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .cancelRecipes("craft/block_from_ingot", "craft/ingot_from_block")
                        .cancelRecipes("craft/ingot_from_nugget", "craft/nugget_from_ingot")
                        .cancelRecipes("smelting/ore_smelting", "smelting/ore_blasting")
                        .build()
        );
        MaterialRegistry.addMaterial(
                new MaterialBuilder("iron", METALLIC,
                        new BakableTargetColoramp(0xC8C8C8, "modern_industrialization:textures/materialsets/metallic/ingot.png",
                                "minecraft:textures/item/iron_ingot.png"))
                        .addRegularParts(ITEM_BASE)
                        .overridePart(ExternalPart.of("ingot", "#c:iron_ingots", "minecraft:iron_ingot"))
                        .overridePart(ExternalPart.of("nugget", "#c:iron_nuggets", "minecraft:iron_nugget"))
                        .overridePart(ExternalPart.of("block", "#c:iron_blocks", "minecraft:iron_block"))
                        .addParts(ExternalPart.of("ore", "#c:iron_ores", "minecraft:iron_ore"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .cancelRecipes("craft/block_from_ingot", "craft/ingot_from_block")
                        .cancelRecipes("craft/ingot_from_nugget", "craft/nugget_from_ingot")
                        .cancelRecipes("smelting/ore_smelting", "smelting/ore_blasting")
                        .build()
        );
        MaterialRegistry.addMaterial(
                new MaterialBuilder("coal", STONE, 0x282828)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .addParts(ExternalPart.of("ore", "#c:coal_ores", "minecraft:coal_ore"))
                        .overridePart(ExternalPart.of("block", "#c:coal_blocks", "minecraft:coal_block"))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .addRecipes(context -> {
                            new MIRecipeBuilder(context, "compressor", "coal").addTaggedPartInput("dust", 1).addOutput("minecraft:coal", 1);
                            new MIRecipeBuilder(context, "macerator", "dust").addItemInput("minecraft:coal", 1).addPartOutput(DUST, 1);

                        })
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("redstone", GEM, 0xd20000)
                        .addRegularParts(DUST, TINY_DUST, CRUSHED_DUST)
                        .overridePart(ExternalPart.of("dust", "minecraft:redstone", "minecraft:redstone"))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .cancelRecipes("macerator/crushed_dust")
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("quartz", GEM, 0xf0ebe4)
                        .addRegularParts(CRUSHED_DUST, MIParts.GEM, DUST, TINY_DUST)
                        .overridePart(ExternalPart.of(MIParts.GEM, "minecraft:quatz", "minecraft:quatz"))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("emerald", SHINY, 0x3FF385)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(MIParts.GEM)
                        .overridePart(ExternalPart.of(MIParts.GEM, "minecraft:emerald", "minecraft:emerald"))
                        .addRecipes(StandardRecipes::apply)
                        .addRecipes(context -> {
                            new MIRecipeBuilder(context, "macerator", "dust").addItemInput("minecraft:emerald_ore", 1).addPartOutput(CRUSHED_DUST, 2);
                        })
                        .build()
        );


        MaterialRegistry.addMaterial(
                new MaterialBuilder("copper", SHINY, 0xff6600)
                        .addRegularParts(ITEM_ALL)
                        .addRegularParts(ORE)
                        .addRegularParts(WIRE)
                        .addRegularParts(FINE_WIRE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addParts(CableMaterialPart.of(CableTier.LV))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("bronze", METALLIC, 0xffcc00)
                        .addRegularParts(ITEM_ALL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("tin", DULL, 0xcbe4e4)
                        .addRegularParts(ITEM_ALL)
                        .addRegularParts(ORE)
                        .addRegularParts(WIRE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addParts(CableMaterialPart.of(CableTier.LV))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("steel", METALLIC, 0x3f3f3f)
                        .addRegularParts(ITEM_ALL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("lignite_coal", STONE, 0x644646)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(ORE)
                        .addRegularParts(MIParts.GEM)
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .addRecipes(context -> {
                            new MIRecipeBuilder(context, "compressor", "lignite_coal").addTaggedPartInput("dust", 1).addPartOutput(MIParts.GEM, 1);
                            new MIRecipeBuilder(context, "macerator", "dust").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 1);
                            new SmeltingRecipeBuilder(context, ORE, MIParts.GEM, 0.7, true);
                            new SmeltingRecipeBuilder(context, ORE, MIParts.GEM, 0.7, false);
                        })
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("aluminum", METALLIC, 0x3fcaff)
                        .addRegularParts(ITEM_ALL)
                        .addRegularParts(WIRE, FINE_WIRE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addParts(CableMaterialPart.of(CableTier.MV))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("bauxite", DULL, 0xC86400)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(ORE)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("lead", DULL, 0x3C286E)
                        .addRegularParts(ITEM_BASE)
                        .addRegularParts(ORE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .cancelRecipes("macerator/crushed_dust")
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("battery_alloy", DULL, 0x9C7CA0)
                        .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, CURVED_PLATE, NUGGET, BLOCK)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("invar", METALLIC, 0xDCDC96)
                        .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, LARGE_PLATE, NUGGET, GEAR, BLOCK)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("cupronickel", METALLIC, 0xE39681)
                        .addRegularParts(TINY_DUST, DUST, INGOT, DOUBLE_INGOT, PLATE, WIRE, NUGGET, BLOCK, COIL)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .addParts(CableMaterialPart.of(CableTier.MV))
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("antimony", SHINY, 0xDCDCF0)
                        .addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(ORE)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("nickel", METALLIC, 0xFAFAC8)
                        .addRegularParts(ITEM_BASE)
                        .addRegularParts(ORE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("silver", SHINY, 0xDCDCFF)
                        .addRegularParts(ITEM_BASE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("sodium", STONE, 0x071CB8)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("salt", GEM, 0xc7d6c5)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(ORE)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("titanium", METALLIC, 0xDCA0F0)
                        .addRegularParts(ITEM_ALL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );


        MaterialRegistry.addMaterial(
                new MaterialBuilder("electrum", SHINY, 0xFFFF64)
                        .addRegularParts(ITEM_BASE)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRegularParts(WIRE, FINE_WIRE)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addParts(CableMaterialPart.of(CableTier.MV))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("silicon", METALLIC, 0x3C3C50)
                        .addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(PLATE, DOUBLE_INGOT)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("stainless_steel", SHINY, 0xC8C8DC)
                        .addRegularParts(ITEM_ALL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("chrome", SHINY, 0xFFE6E6)
                        .addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(PLATE, LARGE_PLATE, DOUBLE_INGOT)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("manganese", DULL, 0xC1C1C1)
                        .addRegularParts(ITEM_PURE_METAL)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("fluorite", SHINY, 0xAF69CF)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(ORE)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("sodium_fluorosilicate", DULL, 0xD1EDE5)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("sodium_fluoroberyllate", DULL, 0x77B889)
                        .addRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("beryllium_hydroxide", DULL, 0x4E875E)
                        .addRegularParts(DUST, TINY_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("beryllium_oxide", DULL, 0x54B36E)
                        .addRegularParts(DUST, TINY_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("beryllium", SHINY, 0x64B464)
                        .addRegularParts(ITEM_ALL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("annealed_copper", SHINY, 0xff924f)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRegularParts(PLATE, WIRE, DOUBLE_INGOT)
                        .addParts(CableMaterialPart.of(CableTier.HV))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("uranium", DULL, 0x39e600)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRegularParts(ORE)
                        .addRegularParts(MIParts.GEM)
                        .addRecipes(StandardRecipes::apply)
                        .addRecipes(context -> {
                                new MIRecipeBuilder(context,"macerator", "ore").addPartInput(ORE, 1).addPartOutput(MIParts.GEM, 2);
                                new MIRecipeBuilder(context,"macerator", "uranium").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 2);
                        }
                        )
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("uranium_235", SHINY, 0xe60045)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("uranium_238", DULL, 0x55bd33)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("plutonium", SHINY, 0xd701e7)
                        .addRegularParts(ITEM_PURE_METAL)
                        //.addRegularParts(ORE) if other mod
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("mox", SHINY, 0x00e7e5)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("platinum", SHINY, 0xffe5ba)
                        .addRegularParts(ITEM_PURE_METAL)
                        .addRegularParts(ORE)
                        .addRegularParts(PLATE, DOUBLE_INGOT, WIRE, FINE_WIRE)
                        .addParts(CableMaterialPart.of(CableTier.HV))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .cancelRecipes("macerator/ore")
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("kanthal", METALLIC, 0xcfcb00)
                        .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, BLOCK, COIL)
                        .addParts(CableMaterialPart.of(CableTier.HV))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("iridium", SHINY, 0xe1e6f5)
                        .addRegularParts(ITEM_PURE_METAL)
                        .removeRegularParts(CRUSHED_DUST)
                        .addRegularParts(ORE)
                        .addRegularParts(MIParts.GEM)
                        .addRecipes(StandardRecipes::apply)
                        .addRecipes(context -> {
                                    new MIRecipeBuilder(context,"macerator", "ore").addPartInput(ORE, 1).addPartOutput(MIParts.GEM, 2);
                                    new MIRecipeBuilder(context,"macerator", "iridium").addPartInput(MIParts.GEM, 1).addPartOutput(DUST, 2);
                                }
                        )
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("mozanite", STONE, 0x96248e)
                        .addRegularParts(CRUSHED_DUST, DUST, TINY_DUST, ORE)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("cadmium", DULL, 0x967224)
                        .addRegularParts(DUST, TINY_DUST, INGOT, PLATE, ROD, DOUBLE_INGOT)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("neodymium", STONE, 0x1d4506)
                        .addRegularParts(DUST, TINY_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("yttrium", STONE, 0x135166)
                        .addRegularParts(DUST, TINY_DUST)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("supraconductor", SHINY, 0xa3d9ff)
                        .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, WIRE, DOUBLE_INGOT, COIL)
                        .addParts(CableMaterialPart.of(CableTier.SUPRACONDUCTOR))
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("tungsten", METALLIC, 0x3b2817)
                        .addRegularParts(TINY_DUST, DUST, PLATE, INGOT, NUGGET, LARGE_PLATE, DOUBLE_INGOT, ROD, CRUSHED_DUST, BLOCK, ORE)
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );


    }

    /**
     * Add material tags for special parts, like vanilla stuff
     */
    private static void addExtraTags() {
        MaterialHelper.registerItemTag("c:iron_blocks", JTag.tag().add(new Identifier("minecraft:iron_block")));
        MaterialHelper.registerItemTag("c:iron_ingots", JTag.tag().add(new Identifier("minecraft:iron_ingot")));
        MaterialHelper.registerItemTag("c:iron_nuggets", JTag.tag().add(new Identifier("minecraft:iron_nugget")));
        MaterialHelper.registerItemTag("c:iron_ores", JTag.tag().add(new Identifier("minecraft:iron_ore")));

        MaterialHelper.registerItemTag("c:gold_blocks", JTag.tag().add(new Identifier("minecraft:gold_block")));
        MaterialHelper.registerItemTag("c:gold_ingots", JTag.tag().add(new Identifier("minecraft:gold_ingot")));
        MaterialHelper.registerItemTag("c:gold_nuggets", JTag.tag().add(new Identifier("minecraft:gold_nugget")));
        MaterialHelper.registerItemTag("c:gold_ores", JTag.tag().tag(new Identifier("minecraft:gold_ores")));

        MaterialHelper.registerItemTag("c:coal_ores", JTag.tag().add(new Identifier("minecraft:coal_ore")));
        MaterialHelper.registerItemTag("c:coal_blocks", JTag.tag().add(new Identifier("minecraft:coal_block")));

    }
}
