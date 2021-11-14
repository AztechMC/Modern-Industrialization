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
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BarrelItem extends BlockItem {

    private static final int ITEM_BAR_COLOR = MathHelper.packRgb(0.4F, 0.4F, 1.0F);
    public final long stackCapacity;

    public BarrelItem(Block block, long stackCapacity) {
        super(block, new Item.Settings().maxCount(1).group(ModernIndustrialization.ITEM_GROUP));
        this.stackCapacity = stackCapacity;
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getSubNbt("BlockEntityTag") == null;
    }

    public ItemVariant getItemVariant(ItemStack stack) {
        if (isEmpty(stack)) {
            return ItemVariant.blank();
        } else {
            return ItemVariant.fromNbt(stack.getSubNbt("BlockEntityTag").getCompound("item"));
        }
    }

    private void setItemVariant(ItemStack stack, ItemVariant item) {
        stack.getOrCreateSubNbt("BlockEntityTag").put("item", item.toNbt());
    }

    public long insert(ItemStack stackBarrel, ItemVariant inserted, long maxAmount) {
        StoragePreconditions.notBlankNotNegative(inserted, maxAmount);

        if (this.isEmpty(stackBarrel) || this.getItemVariant(stackBarrel).equals(inserted)) {
            long maxInsert;
            if (isEmpty(stackBarrel)) {
                maxInsert = stackCapacity * inserted.getItem().getMaxCount();
            } else {
                maxInsert = getCapacity(stackBarrel) - getAmount(stackBarrel);
            }

            long insertedAmount = Math.min(maxAmount, maxInsert);

            if (insertedAmount > 0) {
                setAmount(stackBarrel, getAmount(stackBarrel) + insertedAmount);
                setItemVariant(stackBarrel, inserted);
            }
            return insertedAmount;
        }
        return 0;
    }

    public long getAmount(ItemStack stack) {
        if (getItemVariant(stack).isBlank()) {
            return 0;
        }
        NbtCompound tag = stack.getSubNbt("BlockEntityTag");
        if (tag == null)
            return 0;
        else
            return tag.getLong("amt");
    }

    private void setAmount(ItemStack stack, long amount) {
        Preconditions.checkArgument(amount >= 0, "Can not set a barrel item to a negative amount");

        stack.getOrCreateSubNbt("BlockEntityTag").putLong("amt", amount);
        if (amount == 0) {
            stack.removeSubNbt("BlockEntityTag");
        }
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
            return Optional.of(new BarrelTooltipData(getItemVariant(stack), getAmount(stack), getCapacity(stack)));
        } else {
            return Optional.empty();
        }
    }

    public long getCapacity(ItemStack stack) {
        return stackCapacity * getItemVariant(stack).getItem().getMaxCount();

    }

    public boolean isItemBarVisible(ItemStack stack) {
        return getAmount(stack) > 0;
    }

    public int getItemBarStep(ItemStack stack) {
        return (int) Math.min(1 + (12 * getAmount(stack)) / getCapacity(stack), 13);
    }

    public int getItemBarColor(ItemStack stack) {
        return ITEM_BAR_COLOR;
    }

    public boolean onStackClicked(ItemStack stackBarrel, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT) {
            return false;
        } else {
            ItemStack itemStack = slot.getStack();
            if (itemStack.isEmpty() && !isEmpty(stackBarrel)) {
                long amount = Math.min(getAmount(stackBarrel), getItemVariant(stackBarrel).getItem().getMaxCount());
                ItemStack newStack = getItemVariant(stackBarrel).toStack((int) (amount));
                slot.setStack(newStack);
                setAmount(stackBarrel, getAmount(stackBarrel) - amount);
            } else if (!itemStack.isEmpty() && itemStack.getItem().canBeNested()) {
                itemStack.decrement((int) insert(stackBarrel, ItemVariant.of(itemStack), itemStack.getCount()));
            }
            return true;
        }
    }

    public boolean onClicked(ItemStack stackBarrel, ItemStack itemStack, Slot slot, ClickType clickType, PlayerEntity player,
            StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player)) {
            if (!itemStack.isEmpty()) {
                itemStack.decrement((int) insert(stackBarrel, ItemVariant.of(itemStack), itemStack.getCount()));
            }
            return true;
        } else {
            return false;
        }
    }

}
