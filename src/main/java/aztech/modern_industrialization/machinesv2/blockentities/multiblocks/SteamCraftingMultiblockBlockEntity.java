package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.models.MachineCasingModel;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SteamCraftingMultiblockBlockEntity extends MachineBlockEntity implements Tickable, ChunkUnloadBlockEntity {
    public SteamCraftingMultiblockBlockEntity(BlockEntityType<?> type, String name, ShapeTemplate shapeTemplate) {
        super(type, new MachineGuiParameters.Builder(name, false).build());

        this.shapeTemplate = shapeTemplate;
    }

    private final ShapeTemplate shapeTemplate;
    @Nullable
    private ShapeMatcher shapeMatcher = null;

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return ActionResult.PASS;
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(MachineCasings.BRONZE);
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {

    }

    @Override
    public void fromClientTag(CompoundTag tag) {

    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        return tag;
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            if (shapeMatcher == null) {
                shapeMatcher = new ShapeMatcher(world, pos, Direction.NORTH, shapeTemplate);
                shapeMatcher.registerListeners(world);
            }
            shapeMatcher.rematchIfNecessary(world);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (shapeMatcher != null) {
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }

    @Override
    public void onChunkUnload() {
        if (shapeMatcher != null) {
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }
}
