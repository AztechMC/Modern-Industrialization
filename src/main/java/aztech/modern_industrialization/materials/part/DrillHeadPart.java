package aztech.modern_industrialization.materials.part;

import aztech.modern_industrialization.api.item.modular_tools.ComponentTier;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry.HeadProperties;
import aztech.modern_industrialization.items.modulartools.ModularToolItem.ToolType;

public class DrillHeadPart implements PartKeyProvider {
    @Override
    public PartKey key() {
        return new PartKey("drill_head");
    }

    public PartTemplate simple() {
        return new PartTemplate("Drill Head", "drill_head");
    }

    public PartTemplate withModularComponent(ComponentTier tier, int miningLevel, float miningSpeed,
            double attackDamage) {
        return new PartTemplate("Drill Head", "drill_head").withRegister(
                (partContext, part, itemPath, itemId, itemTag, englishName) -> {
                    var item = PartTemplate.createSimpleItem(englishName, itemPath, partContext, part);
                    HeadRegistry.register(item,
                            new HeadProperties(tier, ToolType.DRILL, miningLevel, miningSpeed, attackDamage));
                });
    }
}
