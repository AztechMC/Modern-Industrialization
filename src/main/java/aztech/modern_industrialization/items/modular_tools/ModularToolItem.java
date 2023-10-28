/*
 * MIT License
 *
 * Copyright (c) 2023 Justin Hu
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
package aztech.modern_industrialization.items.modular_tools;

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.items.DynamicEnchantmentItem;
import aztech.modern_industrialization.items.DynamicToolItem;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ItemHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class ModularToolItem extends Item implements Vanishable, DynamicEnchantmentItem, DynamicToolItem {
    public ModularToolItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (state.getDestroySpeed(world, pos) != 0.0f) {
            consumeEnergy(stack);
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        consumeEnergy(stack);
        return true;
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        int requiredLevel = MiningLevelManager.getRequiredMiningLevel(state);
        return requiredLevel <= toolLevel(stack) && hasEnoughEnergy(stack) && isSupportedBlock(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state)) {
            return getMiningSpeedMultiplier(stack);
        }
        return 1;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && hasEnoughEnergy(stack)) {
            return ItemHelper.createToolModifiers(getAttackDamage(stack));
        }
        return ImmutableMultimap.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        // TODO: if fluid fuelled, add fluid tooltip; else add EU tooltip
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
    public InteractionResult useOn(UseOnContext context) {
        // TOOD: add handling for stripping, pathing, and tilling, depending on head
        return InteractionResult.PASS;
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEnchantments'");
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        // TODO: add handling for shearing, depending on head
        return InteractionResult.PASS;
    }

    /**
     * get energy capacity, in EU or in mB, depending on energy storage module
     */
    private static long getCapacity(ItemStack stack) {
        return 0; // TODO
    }

    /**
     * report consumed energy for one operation, in EU
     */
    private static long getConsumedEnergy(ItemStack stack) {
        // TODO
        return 0;
    }

    /**
     * consume one op's worth of energy; if using fluid fuels, rounds up to
     * nearest mB
     */
    private static void consumeEnergy(ItemStack stack) {
        // TODO
    }

    private static boolean hasEnoughEnergy(ItemStack stack) {
        switch (getEnergyType(stack)) {
        case FLUID: {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            if (fluid.getFluid() == Fluids.EMPTY) {
                return false;
            }

            long euPerMb = FluidFuelRegistry.getEu(fluid.getFluid());
            long mbRequired = (getConsumedEnergy(stack) + euPerMb - 1) / euPerMb; // do ceiling division
            return FluidFuelItemHelper.getAmount(stack) > mbRequired;
        }
        case ELECTRIC: {
            return SimpleEnergyItem.getStoredEnergyUnchecked(stack) > getConsumedEnergy(stack);
        }
        default: {
            return false;
        }
        }
    }

    private static int toolLevel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        // TODO: get level of tool from head type; steel = 2, aluminium = 3, stainless =
        // 4, titanium = 5
        return 0;
    }

    private static float getMiningSpeedMultiplier(ItemStack stack) {
        // TODO: calculate based on head and modules
        return 1.0f;
    }

    private static double getAttackDamage(ItemStack stack) {
        // TODO: calculate based on head and modules
        return 0.0;
    }

    private static double getDurabilityBarProgress(ItemStack stack) {
        switch (getEnergyType(stack)) {
        case FLUID: {
            return (double) FluidFuelItemHelper.getAmount(stack) / (double) getCapacity(stack);
        }
        case ELECTRIC: {
            return (double) SimpleEnergyItem.getStoredEnergyUnchecked(stack) / (double) getCapacity(stack);
        }
        default: {
            return 0.0;
        }
        }
    }

    public static EnergyType getEnergyType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return EnergyType.NONE;
        } else {
            return EnergyType.values()[tag.getByte("energyType")];
        }
    }

    public static enum EnergyType {
        NONE,
        FLUID,
        ELECTRIC
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
}
