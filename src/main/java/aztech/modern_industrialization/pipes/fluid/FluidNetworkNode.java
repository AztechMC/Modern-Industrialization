package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.fluid.FluidInventory;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FluidNetworkNode extends PipeNetworkNode implements Tickable {
    int amount = 0;
    private List<FluidConnection> connections = new ArrayList<>();

    @Override
    public void tick() {
        network.tick();
    }

    void interactWithConnections() {
        FluidNetworkData data = (FluidNetworkData) network.data;
        for(FluidConnection connection : connections) { // TODO: limit insert and extract rate
            // Insert
            if(amount > 0 && connection.canInsert(data.fluid)) {
                int inserted = connection.fluidInventory.insert(connection.direction, data.fluid, amount, false);
                amount -= inserted;
            }
            // Extract any
            if(data.fluid == Fluids.EMPTY) {
                for(Fluid fluid : connection.fluidInventory.getExtractableFluids(connection.direction)) {
                    int extracted = connection.fluidInventory.extract(connection.direction, fluid, data.nodeCapacity, false);
                    if(extracted > 0) {
                        amount = extracted;
                        data.fluid = fluid;
                        break;
                    }
                }
            }
            // Extract current fluid
            else {
                int extracted = connection.fluidInventory.extract(connection.direction, data.fluid, data.nodeCapacity - amount, false);
                amount += extracted;
            }
        }
    }

    @Override
    public void updateConnections(World world, BlockPos pos) {
        connections.clear();
        for(Direction direction : Direction.values()) {
            BlockPos adjPos = pos.offset(direction);
            BlockEntity entity = world.getBlockEntity(adjPos);
            if(entity instanceof FluidInventory) {
               connections.add(new FluidConnection(direction, (FluidInventory)entity));
            }
        }
    }

    public Set<Direction> getRenderedConnections(BlockPos pos) {
        Set<Direction> links = network.manager.getNodeLinks(pos);
        for(FluidConnection connection : connections) {
            links.add(connection.direction);
        }
        return links;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("amount", amount);
        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        amount = tag.getInt("amount");
    }

    private static class FluidConnection {
        private final Direction direction;
        private final FluidInventory fluidInventory;

        private FluidConnection(Direction direction, FluidInventory fluidInventory) {
            this.direction = direction;
            this.fluidInventory = fluidInventory;
        }

        private boolean canInsert(Fluid fluid) {
            return true;
        }

        private boolean canExtract(Fluid fluid) {
            return true;
        }
    }
}
