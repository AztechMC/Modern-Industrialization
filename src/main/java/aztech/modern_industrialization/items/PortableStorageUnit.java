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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import dev.technici4n.grandpower.api.ISimpleEnergyItem;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import java.util.Optional;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class PortableStorageUnit extends Item implements ItemContainingItemHelper {

    public static final Reference2LongMap<Item> CAPACITY_PER_BATTERY = new Reference2LongOpenHashMap<>();
    private final static int MAX_BATTERY_COUNT = 10000;

    public PortableStorageUnit(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void onChange(ItemStack stack) {
        this.setStoredEnergy(stack, Math.min(this.getEnergyCapacity(stack), this.getStoredEnergy(stack)));
    }

    public long getEnergyCapacity(ItemStack stack) {
        if (this.isEmpty(stack)) {
            return 0;
        } else {
            return CAPACITY_PER_BATTERY.getLong(this.getResource(stack).getItem()) * this.getAmount(stack);
        }
    }

    public long getEnergyMaxInput(ItemStack stack) {
        return Long.MAX_VALUE;
    }

    public long getEnergyMaxOutput(ItemStack stack) {
        return Long.MAX_VALUE;
    }

    @Override
    public StorageBehaviour<ItemVariant> getBehaviour() {

        return new StorageBehaviour<>() {
            @Override
            public long getCapacityForResource(ItemVariant resource) {
                return MAX_BATTERY_COUNT;
            }

            public boolean canInsert(ItemVariant maybeBattery) {
                return CAPACITY_PER_BATTERY.containsKey(maybeBattery.getItem());
            }
        };
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stackBarrel, Slot slot, ClickAction clickType, Player player) {
        return handleStackedOnOther(stackBarrel, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player,
            SlotAccess cursorStackReference) {
        return handleOtherStackedOnMe(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!isEmpty(stack)) {
            return Optional.of(new BarrelTooltipData(getResource(stack), getAmount(stack),
                    MAX_BATTERY_COUNT, false));
        }

        return Optional.empty();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getEnergyCapacity(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (getEnergyCapacity(stack) > 0) {
            return (int) Math.round(getStoredEnergy(stack) / (double) getEnergyCapacity(stack) * 13);
        } else {
            return 0;
        }
    }

    /**
     * @return The energy stored in the stack. Count is ignored.
     */
    public long getStoredEnergy(ItemStack stack) {
        return ISimpleEnergyItem.getStoredEnergyUnchecked(stack);
    }

    /**
     * Directly set the energy stored in the stack. Count is ignored.
     * It's up to callers to ensure that the new amount is >= 0 and <= capacity.
     */
    public void setStoredEnergy(ItemStack stack, long newAmount) {
        ISimpleEnergyItem.setStoredEnergyUnchecked(stack, newAmount);
    }
}
