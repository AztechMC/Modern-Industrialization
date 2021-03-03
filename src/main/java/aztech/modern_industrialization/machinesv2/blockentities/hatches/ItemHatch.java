package aztech.modern_industrialization.machinesv2.blockentities.hatches;

import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machinesv2.components.OrientationComponent;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.multiblocks.HatchBlockEntity;
import net.minecraft.block.entity.BlockEntityType;

public class ItemHatch extends HatchBlockEntity {
    public ItemHatch(BlockEntityType<?> type, MachineGuiParameters guiParams, boolean input, MIInventory inventory) {
        super(type, guiParams, new OrientationComponent.Params(true, true, false));

        this.input = input;
        this.inventory = inventory;
    }

    private final boolean input;
    private final MIInventory inventory;

    @Override
    public HatchType getHatchType() {
        return input ? HatchType.ITEM_INPUT : HatchType.ITEM_OUTPUT;
    }

    @Override
    public MIInventory getInventory() {
        return inventory;
    }
}
