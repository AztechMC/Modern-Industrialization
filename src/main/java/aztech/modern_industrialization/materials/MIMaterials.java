package aztech.modern_industrialization.materials;

import aztech.modern_industrialization.materials.part.ExternalPart;
import aztech.modern_industrialization.materials.part.MaterialPart;
import aztech.modern_industrialization.materials.part.PipeMaterialPart;
import aztech.modern_industrialization.materials.part.PipeType;

import static aztech.modern_industrialization.materials.MaterialSet.SHINY;

// TODO: register ALL MATERIALS in this class
public class MIMaterials {
    public static void init() {
        addMaterials();
        addRecipes();
        registerMaterials();
        registerRecipes();
    }

    public static final String[] ITEM_BASE = new String[]{"ingot", "plate", "large_plate", "nugget", "double_ingot",
            "tiny_dust", "dust", "curved_plate", "crushed_dust"};

    private static void addMaterials() {
        MaterialRegistry.addMaterial(
                new MaterialBuilder("gold", SHINY, 0xFFE650)
                        .addRegularItemParts(ITEM_BASE)
                        .overridePart(ExternalPart.of("ingot", "#c:gold_ingots", "minecraft:gold_ingot"))
                        .overridePart(ExternalPart.of("nugget", "#c:gold_nuggets", "minecraft:gold_nugget"))
                        .addParts(ExternalPart.of("ore", "#c:gold_ores", "minecraft:gold_ore"))
                        .addParts(PipeMaterialPart.of(PipeType.ITEM))
                        .addParts(PipeMaterialPart.of(PipeType.FLUID))
                        .build()
        );
    }

    private static void addRecipes() {}

    private static void registerMaterials() {
        for (Material material : MaterialRegistry.MATERIALS.values()) {
            for (MaterialPart part : material.parts.values()) {
                part.register();
            }
        }
    }

    private static void registerRecipes() {

    }
}
