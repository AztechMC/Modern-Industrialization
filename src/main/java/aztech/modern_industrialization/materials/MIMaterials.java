package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.ExternalPart;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.materials.part.PipeMaterialPart;
import aztech.modern_industrialization.materials.part.PipeType;
import aztech.modern_industrialization.materials.recipe.ForgeHammerRecipes;
import aztech.modern_industrialization.materials.recipe.SmeltingRecipes;
import aztech.modern_industrialization.materials.recipe.StandardRecipes;

import static aztech.modern_industrialization.materials.MaterialSet.METALLIC;
import static aztech.modern_industrialization.materials.MaterialSet.SHINY;
import static aztech.modern_industrialization.materials.part.MIParts.ITEM_BASE;

// TODO: register ALL MATERIALS in this class
public class MIMaterials {
    public static void init() {
        addMaterials();
    }

    private static void addMaterials() {
        // TODO: forge hammer
        MaterialRegistry.addMaterial(
                new MaterialBuilder("gold", SHINY, 0xFFE650)
                        .addRegularParts(ITEM_BASE)
                        .overridePart(ExternalPart.of("ingot", "#c:gold_ingots", "minecraft:gold_ingot"))
                        .overridePart(ExternalPart.of("nugget", "#c:gold_nuggets", "minecraft:gold_nugget"))
                        .addParts(ExternalPart.of("ore", "#c:gold_ores", "minecraft:gold_ore"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );
        MaterialRegistry.addMaterial(
                new MaterialBuilder("iron", METALLIC, 0xC8C8C8)
                        .addRegularParts(ITEM_BASE)
                        .overridePart(ExternalPart.of("ingot", "#c:iron_ingots", "minecraft:iron_ingot"))
                        .overridePart(ExternalPart.of("nugget", "#c:iron_nuggets", "minecraft:iron_nugget"))
                        .addParts(ExternalPart.of("ore", "#c:iron_ores", "minecraft:iron_ore"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .addRecipes(ForgeHammerRecipes::apply, SmeltingRecipes::apply, StandardRecipes::apply)
                        .build()
        );
    }
}
