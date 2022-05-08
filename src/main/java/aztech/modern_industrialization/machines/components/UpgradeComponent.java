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
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeComponent implements IComponent.ServerOnly {

    private ItemStack itemStack;
    public final static Map<Item, Long> upgrades = new IdentityHashMap<>();

    static {
        upgrades.put(MIItem.BASIC_UPGRADE.asItem(), 2L);
        upgrades.put(MIItem.ADVANCED_UPGRADE.asItem(), 8L);
        upgrades.put(MIItem.TURBO_UPGRADE.asItem(), 32L);
        upgrades.put(MIItem.HIGHLY_ADVANCED_UPGRADE.asItem(), 128L);
        upgrades.put(MIItem.QUANTUM_UPGRADE.asItem(), 999999999L);
    }

    public UpgradeComponent() {
        itemStack = ItemStack.EMPTY;

    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.put("upgradesItemStack", itemStack.save(new CompoundTag()));
    }

    @Override
    public void readNbt(CompoundTag tag) {
        itemStack = ItemStack.of(tag.getCompound("upgradesItemStack"));
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (stackInHand.getItem() == MIItem.ITEM_CROWBAR.asItem() && player.isShiftKeyDown()) {
            BlockPos pos = be.getBlockPos();
            if (!itemStack.isEmpty()) {
                Containers.dropItemStack(be.getLevel(), pos.getX(), pos.getY(), pos.getZ(), itemStack);
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();

                }
                return InteractionResult.sidedSuccess(be.getLevel().isClientSide);
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
                    int maxAdded = Math.min(stackInHand.getCount(), itemStack.getMaxStackSize() - itemStack.getCount());
                    changed = maxAdded > 0;
                    itemStack.grow(maxAdded);
                    if (!player.isCreative()) {
                        stackInHand.shrink(maxAdded);
                    }
                }
                if (changed) {
                    be.setChanged();
                    if (!be.getLevel().isClientSide()) {
                        be.sync();

                    }
                    return InteractionResult.sidedSuccess(be.getLevel().isClientSide);
                }

            }
        }

        return InteractionResult.PASS;
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
