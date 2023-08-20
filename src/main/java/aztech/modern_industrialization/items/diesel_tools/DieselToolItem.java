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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.items.DynamicEnchantmentItem;
import aztech.modern_industrialization.items.DynamicToolItem;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ItemHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;

public class DieselToolItem extends Item implements Vanishable, DynamicEnchantmentItem, DynamicToolItem {
    public static final int CAPACITY = 4 * 81000;
    private final double damage;

    public DieselToolItem(Properties settings, double damage) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
        this.damage = damage;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(world, pos) != 0.0f) {
            FluidFuelItemHelper.decrement(stack);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        FluidFuelItemHelper.decrement(stack);
        return true;
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        int requiredLevel = MiningLevelManager.getRequiredMiningLevel(state);
        return requiredLevel <= 4 && FluidFuelItemHelper.getAmount(stack) > 0 && isSupportedBlock(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state)) {
            return getMiningSpeedMultiplier(stack);
        }
        return 1;
    }

    private float getMiningSpeedMultiplier(ItemStack stack) {
        long amount = FluidFuelItemHelper.getAmount(stack);
        if (amount > 0) {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            int burnTicks = FluidFuelRegistry.getEu(fluid.getFluid());
            if (burnTicks > 0) {
                return 1.0f + burnTicks / 8.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && FluidFuelItemHelper.getAmount(stack) > 0) {
            return ItemHelper.createToolModifiers(damage * FluidFuelRegistry.getEu(FluidFuelItemHelper.getFluid(stack).getFluid()) / 600);
        }
        return ImmutableMultimap.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Fluid fluid = FluidFuelItemHelper.getFluid(stack).getFluid();

        if (fluid instanceof MIFluid cf) {
            return cf.color;
        } else {
            return 0;
        }
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    private static boolean isFortune(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("fortune");
    }

    private static void setFortune(ItemStack stack, boolean fortune) {
        if (fortune) {
            stack.getOrCreateTag().putBoolean("fortune", true);
        } else {
            stack.removeTagKey("fortune");
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        // Toggle between silk touch and fortune
        if (hand == InteractionHand.MAIN_HAND && user.isShiftKeyDown()) {
            ItemStack stack = user.getItemInHand(hand);
            setFortune(stack, !isFortune(stack));
            if (!world.isClientSide) {
                user.displayClientMessage(
                        isFortune(stack) ? MIText.ToolSwitchedFortune.text() : MIText.ToolSwitchedSilkTouch.text(),
                        true);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }
        return super.use(world, user, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level w = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = w.getBlockState(pos);
        Player player = context.getPlayer();
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (stack.is(ItemTags.AXES)) {
                Block newBlock = StrippingAccess.getStrippedBlocks().get(state.getBlock());
                if (newBlock != null) {
                    w.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)), 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
            }
            if (stack.is(ItemTags.SHOVELS)) {
                BlockState newState = PathingAccess.getPathStates().get(state.getBlock());
                if (newState != null) {
                    w.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newState, 11);
                        FluidFuelItemHelper.decrement(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
            }
            if (stack.is(ItemTags.HOES)) {
                Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItem.TILLABLES.get(state.getBlock());
                if (pair != null) {
                    Predicate<UseOnContext> predicate = pair.getFirst();
                    Consumer<UseOnContext> consumer = pair.getSecond();
                    if (predicate.test(context)) {
                        w.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!w.isClientSide) {
                            consumer.accept(context);
                            if (player != null) {
                                FluidFuelItemHelper.decrement(stack);
                            }
                        }

                        return InteractionResult.sidedSuccess(w.isClientSide);
                    }
                }
            }

        }
        return super.useOn(context);
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        Reference2IntMap<Enchantment> map = new Reference2IntArrayMap<>();
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (!isFortune(stack)) {
                map.put(Enchantments.SILK_TOUCH, Enchantments.SILK_TOUCH.getMaxLevel());
            } else {
                map.put(Enchantments.BLOCK_FORTUNE, Enchantments.BLOCK_FORTUNE.getMaxLevel());
            }
        }
        return map;
    }

    private static class StrippingAccess extends AxeItem {
        private StrippingAccess(Tier material, float attackDamage, float attackSpeed, Properties settings) {
            super(material, attackDamage, attackSpeed, settings);
        }

        public static Map<Block, Block> getStrippedBlocks() {
            return STRIPPABLES;
        }
    }

    private static class PathingAccess extends ShovelItem {
        private PathingAccess(Tier material, float attackDamage, float attackSpeed, Properties settings) {
            super(material, attackDamage, attackSpeed, settings);
        }

        public static Map<Block, BlockState> getPathStates() {
            return FLATTENABLES;
        }
    }

    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {

        final int defaultMb = 100;
        float speedMultiplier = this.getMiningSpeedMultiplier(stack);
        int costMb = (int) (defaultMb / speedMultiplier);

        if (FluidFuelItemHelper.getAmount(stack) >= costMb) {
            if (stack.is(ConventionalItemTags.SHEARS) && interactionTarget instanceof Shearable shearable) {
                if (!interactionTarget.level().isClientSide && shearable.readyForShearing()) {
                    shearable.shear(SoundSource.PLAYERS);
                    interactionTarget.gameEvent(GameEvent.SHEAR, player);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }
}
