package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.part.CableMaterialPart;
import aztech.modern_industrialization.materials.part.ExternalPart;
import aztech.modern_industrialization.materials.part.PipeMaterialPart;
import aztech.modern_industrialization.materials.part.PipeType;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import aztech.modern_industrialization.materials.recipe.builder.MIRecipeBuilder;
import aztech.modern_industrialization.materials.recipe.builder.SmeltingRecipeBuilder;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;

import static aztech.modern_industrialization.materials.MaterialSet.*;
import static aztech.modern_industrialization.materials.part.MIParts.*;

// TODO: register ALL MATERIALS in this class
public class MIMaterials {
    public static void init() {
        addMaterials();
        addExtraTags();
    }

    private static void addMaterials() {
        MaterialRegistry.addMaterial(
                new MaterialBuilder("gold", SHINY, 0xFFE650)
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
                new MaterialBuilder("iron", METALLIC, 0xC8C8C8)
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
                        .addRegularParts(DUST, TINY_DUST)
                        .overridePart(ExternalPart.of("dust", "minecraft:redstone", "minecraft:redstone"))
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .cancelRecipes("macerator/crushed_dust")
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("copper", SHINY, 0xC8C8C8)
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
                new MaterialBuilder("tin", DULL, 0xDCDCDC)
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
                new MaterialBuilder("steel", METALLIC, 0xDCDCDC)
                        .addRegularParts(ITEM_ALL)
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );

        /* TODO
        MaterialRegistry.addMaterial(
                new MaterialBuilder("lignite_coal", STONE, 0x644646)
                        .addRegularParts(ITEM_PURE_NON_METAL)
                        .removeRegularParts(BLOCK)
                        .addRegularParts(ORE)
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .addRecipes(context -> {
                            new MIRecipeBuilder(context, "compressor", "lignite_coal").addTaggedPartInput("dust", 1).addOutput("modern_industrialization:lignite_coal", 1);
                            new MIRecipeBuilder(context, "macerator", "dust").addItemInput("modern_industrialization:lignite_coal", 1).addPartOutput(DUST, 1);
                        })
                        .build()
        );*/

        MaterialRegistry.addMaterial(
                new MaterialBuilder("aluminum", METALLIC, 0x80C8F0)
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
                new MaterialBuilder("battery_allow", DULL, 0x9C7CA0)
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
                        .addRegularParts(PURE_METAL)
                        .addRegularParts(ORE, BLOCK)
                        .addRecipes(StandardRecipes::apply, SmeltingRecipes::apply)
                        .build()
        );

        MaterialRegistry.addMaterial(
                new MaterialBuilder("nickel", METALLIC, 0xFAFAC8)
                        .addRegularParts(ITEM_BASE)
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
                        .addRecipes(StandardRecipes::apply)
                        .build()
        );

        //TODO : QUARTZ with GEM

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
