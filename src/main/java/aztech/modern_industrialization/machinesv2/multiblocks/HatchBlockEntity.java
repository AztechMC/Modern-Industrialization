package aztech.modern_industrialization.machinesv2.multiblocks;

import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasingModel;
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
    public abstract MachineCasingModel getUnmatchedCasing();

    public boolean isMatched() {
        return matchedCasing != null;
    }

    public void unlink() {
        matchedCasing = null;
    }

    public void link(String casing) {
        matchedCasing = casing;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineCasingModel mcm = isMatched() ? MachineCasings.get(matchedCasing) : getUnmatchedCasing();
        MachineModelClientData data = new MachineModelClientData(mcm);
        data.frontDirection = Direction.NORTH; // hatches don't have a front side so it's irrelevant
        data.outputDirection = orientation.outputDirection;
        data.itemAutoExtract = orientation.extractItems;
        data.fluidAutoExtract = orientation.extractFluids;
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
}
