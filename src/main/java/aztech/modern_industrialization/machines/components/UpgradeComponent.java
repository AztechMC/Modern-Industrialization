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
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UpgradeComponent implements IComponent.ServerOnly, DropableComponent {

    private ItemStack itemStack = ItemStack.EMPTY;
    private final static Map<ResourceLocation, Long> UPGRADES = new HashMap<>();

    public static long getExtraEu(ItemLike item) {
        return UPGRADES.getOrDefault(BuiltInRegistries.ITEM.getKey(item.asItem()), 0L);
    }

    public static void registerUpgrade(ItemLike item, long extraEu) {
        registerUpgrade(BuiltInRegistries.ITEM.getKey(item.asItem()), extraEu);
    }

    public static void registerUpgrade(ResourceLocation itemId, long extraEu) {
        Objects.requireNonNull(itemId);

        if (extraEu <= 0) {
            throw new IllegalArgumentException("extraEu must be positive");
        }

        if (UPGRADES.containsKey(itemId)) {
            throw new IllegalArgumentException("Upgrade already registered:" + itemId);
        }

        UPGRADES.put(itemId, extraEu);
    }

    static {
        registerUpgrade(MIItem.BASIC_UPGRADE.asItem(), 2L);
        registerUpgrade(MIItem.ADVANCED_UPGRADE.asItem(), 8L);
        registerUpgrade(MIItem.TURBO_UPGRADE.asItem(), 32L);
        registerUpgrade(MIItem.HIGHLY_ADVANCED_UPGRADE.asItem(), 128L);
        registerUpgrade(MIItem.QUANTUM_UPGRADE.asItem(), 999999999L);

        KubeJSProxy.instance.fireRegisterUpgradesEvent();
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.put("upgradesItemStack", itemStack.save(new CompoundTag()));
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        itemStack = ItemStack.of(tag.getCompound("upgradesItemStack"));
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.isEmpty()) {
            return InteractionResult.PASS;
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

        return InteractionResult.PASS;
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
