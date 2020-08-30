package aztech.modern_industrialization.machines.impl.multiblock;

import aztech.modern_industrialization.machines.impl.MachineBlockEntity;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class HatchBlockEntity extends MachineBlockEntity implements ChunkUnloadBlockEntity {
    protected BlockPos controllerPos = null;
    protected boolean lateLoaded = false;
    public final HatchType type;

    public HatchBlockEntity(MachineFactory factory, HatchType type) {
        super(factory, null);
        this.type = type;
    }

    protected void lateLoad() {
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

    // TODO: override methods

    @Override
    public void tick() {
        lateLoad();
        // TODO: auto-input/auto-output
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
    public void onChunkUnload() {
        if(controllerPos != null) {
            ((MultiblockMachineBlockEntity) world.getBlockEntity(controllerPos)).hatchUnloaded(pos);
        }
    }
}
