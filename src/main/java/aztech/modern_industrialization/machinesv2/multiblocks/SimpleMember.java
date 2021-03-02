package aztech.modern_industrialization.machinesv2.multiblocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

/**
 * The representation of a simple logic-less member that is part of a shape, e.g. a casing.
 */
public interface SimpleMember {
    boolean matchesState(BlockState state);
    BlockState getPreviewState(BlockState state);

    static SimpleMember forBlock(Block block) {
        return new SimpleMember() {
            @Override
            public boolean matchesState(BlockState state) {
                return state.isOf(block);
            }

            @Override
            public BlockState getPreviewState(BlockState state) {
                return block.getDefaultState();
            }
        };
    }

    static SimpleMember forBlock(Identifier id) {
        return forBlock(Registry.BLOCK.get(id));
    }
}
