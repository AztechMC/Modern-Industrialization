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

import aztech.modern_industrialization.api.datamaps.MIDataMaps;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UpgradeComponent implements IComponent.ServerOnly, DropableComponent {

    private ItemStack itemStack = ItemStack.EMPTY;

    public static long getExtraEu(ItemLike item) {
        var data = item.asItem().builtInRegistryHolder().getData(MIDataMaps.MACHINE_UPGRADES);
        return data == null ? 0 : data.extraMaxEu();
    }

    static {
        KubeJSProxy.instance.fireRegisterUpgradesEvent();
    }

    @Override
    public void writeNbt(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("upgradesItemStack", itemStack.saveOptional(registries));
    }

    @Override
    public void readNbt(CompoundTag tag, HolderLookup.Provider registries, boolean isUpgradingMachine) {
        itemStack = ItemStack.parseOptional(registries, tag.getCompound("upgradesItemStack"));
    }

    public ItemInteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (UpgradeComponent.getExtraEu(stackInHand.getItem()) > 0) {
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
                stackInHand.consume(maxAdded, player);
            }
            if (changed) {
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();

                }
                return ItemInteractionResult.sidedSuccess(be.getLevel().isClientSide);
            }

        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public long getAddMaxEUPerTick() {
        if (itemStack.isEmpty()) {
            return 0;
        } else {
            return itemStack.getCount() * UpgradeComponent.getExtraEu(itemStack.getItem());
        }
    }

    @Override
    public ItemStack getDrop() {
        return itemStack;
    }

    public void setStackServer(MachineBlockEntity be, ItemStack stack) {
        itemStack = stack;
        be.setChanged();
        be.sync();
    }
}
