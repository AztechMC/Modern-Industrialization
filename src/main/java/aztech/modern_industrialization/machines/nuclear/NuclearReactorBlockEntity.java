package aztech.modern_industrialization.machines.nuclear;

import alexiil.mc.lib.attributes.item.ItemAttributes;
import aztech.modern_industrialization.MINuclearItem;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.multiblock.HatchBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.HatchType;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockShape;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NuclearReactorBlockEntity extends MultiblockMachineBlockEntity {

    public NuclearReactorBlockEntity(MachineFactory factory, MultiblockShape shape) {
        super(factory, shape, false);
    }

    @Override
    public void tick() {
        if (world.isClient)
            return;
        this.tickCheckShape();
        for (HatchBlockEntity hatch : linkedHatches.values()) {
            if (hatch.type == HatchType.ITEM_INPUT) {
                hatch.autoExtractItems(ItemAttributes.INSERTABLE.get(this.getWorld(), this.getPos()), true);
            }
        }
        for (ConfigurableItemStack stack : this.itemStacks) {
            if (stack.getStack().getCount() > 0) {
                ItemStack is = stack.getStack();
                if (is.getItem() instanceof MINuclearItem) {
                    MINuclearItem item = (MINuclearItem) is.getItem();
                    int damage = is.getDamage();
                    int durability = item.getDurability();
                    if (damage < durability - 1) {
                        is.setDamage(damage + 1);
                    } else if (damage == durability - 1) {
                        Item depleted = item.getDepleted();
                        if (depleted != null) {
                            boolean inserted = false;
                            ItemStack depletedStack = new ItemStack(depleted, 1);

                            attempt: for (int attempts = 0; attempts < 2; attempts++) {
                                for (HatchBlockEntity hatch : linkedHatches.values()) {
                                    if (hatch.type == HatchType.ITEM_OUTPUT) {
                                        for (ConfigurableItemStack outputCStack : hatch.getItemStacks()) {
                                            ItemStack outputStack = outputCStack.getStack();
                                            if (!outputStack.isEmpty() || attempts == 1) {
                                                if (outputCStack.canInsert(depletedStack)) {
                                                    if (outputStack.isEmpty()) {
                                                        outputCStack.setStack(depletedStack);
                                                        inserted = true;
                                                        break attempt;
                                                    } else if (outputStack.getCount() + 1 <= depleted.getMaxCount()) {
                                                        outputStack.increment(1);
                                                        inserted = true;
                                                        break attempt;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (inserted) {
                                is.setCount(0);
                            }

                        } else {
                            is.setCount(0);
                        }
                    }
                }
            }

        }
        markDirty();
    }
}
