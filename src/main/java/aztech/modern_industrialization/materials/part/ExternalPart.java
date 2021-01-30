package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.materials.MaterialBuilder;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Function;

public class ExternalPart implements MaterialPart {
    private final String part, taggedItemId, itemId;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(String part, String taggedItemId, String itemId) {
        return ctx -> new ExternalPart(part, taggedItemId, itemId);
    }

    private ExternalPart(String part, String taggedItemId, String itemId) {
        this.part = part;
        this.taggedItemId = taggedItemId;
        this.itemId = itemId;
    }

    @Override
    public String getPart() {
        return part;
    }

    @Override
    public String getTaggedItemId() {
        return taggedItemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register() {
    }

    @Override
    public Item getItem() {
        return Registry.ITEM.get(new Identifier(itemId));
    }
}
