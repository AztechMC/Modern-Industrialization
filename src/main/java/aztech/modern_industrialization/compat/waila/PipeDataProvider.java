package aztech.modern_industrialization.compat.waila;

import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkNode;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class PipeDataProvider implements IServerDataProvider<BlockEntity> {
    @Override
    public void appendServerData(CompoundTag data, ServerPlayerEntity player, World world, BlockEntity be) {
        PipeBlockEntity pipe = (PipeBlockEntity) be;

        for (PipeNetworkNode node : pipe.getNodes()) {
            CompoundTag pipeData = new CompoundTag();

            if (node instanceof FluidNetworkNode) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) node;
                pipeData.putInt("amount", fluidNode.getAmount());
                pipeData.putInt("capacity", fluidNode.getCapacity());
                pipeData.put("fluid", fluidNode.getFluid().toTag());
            }

            if (node instanceof ElectricityNetworkNode) {
                ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) node;
                pipeData.putLong("eu", electricityNode.getEu());
                pipeData.putLong("maxEu", electricityNode.getMaxEu());
                pipeData.putString("tier", electricityNode.getTier().toString());
            }

            data.put(node.getType().getIdentifier().toString(), pipeData);
        }
    }
}
