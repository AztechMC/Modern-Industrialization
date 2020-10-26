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
package aztech.modern_industrialization.items.diesel_tools;

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.api.DynamicEnchantmentItem;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import java.util.List;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Vanishable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// TODO: attack speed and damage
public class DieselToolItem extends Item
        implements DynamicAttributeTool, Vanishable, AttributeProviderItem, DurabilityBarItem, DynamicEnchantmentItem {
    public static final int CAPACITY = 4000;

    public DieselToolItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient && state.getHardness(world, pos) != 0.0f) {
            FluidFuelItemHelper.decrement(stack);
        }
        return super.postMine(stack, world, state, pos, miner);
    }

    @Override
    public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (isIn(tag)) {
            return getMiningSpeedMultiplier(stack);
        }
        return 0;
    }

    @Override
    public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (isIn(tag)) {
            return 4; // TODO: higher mining level?
        }
        return 0;
    }

    private float getMiningSpeedMultiplier(ItemStack stack) {
        int amount = FluidFuelItemHelper.getAmount(stack);
        if (amount > 0) {
            FluidKey fluid = FluidFuelItemHelper.getFluid(stack);
            int burnTicks = FluidFuelRegistry.getBurnTicks(fluid);
            if (burnTicks > 0) {
                return 1.0f + burnTicks / 2.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public void addAllAttributes(Reference<ItemStack> reference, LimitedConsumer<ItemStack> limitedConsumer, ItemAttributeList<?> to) {
        FluidFuelItemHelper.offerInsertable(reference, to, CAPACITY);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return false;
    }

    @Override
    public double getDurabilityBarProgress(ItemStack stack) {
        return 1.0 - (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    @Override
    public boolean hasDurabilityBar(ItemStack stack) {
        return true;
    }

    private static boolean isFortune(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("fortune");
    }

    private static void setFortune(ItemStack stack, boolean fortune) {
        if (fortune) {
            stack.getOrCreateTag().putBoolean("fortune", fortune);
        } else {
            stack.removeSubTag("fortune");
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Toggle between silk touch and fortune
        ItemStack stack = user.getStackInHand(hand);
        setFortune(stack, !isFortune(stack));
        return TypedActionResult.method_29237(stack, world.isClient);
    }

    @Override
    public int getLevel(Enchantment enchantment, ItemStack stack) {
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (enchantment == Enchantments.SILK_TOUCH && !isFortune(stack)) {
                return enchantment.getMaxLevel();
            } else if (enchantment == Enchantments.FORTUNE && isFortune(stack)) {
                return enchantment.getMaxLevel();
            }
        }
        return 0;
    }
}
