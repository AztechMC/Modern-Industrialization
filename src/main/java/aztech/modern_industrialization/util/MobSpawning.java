package aztech.modern_industrialization.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.EntityType;

public class MobSpawning {
    public static final AbstractBlock.TypedContextPredicate<EntityType<?>> NO_SPAWN = (s, w, p, t) -> false;
}
