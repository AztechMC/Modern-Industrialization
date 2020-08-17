package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.MIPipes;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeItem extends Item {
    final PipeNetworkType type;
    final PipeNetworkData defaultData;

    public PipeItem(Settings settings, PipeNetworkType type, PipeNetworkData defaultData) {
        super(settings);
        this.type = type;
        this.defaultData = defaultData;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        // TODO: Check BlockItem code and implement all checks.
        // TODO: Check advancement criteria.
        World world = context.getWorld();
        // First, try to add a pipe to an existing block
        BlockPos hitPos = context.getBlockPos();
        BlockEntity entity = world.getBlockEntity(hitPos);
        if(entity instanceof PipeBlockEntity) {
            PipeBlockEntity pipeEntity = (PipeBlockEntity) entity;
            if(pipeEntity.canAddPipe(type)) {
                // The pipe could be added, it's a success
                if(!world.isClient) {
                    pipeEntity.addPipe(type, defaultData);
                }
                world.updateNeighbors(hitPos, null);
                return ActionResult.success(world.isClient);
            }
        }
        // Place a new block otherwise
        BlockPos placingPos = context.getBlockPos().offset(context.getSide());
        if(world.getBlockState(placingPos).isAir()) {
            world.setBlockState(placingPos, MIPipes.BLOCK_PIPE.getDefaultState(), 11); // TODO: check flags
            if(!world.isClient) {
                PipeBlockEntity pipeEntity = (PipeBlockEntity) world.getBlockEntity(placingPos);
                pipeEntity.addPipe(type, defaultData.clone());
            }
        }

        return super.useOnBlock(context);
    }
}
