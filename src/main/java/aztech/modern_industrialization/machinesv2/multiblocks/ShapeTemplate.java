package aztech.modern_industrialization.machinesv2.multiblocks;

import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * An immutable description of a multiblock shape.
 */
public class ShapeTemplate {
    final Map<BlockPos, SimpleMember> simpleMembers = new HashMap<>();
    final Map<BlockPos, HatchFlags> hatchFlags = new HashMap<>();

    public static class Builder {
        private final ShapeTemplate template = new ShapeTemplate();

        public Builder add3by3(int y, SimpleMember member, boolean hollow, @Nullable HatchFlags flags) {
            for (int x = -1; x <= 1; x++) {
                for (int z = 0; z <= 2; z++) {
                    if (hollow && x == 0 && z == 1) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    template.simpleMembers.put(pos, member);
                    if (flags != null) {
                        template.hatchFlags.put(pos, flags);
                    }
                }
            }
            return this;
        }

        public Builder remove(int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            template.simpleMembers.remove(pos);
            template.hatchFlags.remove(pos);
            return this;
        }

        public ShapeTemplate build() {
            remove(0, 0, 0);
            return template;
        }
    }
}
