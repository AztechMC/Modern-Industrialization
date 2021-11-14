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

import aztech.modern_industrialization.api.DynamicEnchantmentItem;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ItemHelper;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// TODO: attack speed and damage
public class DieselToolItem extends Item implements DynamicAttributeTool, Vanishable, DynamicEnchantmentItem {
    public static final int CAPACITY = 4 * 81000;
    private final double damage;

    public DieselToolItem(Settings settings, double damage) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON));
        this.damage = damage;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getHardness(world, pos) != 0.0f) {
            FluidFuelItemHelper.decrement(stack);
        }
        return true;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        FluidFuelItemHelper.decrement(stack);
        return true;
    }

    @Override
    public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.contains(this)) {
            return getMiningSpeedMultiplier(stack);
        }
        return 0;
    }

    @Override
    public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.contains(this) && FluidFuelItemHelper.getAmount(stack) > 0) {
            return 4; // TODO: higher mining level?
        }
        return 0;
    }

    private float getMiningSpeedMultiplier(ItemStack stack) {
        long amount = FluidFuelItemHelper.getAmount(stack);
        if (amount > 0) {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            int burnTicks = FluidFuelRegistry.getEu(fluid.getFluid());
            if (burnTicks > 0) {
                return 1.0f + burnTicks / 4.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot == EquipmentSlot.MAINHAND && FluidFuelItemHelper.getAmount(stack) > 0) {
            return ItemHelper.createToolModifiers(damage * FluidFuelRegistry.getEu(FluidFuelItemHelper.getFluid(stack).getFluid()) / 300);
        }
        return EMPTY;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        Fluid fluid = FluidFuelItemHelper.getFluid(stack).getFluid();

        if (fluid instanceof CraftingFluid cf) {
            return cf.color;
        } else {
            return 0;
        }
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    private static boolean isFortune(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.getBoolean("fortune");
    }

    private static void setFortune(ItemStack stack, boolean fortune) {
        if (fortune) {
            stack.getOrCreateNbt().putBoolean("fortune", true);
        } else {
            stack.removeSubNbt("fortune");
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        // Toggle between silk touch and fortune
        if (hand == Hand.MAIN_HAND && user.isSneaking()) {
            ItemStack stack = user.getStackInHand(hand);
            setFortune(stack, !isFortune(stack));
            if (!world.isClient) {
                user.sendMessage(new TranslatableText("text.modern_industrialization.tool_switched_" + (isFortune(stack) ? "fortune" : "silk_touch")),
                        false);
            }
            return TypedActionResult.success(stack, world.isClient);
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ItemStack stack = context.getStack();
        World w = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = w.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (FabricToolTags.AXES.contains(this)) {
                Block newBlock = StrippingAccess.getStrippedBlocks().get(state.getBlock());
                if (newBlock != null) {
                    w.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1, 1);
                    if (!w.isClient) {
                        w.setBlockState(pos, newBlock.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)), 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return ActionResult.success(w.isClient);
                }
            } else if (FabricToolTags.SHOVELS.contains(this)) {
                BlockState newState = PathingAccess.getPathStates().get(state.getBlock());
                if (newState != null) {
                    w.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1, 1);
                    if (!w.isClient) {
                        w.setBlockState(pos, newState, 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return ActionResult.success(w.isClient);
                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        Reference2IntMap<Enchantment> map = new Reference2IntArrayMap<>();
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (!isFortune(stack)) {
                map.put(Enchantments.SILK_TOUCH, Enchantments.SILK_TOUCH.getMaxLevel());
            } else {
                map.put(Enchantments.FORTUNE, Enchantments.FORTUNE.getMaxLevel());
            }
        }
        return map;
    }

    private static class StrippingAccess extends AxeItem {
        private StrippingAccess(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings);
        }

        public static Map<Block, Block> getStrippedBlocks() {
            return STRIPPED_BLOCKS;
        }
    }

    private static class PathingAccess extends ShovelItem {
        private PathingAccess(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
            super(material, attackDamage, attackSpeed, settings);
        }

        public static Map<Block, BlockState> getPathStates() {
            return PATH_STATES;
        }
    }
}
