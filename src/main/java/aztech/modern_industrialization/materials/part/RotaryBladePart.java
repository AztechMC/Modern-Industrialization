package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.api.item.modular_tools.ComponentTier;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry.HeadProperties;
import aztech.modern_industrialization.items.modular_tools.ModularToolItem.ToolType;

public class RotaryBladePart implements PartKeyProvider {
    @Override
    public PartKey key() {
        return new PartKey("rotary_blade");
    }

    public PartTemplate simple() {
        return new PartTemplate("Rotary Blade", "rotary_blade");
    }

    public PartTemplate withModularComponent(ComponentTier tier, float miningSpeed, double attackDamage) {
        return new PartTemplate("Rotary Blade", "rotary_blade").withRegister(
                (partContext, part, itemPath, itemId, itemTag, englishName) -> {
                    var item = PartTemplate.createSimpleItem(englishName, itemPath, partContext, part);
                    HeadRegistry.register(item,
                            new HeadProperties(tier, ToolType.CHAINSAW, -1, miningSpeed, attackDamage));
                });
    }
}
