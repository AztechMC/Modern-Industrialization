package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.pipes.MIPipes;

import java.util.function.Function;

public class CableMaterialPart extends PipeMaterialPart {
    private final CableTier tier;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(CableTier tier) {
        return ctx -> new CableMaterialPart(ctx.getMaterialName(), tier, ctx.getColor());
    }

    protected CableMaterialPart(String material, CableTier tier, int color) {
        super(material, PipeType.CABLE, color);
        this.tier = tier;
    }

    @Override
    public void register() {
        MIPipes.INSTANCE.registerElectricityPipeType(materialName, color, tier);
    }
}
