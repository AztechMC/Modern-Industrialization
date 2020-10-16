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
    final VoxelShape shape;
    /**
     * The network type.
     */
    final PipeNetworkType type;
    /**
     * If null, the center of the pipe. Otherwise, the connector in the given direction.
     */
    final Direction direction;

    /**
     * Whether this pipe being right-clicked opens a gui.
     */
    final boolean opensGui;

    PipeVoxelShape(VoxelShape shape, PipeNetworkType type, Direction direction, boolean opensGui) {
        this.shape = shape;
        this.type = type;
        this.direction = direction;
        this.opensGui = opensGui;
    }
}
