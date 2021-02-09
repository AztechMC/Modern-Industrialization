package aztech.modern_industrialization.machinesv2;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.util.MobSpawning;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;

import java.util.function.Supplier;

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;

public class MachineBlock extends MIBlock implements BlockEntityProvider {
    private final Supplier<BlockEntity> blockEntityConstructor;

    public MachineBlock(String machineId, Supplier<BlockEntity> blockEntityConstructor) {
        super(machineId, FabricBlockSettings.of(METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool()
                .allowsSpawning(MobSpawning.NO_SPAWN));
        this.blockEntityConstructor = blockEntityConstructor;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView world) {
        return blockEntityConstructor.get();
    }
}
