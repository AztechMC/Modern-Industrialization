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

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.items.DynamicToolItem;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ItemHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

public class DieselToolItem extends Item implements DynamicToolItem {
    public static final int CAPACITY = 4 * FluidType.BUCKET_VOLUME;
    private final double damage;

    public DieselToolItem(Properties settings, double damage) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON).component(MIComponents.SILK_TOUCH, true));
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
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state) && FluidFuelItemHelper.getAmount(stack) > 0
                && !state.is(Tiers.NETHERITE.getIncorrectBlocksForDrops())) {
            return true;
        }
        return super.isCorrectToolForDrops(stack, state);
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
            int burnTicks = FluidFuel.getEu(fluid.getFluid());
            if (burnTicks > 0) {
                return 1.0f + burnTicks / 8.0f;
            }
        }
        return 1.0f;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            return ItemHelper.getToolModifiers(damage * FluidFuel.getEu(FluidFuelItemHelper.getFluid(stack).getFluid()) / 600);
        }
        return ItemAttributeModifiers.EMPTY;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);

        if (context.registries() != null) {
            var enchantmentRegistry = context.registries().lookupOrThrow(Registries.ENCHANTMENT);

            tooltip.add(MIText.MiningMode.text(enchantmentRegistry.get(isFortune(stack) ? Enchantments.FORTUNE : Enchantments.SILK_TOUCH)
                    .orElseThrow().value().description().copy().setStyle(TextHelper.NUMBER_TEXT)).setStyle(TextHelper.GRAY_TEXT.withItalic(false)));
        }
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
        return !stack.getOrDefault(MIComponents.SILK_TOUCH, false);
    }

    private static void setFortune(ItemStack stack, boolean fortune) {
        stack.set(MIComponents.SILK_TOUCH, !fortune);
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

    // TODO NEO override canPerformAction

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
                // TODO NEO: restore tilling
//                Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> pair = HoeItem.TILLABLES.get(state.getBlock());
//                if (pair != null) {
//                    Predicate<UseOnContext> predicate = pair.getFirst();
//                    Consumer<UseOnContext> consumer = pair.getSecond();
//                    if (predicate.test(context)) {
//                        w.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
//                        if (!w.isClientSide) {
//                            consumer.accept(context);
//                            if (player != null) {
//                                FluidFuelItemHelper.decrement(stack);
//                            }
//                        }
//
//                        return InteractionResult.sidedSuccess(w.isClientSide);
//                    }
//                }
            }

        }
        return super.useOn(context);
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return getAllEnchantments(stack, enchantment.unwrapLookup()).getLevel(enchantment);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        var map = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        if (FluidFuelItemHelper.getAmount(stack) > 0) {
            if (!isFortune(stack)) {
                lookup.get(Enchantments.SILK_TOUCH).ifPresent(h -> map.set(h, h.value().getMaxLevel()));
            } else {
                lookup.get(Enchantments.FORTUNE).ifPresent(h -> map.set(h, h.value().getMaxLevel()));
            }
        }
        return map.toImmutable();
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return FluidFuelItemHelper.getAmount(pStack) > 0;
    }

    private static class StrippingAccess extends AxeItem {
        private StrippingAccess(Tier material, Properties settings) {
            super(material, settings);
        }

        public static Map<Block, Block> getStrippedBlocks() {
            return STRIPPABLES;
        }
    }

    private static class PathingAccess extends ShovelItem {
        private PathingAccess(Tier material, Properties settings) {
            super(material, settings);
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
            if (stack.is(Tags.Items.TOOLS_SHEAR) && interactionTarget instanceof Shearable shearable) {
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
