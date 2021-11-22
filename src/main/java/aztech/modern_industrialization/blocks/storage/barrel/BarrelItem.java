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

import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.items.ItemContainingItemHelper;
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BarrelItem extends BlockItem implements ItemContainingItemHelper {

    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    public final long stackCapacity;

    public BarrelItem(Block block, long stackCapacity) {
        super(block, new Item.Settings().maxCount(1).group(ModernIndustrialization.ITEM_GROUP));
        this.stackCapacity = stackCapacity;
    }

    @Override
    public long getStackCapacity() {
        return stackCapacity;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(false);
        if (isEmpty(stack)) {
            tooltip.add(new TranslatableText("text.modern_industrialization.empty").setStyle(style));
            tooltip.add(new TranslatableText("text.modern_industrialization.barrel_stack", stackCapacity).setStyle(TextHelper.YELLOW));
        }
    }

    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (!isEmpty(stack)) {
            return Optional.of(new BarrelTooltipData(getItemVariant(stack), getAmount(stack), getCurrentCapacity(stack)));
        } else {
            return Optional.empty();
        }
    }

    public boolean isItemBarVisible(ItemStack stack) {
        return getAmount(stack) > 0;
    }

    public int getItemBarStep(ItemStack stack) {
        return (int) Math.min(1 + (12 * getAmount(stack)) / getCurrentCapacity(stack), 13);
    }

    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    @Override
    public boolean onStackClicked(ItemStack stackBarrel, Slot slot, ClickType clickType, PlayerEntity player) {
        return handleOnStackClicked(stackBarrel, slot, clickType, player);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player,
            StackReference cursorStackReference) {
        return handleOnClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }
}
