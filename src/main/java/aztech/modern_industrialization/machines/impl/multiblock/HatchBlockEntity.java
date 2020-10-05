package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.FLUID_OUTPUT;
import static aztech.modern_industrialization.machines.impl.multiblock.HatchType.ITEM_OUTPUT;

public class HatchBlockEntity extends MachineBlockEntity {
    private BlockPos controllerPos = null;
    /**
     * This variable is use to lazily sync controllerPos, to prevent needless updates when the controller checks the shape!
     */
    private BlockPos lastSyncedControllerPos = null;
    private boolean lateLoaded = false;
    public final HatchType type;

    public HatchBlockEntity(MachineFactory factory, HatchType type) {
        super(factory);
        this.type = type;
    }

    private void lateLoad() {
        if(lateLoaded) return;
        lateLoaded = true;
        clearLocks();
        if(controllerPos != null) {
            BlockEntity controllerEntity = world.getBlockEntity(controllerPos);
            controllerPos = null;
            if(controllerEntity instanceof MultiblockMachineBlockEntity) {
                ((MultiblockMachineBlockEntity) controllerEntity).hatchLoaded();
            }
        }
    }

    boolean isUnlinked() {
        return controllerPos == null;
    }

    void unlink() {
        controllerPos = null;
        markDirty();
    }

    void link(MultiblockMachineBlockEntity controller) {
        controllerPos = controller.getPos();
        markDirty();
    }

    // TODO: override methods

    @Override
    public void tick() {
        if(world.isClient) return;
        lateLoad();
        if(controllerPos != lastSyncedControllerPos) sync();
        if(extractItems && type == ITEM_OUTPUT) {
            autoExtractItems(world, pos, outputDirection);
        }
        if(extractFluids && type == FLUID_OUTPUT) {
            autoExtractFluids(world, pos, outputDirection);
        }
        markDirty();
        // TODO: auto-input
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        NbtHelper.putBlockPos(tag, "controllerPos", controllerPos);
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        controllerPos = NbtHelper.getBlockPos(tag, "controllerPos");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        NbtHelper.putBlockPos(tag, "controllerPos", controllerPos);
        lastSyncedControllerPos = controllerPos;
        return super.toClientTag(tag);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        controllerPos = NbtHelper.getBlockPos(tag, "controllerPos");
        if(controllerPos == null) {
            controllerPos = null;
            casingOverride = null;
        } else {
            MultiblockMachineBlockEntity be = (MultiblockMachineBlockEntity) world.getBlockEntity(controllerPos);
            casingOverride = be == null ? null : be.hatchCasing;
        }
        super.fromClientTag(tag);
    }

    @Override
    public void markRemoved() {
        if(controllerPos != null) {
            MultiblockMachineBlockEntity be = (MultiblockMachineBlockEntity) world.getBlockEntity(controllerPos);
            if(be != null) {
                be.hatchRemoved(pos);
            }
        }
        super.markRemoved();
    }
}
