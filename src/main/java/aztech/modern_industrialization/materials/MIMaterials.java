package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.ExternalPart;
import aztech.modern_industrialization.materials.part.PipeMaterialPart;
import aztech.modern_industrialization.materials.part.PipeType;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;
import net.devtech.arrp.json.tags.JTag;
import net.minecraft.util.Identifier;

import static aztech.modern_industrialization.materials.MaterialSet.METALLIC;
import static aztech.modern_industrialization.materials.MaterialSet.SHINY;
import static aztech.modern_industrialization.materials.part.MIParts.ITEM_BASE;

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
                        .addParts(ExternalPart.of("ore", "#c:gold_ores", "minecraft:gold_ore"))
                        .addParts(ExternalPart.of("block", "#c:gold_blocks", "minecraft:gold_block"))
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
                        .addParts(ExternalPart.of("ore", "#c:iron_ores", "minecraft:iron_ore"))
                        .addParts(ExternalPart.of("block", "#c:iron_blocks", "minecraft:iron_block"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .cancelRecipes("craft/block_from_ingot", "craft/ingot_from_block")
                        .cancelRecipes("craft/ingot_from_nugget", "craft/nugget_from_ingot")
                        .cancelRecipes("smelting/ore_smelting", "smelting/ore_blasting")
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
    }
}
