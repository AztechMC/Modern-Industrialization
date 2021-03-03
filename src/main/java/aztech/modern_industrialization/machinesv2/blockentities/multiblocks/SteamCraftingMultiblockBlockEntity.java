package aztech.modern_industrialization.machinesv2.blockentities.multiblocks;

import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machinesv2.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class SteamCraftingMultiblockBlockEntity extends MachineBlockEntity implements Tickable, ChunkUnloadBlockEntity {
    public SteamCraftingMultiblockBlockEntity(BlockEntityType<?> type, String name, ShapeTemplate shapeTemplate) {
        super(type, new MachineGuiParameters.Builder(name, false).build());

        this.orientation = new OrientationComponent(new OrientationComponent.Params(false, false, false));
        this.shapeTemplate = shapeTemplate;
        registerComponents(orientation);
    }

    private final OrientationComponent orientation;
    private final ShapeTemplate shapeTemplate;
    @Nullable
    private ShapeMatcher shapeMatcher = null;

    @Override
    public MIInventory getInventory() {
        return MIInventory.EMPTY;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = OrientationHelper.onUse(player, hand, face, orientation, this);
        if (result.isAccepted()) {
            if (shapeMatcher != null) {
                shapeMatcher.unlinkHatches();
                shapeMatcher.unregisterListeners(world);
                shapeMatcher = null;
            }
        }
        return result;
    }

    @Override
    protected MachineModelClientData getModelData() {
        return new MachineModelClientData(null, orientation.facingDirection);
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        if (!world.isClient) {
            if (shapeMatcher == null) {
                shapeMatcher = new ShapeMatcher(world, pos, orientation.facingDirection, shapeTemplate);
                shapeMatcher.registerListeners(world);
            }
            shapeMatcher.rematchIfNecessary(world);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }

    @Override
    public void onChunkUnload() {
        if (shapeMatcher != null) {
            shapeMatcher.unlinkHatches();
            shapeMatcher.unregisterListeners(world);
            shapeMatcher = null;
        }
    }
}
