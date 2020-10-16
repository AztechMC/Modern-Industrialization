package aztech.modern_industrialization.machines.impl.multiblock;

import static net.minecraft.util.math.Direction.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * A multiblock shape. It uses its own coordinate system. The controller block
 * is placed at (0, 0, 0), facing north (-z).
 */
public class MultiblockShape {
    public interface Entry {
        boolean matches(BlockView world, BlockPos pos);

        Text getErrorMessage();
    }

    Map<BlockPos, Entry> entries = new HashMap<>();
    int maxHatches = Integer.MAX_VALUE;
    private Text errorMessage = null;

    public void addEntry(BlockPos pos, Entry entry) {
        if (entry == null)
            throw new IllegalArgumentException("Can't accept null entry");
        if (entries.put(pos, entry) != null)
            throw new IllegalStateException("Can't override an existing multiblock entry");
    }

    public void addEntry(int x, int y, int z, Entry entry) {
        addEntry(new BlockPos(x, y, z), entry);
    }

    public MultiblockShape setMaxHatches(int maxHatches) {
        this.maxHatches = maxHatches;
        return this;
    }

    public boolean matchShape(World world, BlockPos controllerPos, Direction controllerDirection, Map<BlockPos, HatchBlockEntity> outHatches,
            Set<BlockPos> outStructure) {
        if (controllerDirection.getAxis().isVertical())
            throw new IllegalArgumentException("Multiblocks can only be oriented horizontally");
        errorMessage = null;

        Function<BlockPos, BlockPos> shapeToWorld = shapePos -> {
            BlockPos rotatedPos;
            if (controllerDirection == NORTH)
                rotatedPos = shapePos;
            else if (controllerDirection == SOUTH)
                rotatedPos = new BlockPos(-shapePos.getX(), shapePos.getY(), -shapePos.getZ());
            else if (controllerDirection == EAST)
                rotatedPos = new BlockPos(-shapePos.getZ(), shapePos.getY(), shapePos.getX());
            else
                rotatedPos = new BlockPos(shapePos.getZ(), shapePos.getY(), -shapePos.getX());
            return rotatedPos.add(controllerPos);
        };

        int hatches = 0;
        for (Map.Entry<BlockPos, Entry> entry : entries.entrySet()) {
            BlockPos worldPos = shapeToWorld.apply(entry.getKey());
            if (entry.getValue().matches(world, worldPos)) {
                if (world.getBlockEntity(worldPos) instanceof HatchBlockEntity) {
                    ++hatches;
                    outHatches.put(worldPos, (HatchBlockEntity) world.getBlockEntity(worldPos));
                } else {
                    outStructure.add(worldPos);
                }
            } else {
                errorMessage = new TranslatableText("text.modern_industrialization.shape_error", worldPos.getX(), worldPos.getY(), worldPos.getZ(),
                        entry.getValue().getErrorMessage());
                return false;
            }
        }

        if (hatches > maxHatches) {
            errorMessage = new TranslatableText("text.modern_industrialization.shape_error_too_many_hatches", hatches, maxHatches);
            return false;
        }

        errorMessage = new TranslatableText("text.modern_industrialization.shape_valid");
        return true;
    }

    public Text getErrorMessage() {
        return errorMessage;
    }
}
