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
package aztech.modern_industrialization.blocks.storage.barrel;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.storage.AbstractStorageBlockItem;
import aztech.modern_industrialization.items.ItemContainingItemHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class BarrelItem extends AbstractStorageBlockItem<ItemVariant> implements ItemContainingItemHelper {

    private static final int ITEM_BAR_COLOR = Mth.color(0.4F, 0.4F, 1.0F);

    public BarrelItem(BarrelBlock block, Properties settings) {
        super(block, settings.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        if (behaviour instanceof BarrelBlock.BarrelStorage barrelStorage) {
            Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
            if (isEmpty(stack) && isUnlocked(stack)) {
                tooltip.add(MIText.Empty.text().setStyle(style));
                tooltip.add(MIText.BarrelStack.text(barrelStorage.stackCapacity).setStyle(TextHelper.YELLOW));
            }
        }
        super.appendHoverText(stack, world, tooltip, context);
    }

    public long getCurrentCapacity(ItemStack stack) {
        return behaviour.getCapacityForResource(getResource(stack));
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!getBehaviour().isCreative()) {
            if (!isEmpty(stack) || !isUnlocked(stack)) {
                return Optional.of(new BarrelTooltipData(getResource(stack), getAmount(stack),
                        getCurrentCapacity(stack), false));
            }
        } else if (!getResource(stack).isBlank()) {
            return Optional.of(new BarrelTooltipData(getResource(stack), -1,
                    -1, true));
        }
        return Optional.empty();
    }

    public boolean isBarVisible(ItemStack stack) {
        return !getBehaviour().isCreative() && getAmount(stack) > 0;
    }

    public int getBarWidth(ItemStack stack) {
        return (int) Math.min(1 + (12 * getAmount(stack)) / getCurrentCapacity(stack), 13);
    }

    public int getBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
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

}
