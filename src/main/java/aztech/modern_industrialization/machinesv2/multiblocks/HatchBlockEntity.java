package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasing;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.List;
import java.util.Objects;

public abstract class HatchBlockEntity extends MachineBlockEntity implements Tickable {
    public HatchBlockEntity(BlockEntityType<?> type, MachineGuiParameters guiParams, OrientationComponent.Params orientationParams) {
        super(type, guiParams);

        this.orientation = new OrientationComponent(orientationParams);
        registerComponents(orientation, new IComponent.ClientOnly() {
            @Override
            public void writeClientNbt(CompoundTag tag) {
                if (matchedCasing != null) {
                    tag.putString("matchedCasing", matchedCasing);
                }
            }

            @Override
            public void readClientNbt(CompoundTag tag) {
                matchedCasing = tag.contains("matchedCasing") ? tag.getString("matchedCasing") : null;
            }
        });
    }

    private String lastSyncedMachineCasing = null;
    private String matchedCasing = null;
    private final OrientationComponent orientation;

    public abstract HatchType getHatchType();

    /**
     * Return true if this hatch upgrades steam multiblocks to steel tier.
     */
    public abstract boolean upgradesToSteel();

    public boolean isMatched() {
        return matchedCasing != null;
    }

    // This amazing function is called when the block entity is loaded.
    @Override
    public void cancelRemoval() {
        super.cancelRemoval();
        clearMachineLock();
    }

    public void unlink() {
        matchedCasing = null;
        clearMachineLock();
    }

    public void link(MachineCasing casing) {
        matchedCasing = casing.name;
    }

    private void clearMachineLock() {
        for (ConfigurableItemStack itemStack : getInventory().itemStacks) {
            itemStack.disableMachineLock();
        }
        for (ConfigurableFluidStack fluidStack : getInventory().fluidStacks) {
            fluidStack.disableMachineLock();
        }
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineCasing casing = isMatched() ? MachineCasings.get(matchedCasing) : null;
        MachineModelClientData data = new MachineModelClientData(casing);
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
        orientation.outputDirection = orientation.outputDirection.getOpposite();
    }

    @Override
    public void tick() {
        if (world.isClient()) return;

        if (!Objects.equals(lastSyncedMachineCasing, matchedCasing)) {
            lastSyncedMachineCasing = matchedCasing;
            sync();
        }
    }

    public void appendItemInputs(List<ConfigurableItemStack> list) {
    }

    public void appendItemOutputs(List<ConfigurableItemStack> list) {
    }

    public void appendFluidInputs(List<ConfigurableFluidStack> list) {
    }

    public void appendFluidOutputs(List<ConfigurableFluidStack> list) {
    }
}
