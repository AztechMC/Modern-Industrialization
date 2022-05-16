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
package aztech.modern_industrialization.blocks.storage.tank;

import static aztech.modern_industrialization.ModernIndustrialization.ITEM_GROUP;

import aztech.modern_industrialization.util.FluidHelper;
import aztech.modern_industrialization.util.NbtHelper;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class TankItem extends BlockItem {

    public final long capacity;

    public TankItem(Block block, long capacity) {
        super(block, new Item.Properties().tab(ITEM_GROUP));
        this.capacity = capacity;
    }

    public void registerItemApi() {
        FluidStorage.ITEM.registerForItems(TankItemStorage::new, this);
    }

    public boolean isEmpty(ItemStack stack) {
        return stack.getTagElement("BlockEntityTag") == null;
    }

    public FluidVariant getFluid(ItemStack stack) {
        return NbtHelper.getFluidCompatible(stack.getTagElement("BlockEntityTag"), "fluid");
    }

    private void setFluid(ItemStack stack, FluidVariant fluid) {
        NbtHelper.putFluid(stack.getOrCreateTagElement("BlockEntityTag"), "fluid", fluid);
    }

    public long getAmount(ItemStack stack) {
        if (getFluid(stack).isBlank()) {
            return 0;
        }
        CompoundTag tag = stack.getTagElement("BlockEntityTag");
        if (tag == null)
            return 0;
        else
            return tag.getLong("amt");
    }

    private void setAmount(ItemStack stack, long amount) {
        stack.getOrCreateTagElement("BlockEntityTag").putLong("amt", amount);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        if (isEmpty(stack)) {
            tooltip.addAll(FluidHelper.getTooltipForFluidStorage(FluidVariant.blank(), 0, capacity));
        } else {
            tooltip.addAll(FluidHelper.getTooltipForFluidStorage(getFluid(stack), getAmount(stack), capacity));
        }
    }

    class TankItemStorage extends SingleVariantItemStorage<FluidVariant> {
        public TankItemStorage(ItemStack stack, ContainerItemContext context) {
            super(context);
        }

        @Override
        protected FluidVariant getBlankResource() {
            return FluidVariant.blank();
        }

        @Override
        protected FluidVariant getResource(ItemVariant currentVariant) {
            return getFluid(currentVariant.toStack());
        }

        @Override
        protected long getAmount(ItemVariant currentVariant) {
            return TankItem.this.getAmount(currentVariant.toStack());
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return capacity;
        }

        @Override
        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
            // TODO: Note that any enchantment or custom name is nuked, fix this?
            ItemStack stack = new ItemStack(currentVariant.getItem());
            if (!newResource.isBlank() && newAmount > 0) {
                setFluid(stack, newResource);
                setAmount(stack, newAmount);
            }
            return ItemVariant.of(stack);
        }
    }
}
