package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.OrientationHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigurableChestMachineBlockEntity extends MachineBlockEntity implements Tickable {

    private final OrientationComponent orientation;
    private final MIInventory inventory;

    public ConfigurableChestMachineBlockEntity(BlockEntityType<?> type) {
        super(type, new MachineGuiParameters.Builder("configurable_chest", true).build());
        orientation = new OrientationComponent(new OrientationComponent.Params(true, false, false));

        List<ConfigurableItemStack> stacks = new ArrayList<>();
        for(int i = 0; i < 21; i++){
            stacks.add(ConfigurableItemStack.standardIOSlot(true));
        }
        SlotPositions itemPositions = new SlotPositions.Builder().addSlots(16, 16, 3, 7).build();
        inventory = new MIInventory(stacks, Collections.EMPTY_LIST, itemPositions, SlotPositions.empty());
        this.registerComponents(orientation, inventory);
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        return OrientationHelper.onUse(player, hand, face, orientation, this);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData(MachineCasings.STEEL_CRATE);
        orientation.writeModelData(data);
        return data;
    }

    @Override
    public void onPlaced(LivingEntity placer, ItemStack itemStack) {
        orientation.onPlaced(placer, itemStack);
    }

    @Override
    public void tick() {
        // TODO Add auto output
    }
}
