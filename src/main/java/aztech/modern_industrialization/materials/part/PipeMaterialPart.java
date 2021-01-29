package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import net.minecraft.item.Item;

import java.util.function.Function;

public class PipeMaterialPart implements MaterialPart {
    protected final String materialName;
    private final PipeType type;
    private final String part;
    private final String itemId;
    protected final int color;
    private PipeNetworkType pipeType;

    public static Function<MaterialBuilder.Context, MaterialPart> of(PipeType type) {
        return ctx -> new PipeMaterialPart(ctx.materialName, type, ctx.color);
    }

    protected PipeMaterialPart(String materialName, PipeType type, int color) {
        this.materialName = materialName;
        this.type = type;
        this.part = type.partName;
        this.itemId = "modern_industrialization:pipe_" + type.internalName + "_" + materialName;
        this.color = color;
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return itemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register() {
        if (type == PipeType.ITEM) {
            MIPipes.INSTANCE.registerItemPipeType(materialName, color);
        } else if (type == PipeType.FLUID) {
            MIPipes.INSTANCE.registerFluidPipeType(materialName, color, 81000);
        }
    }

    @Override
    public Item getItem() {
        throw new UnsupportedOperationException();
    }
}
