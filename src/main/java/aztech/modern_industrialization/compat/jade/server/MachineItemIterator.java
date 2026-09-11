package aztech.modern_industrialization.compat.jade.server;

import aztech.modern_industrialization.api.machine.component.ItemAccess;
import aztech.modern_industrialization.api.machine.holder.MultiblockInventoryComponentHolder;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import snownee.jade.addon.universal.ItemIterator;

public class MachineItemIterator extends ItemIterator<MachineBlockEntity> {
    public MachineItemIterator() {
        super(MachineBlockEntity.class::cast, 0);
    }

    @Override
    public Stream<ItemStack> populate(MachineBlockEntity machine) {
        List<ItemStack> stacks = new ArrayList<>();
        if (machine instanceof MultiblockInventoryComponentHolder multiblock) {
            var component = multiblock.getMultiblockInventoryComponent();
            addStacks(stacks, component.getItemInputs());
            addStacks(stacks, component.getItemOutputs());
        } else {
            addStacks(stacks, machine.getInventory().getItemStacks());
        }
        return stacks.isEmpty() ? Stream.empty() : stacks.stream();
    }

    private static void addStacks(List<ItemStack> stacks, List<? extends ItemAccess> itemAccesses) {
        for (ItemAccess access : itemAccesses) {
            stacks.add(access.toStack());
        }
    }
}
