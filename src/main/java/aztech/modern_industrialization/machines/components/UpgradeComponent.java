/*
 * MIT License
 *
 * Copyright (c) 2020 Azercoco & Technici4n
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;

public class UpgradeComponent implements IComponent.ServerOnly {

    private ItemStack itemStack;
    public final static Map<Item, Long> upgrades = new IdentityHashMap<>();

    static {
        upgrades.put(MIItem.BASIC_UPGRADE, 2L);
        upgrades.put(MIItem.ADVANCED_UPGRADE, 8L);
        upgrades.put(MIItem.TURBO_UPGRADE, 32L);
        upgrades.put(MIItem.HIGHLY_ADVANCED_UPGRADE, 128L);
        upgrades.put(MIItem.QUANTUM_UPGRADE, 999999999L);
    }

    public UpgradeComponent() {
        itemStack = ItemStack.EMPTY;

    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.put("upgradesItemStack", itemStack.writeNbt(new NbtCompound()));
    }

    @Override
    public void readNbt(NbtCompound tag) {
        itemStack = ItemStack.fromNbt(tag.getCompound("upgradesItemStack"));
    }

    public ActionResult onUse(MachineBlockEntity be, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.isEmpty()) {
            return ActionResult.PASS;
        }
        if (stackInHand.getItem() == MIItem.ITEM_CROWBAR && player.isSneaking()) {
            BlockPos pos = be.getPos();
            if (!itemStack.isEmpty()) {
                ItemScatterer.spawn(be.getWorld(), pos.getX(), pos.getY(), pos.getZ(), itemStack);
                be.markDirty();
                if (!be.getWorld().isClient()) {
                    be.sync();

                }
                return ActionResult.success(be.getWorld().isClient);
            }

        } else {
            if (upgrades.containsKey(stackInHand.getItem())) {
                boolean changed = false;
                if (itemStack.isEmpty()) {
                    itemStack = stackInHand.copy();
                    if (!player.isCreative()) {
                        stackInHand.setCount(0);
                    }
                    changed = true;

                } else if (stackInHand.getItem() == itemStack.getItem()) {
                    int maxAdded = Math.min(stackInHand.getCount(), itemStack.getMaxCount() - itemStack.getCount());
                    changed = maxAdded > 0;
                    itemStack.increment(maxAdded);
                    if (!player.isCreative()) {
                        stackInHand.decrement(maxAdded);
                    }
                }
                if (changed) {
                    be.markDirty();
                    if (!be.getWorld().isClient()) {
                        be.sync();

                    }
                    return ActionResult.success(be.getWorld().isClient);
                }

            }
        }

        return ActionResult.PASS;
    }

    public long getAddMaxEUPerTick() {
        if (itemStack.isEmpty()) {
            return 0;
        } else {
            return itemStack.getCount() * upgrades.get(itemStack.getItem());
        }
    }

    public ItemStack getDrop() {
        return itemStack;
    }

}
