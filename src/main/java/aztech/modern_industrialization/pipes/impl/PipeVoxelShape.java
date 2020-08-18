package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

/**
 * A voxel shape and the part of the pipe it represents.
 */
class PipeVoxelShape {
    /**
     * The shape.
     */
    VoxelShape shape;
    /**
     * The network type.
     */
    PipeNetworkType type;
    /**
     * If null, the center of the pipe. Otherwise, the connector in the given direction.
     */
    Direction direction;

    public PipeVoxelShape(VoxelShape shape, PipeNetworkType type, Direction direction) {
        this.shape = shape;
        this.type = type;
        this.direction = direction;
    }
}
