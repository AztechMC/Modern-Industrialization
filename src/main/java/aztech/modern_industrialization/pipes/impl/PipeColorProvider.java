package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.concurrent.ThreadLocalRandom;

public class PipeColorProvider implements BlockColorProvider {
    @Override
    public int getColor(BlockState state, BlockRenderView world, BlockPos pos, int tintIndex) {
        PipeBlockEntity entity = (PipeBlockEntity) world.getBlockEntity(pos);
        if(entity == null) return -1;
        int n = entity.connections.size();
        if(n == 0) return -1;
        int i = ThreadLocalRandom.current().nextInt(n);
        return entity.connections.keySet().stream().toArray(PipeNetworkType[]::new)[i].getColor();
    }
}
