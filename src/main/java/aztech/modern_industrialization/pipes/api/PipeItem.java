package aztech.modern_industrialization.pipes.api;

import aztech.modern_industrialization.pipes.MIPipes;
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
        BlockPos placingPos = context.getBlockPos().offset(context.getSide());
        World world = context.getWorld();
        if(world.getBlockState(placingPos).isAir()) {
            // TODO: Check BlockItem code and implement all checks
            world.setBlockState(placingPos, MIPipes.BLOCK_PIPE.getDefaultState(), 11); // TODO: check flags
            PipeBlockEntity entity = (PipeBlockEntity)world.getBlockEntity(placingPos);
            entity.addPipe(type, defaultData.clone());
        }

        return super.useOnBlock(context);
    }
}
