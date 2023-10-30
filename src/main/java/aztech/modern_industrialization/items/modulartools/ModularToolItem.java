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
package aztech.modern_industrialization.items.modulartools;

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.item.modular_tools.EnergyConverterRegistry;
import aztech.modern_industrialization.api.item.modular_tools.EnergyConverterRegistry.ConverterProperties;
import aztech.modern_industrialization.api.item.modular_tools.EnergyStorageRegistry;
import aztech.modern_industrialization.api.item.modular_tools.EnergyStorageRegistry.StorageProperties;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry;
import aztech.modern_industrialization.api.item.modular_tools.HeadRegistry.HeadProperties;
import aztech.modern_industrialization.api.item.modular_tools.ModuleRegistry;
import aztech.modern_industrialization.api.item.modular_tools.ModuleRegistry.CustomModuleEffect;
import aztech.modern_industrialization.api.item.modular_tools.ModuleRegistry.ModuleProperties;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.items.DynamicEnchantmentItem;
import aztech.modern_industrialization.items.DynamicToolItem;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.ItemHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.draylar.magna.api.MagnaTool;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.mininglevel.v1.FabricMineableTags;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantItemStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class ModularToolItem extends Item implements Vanishable, DynamicEnchantmentItem, DynamicToolItem, MagnaTool {
    public static final long BASE_ENERGY_CONSUMPTION = 32;
    private static final ItemStack SHEAR_STACK = new ItemStack(Items.SHEARS);

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
        return requiredLevel <= getHeadProps(stack).flatMap(props -> Optional.of(props.miningLevel())).orElse(-1)
                && hasEnoughEnergy(stack) && isSupportedBlock(stack, state);
    }

    @Override
    public boolean isSupportedBlock(ItemStack stack, BlockState state) {
        switch (getToolType(stack)) {
        case DRILL: {
            return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
        }
        case CHAINSAW: {
            return state.is(BlockTags.MINEABLE_WITH_AXE)
                    || (state.is(FabricMineableTags.SHEARS_MINEABLE)
                            || Items.SHEARS.getDestroySpeed(SHEAR_STACK, state) > 1.0f)
                    || state.is(FabricMineableTags.SWORD_MINEABLE)
                    || state.is(BlockTags.MINEABLE_WITH_HOE);
        }
        default: {
            return false;
        }
        }
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (!hasEnoughEnergy(stack)) {
            return 0.0f;
        }
        if (isSupportedBlock(stack, state)) {
            return getHeadProps(stack).map(props -> props.miningSpeed()).orElse(0.0f);
        }
        return 1.0f;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && hasEnoughEnergy(stack)) {
            return ItemHelper.createToolModifiers(getHeadProps(stack).map(props -> props.attackDamage()).orElse(0.0));
        }
        return ImmutableMultimap.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        switch (getEnergyType(stack)) {
        case FLUID: {
            FluidFuelItemHelper.appendTooltip(stack, tooltip, getCapacity(stack) / 1000);
            break;
        }
        default: {
            break;
        }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        switch (getEnergyType(stack)) {
        case FLUID: {
            return (int) Math
                    .round(13.0 * 1000.0 * (double) FluidFuelItemHelper.getAmount(stack)
                            / (double) getCapacity(stack));
        }
        case ELECTRIC: {
            return (int) Math.round(
                    13.0 * (double) SimpleEnergyItem.getStoredEnergyUnchecked(stack) / (double) getCapacity(stack));
        }
        default: {
            return 0;
        }
        }
    }

    @Override
    public int getBarColor(ItemStack stack) {
        switch (getEnergyType(stack)) {
        case FLUID: {
            if (FluidFuelItemHelper.getFluid(stack).getFluid() instanceof MIFluid cf) {
                return cf.color;
            }
            break;
        }
        case ELECTRIC: {
            return 0xff0000;
        }
        default: {
            break;
        }
        }
        return 0;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Level w = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = w.getBlockState(pos);
        Player player = context.getPlayer();

        if (hasEnoughEnergy(stack)) {
            switch (getToolType(stack)) {
            case DRILL: {
                BlockState newState = PathingAccess.getPathStates().get(state.getBlock());
                if (newState != null) {
                    w.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newState, 11);
                        consumeEnergy(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
                break;
            }
            case CHAINSAW: {
                Block newBlock = StrippingAccess.getStrippedBlocks().get(state.getBlock());
                if (newBlock != null) {
                    w.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1, 1);
                    if (!w.isClientSide) {
                        w.setBlock(pos, newBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS,
                                state.getValue(RotatedPillarBlock.AXIS)), 11);
                        consumeEnergy(stack);
                    }
                    return InteractionResult.sidedSuccess(w.isClientSide);
                }
                var pair = HoeItem.TILLABLES.get(state.getBlock());
                if (pair != null) {
                    Predicate<UseOnContext> predicate = pair.getFirst();
                    Consumer<UseOnContext> consumer = pair.getSecond();
                    if (predicate.test(context)) {
                        w.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!w.isClientSide) {
                            consumer.accept(context);
                            consumeEnergy(stack);
                        }

                        return InteractionResult.sidedSuccess(w.isClientSide);
                    }
                }
                break;
            }
            default: {
                break;
            }
            }
        }
        return super.useOn(context);
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        var map = new Reference2IntArrayMap<Enchantment>();
        if (!hasEnoughEnergy(stack)) {
            return map;
        }

        for (var addon : getAddonProps(stack)) {
            if (addon.isPresent() && addon.get().getFirst().enchantment() != null) {
                Enchantment enchantment = addon.get().getFirst().enchantment();
                map.put(enchantment, addon.get().getSecond() + map.getOrDefault(enchantment, 0));
            }
        }
        return map;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if (hasEnoughEnergy(stack)) {
            if (getToolType(stack) == ToolType.CHAINSAW && interactionTarget instanceof Shearable shearable) {
                if (!interactionTarget.level().isClientSide && shearable.readyForShearing()) {
                    shearable.shear(SoundSource.PLAYERS);
                    interactionTarget.gameEvent(GameEvent.SHEAR, player);
                    consumeEnergy(stack);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.CONSUME;
                }
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean playBreakEffects() {
        return true;
    }

    @Override
    public int getRadius(ItemStack stack) {
        int radius = 0;
        for (var addon : getAddonProps(stack)) {
            if (addon.isPresent() && addon.get().getFirst().customEffect() == CustomModuleEffect.AREA) {
                radius += addon.get().getSecond();
            }
        }
        return radius;
    }

    public static void rebuildTool(ItemStack stack) {
        // clear current energy storage
        SimpleEnergyItem.setStoredEnergyUnchecked(stack, 0);
        FluidFuelItemHelper.setAmount(stack, 0);

        switch (getToolType(stack)) {
        case DRILL: {
            stack.getOrCreateTag().putInt("CustomModelData", 1);
            break;
        }
        case CHAINSAW: {
            stack.getOrCreateTag().putInt("CustomModelData", 2);
            break;
        }
        default: {
            stack.getOrCreateTag().putInt("CustomModelData", 0);
            break;
        }
        }
    }

    private static Optional<ConverterProperties> getConverterProps(ItemStack stack) {
        Optional<Item> energyConverter = getComponent(stack, "energyConverter");
        return energyConverter
                .flatMap(item -> Optional.ofNullable(EnergyConverterRegistry.getProperties(item)));
    }

    private static Optional<StorageProperties> getStorageProps(ItemStack stack) {
        Optional<Item> energyStorage = getComponent(stack, "energyStorage");
        return energyStorage
                .flatMap(item -> Optional.ofNullable(EnergyStorageRegistry.getProperties(item)));
    }

    private static Optional<HeadProperties> getHeadProps(ItemStack stack) {
        Optional<Item> head = getComponent(stack, "head");
        return head.flatMap(item -> Optional.ofNullable(HeadRegistry.getProperties(item)));
    }

    private static List<Optional<Pair<ModuleProperties, Integer>>> getAddonProps(ItemStack stack) {
        List<Optional<ItemStack>> addons = new ArrayList<>();
        for (int idx = 0; idx < 5; ++idx) {
            addons.add(Optional.empty());
        }

        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return List.of();
        }

        CompoundTag addonsTag = tag.getCompound("addons");
        for (int idx = 0; idx < addons.size(); ++idx) {
            if (addonsTag.contains(Integer.toString(idx))) {
                CompoundTag addonTag = addonsTag.getCompound(Integer.toString(idx));
                String addonId = addonTag.getString("id");
                int amount = addonTag.getInt("amount");
                addons.set(idx,
                        Optional.of(new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(addonId)), amount)));
            }
        }
        return addons.stream()
                .map(module -> module.flatMap(
                        item -> {
                            var props = ModuleRegistry.getProperties(item.getItem());
                            if (props == null) {
                                return Optional.empty();
                            } else {
                                return Optional.of(new Pair<>(props, item.getCount()));
                            }
                        }))
                .toList();
    }

    private static Optional<Item> getComponent(ItemStack stack, String componentName) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return Optional.empty();
        }
        String componentId = tag.getString(componentName);
        if (componentId.equals("")) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.ITEM.get(new ResourceLocation(componentId)));
    }

    private static EnergyType getEnergyType(ItemStack stack) {
        var converter = getConverterProps(stack);
        var storage = getStorageProps(stack);
        if (converter.isPresent() && storage.isPresent()
                && converter.get().energyType() == storage.get().energyType()) {
            return converter.get().energyType();
        } else {
            return EnergyType.NONE;
        }
    }

    /**
     * consume one op's worth of energy; if using fluid fuels, rounds up to
     * nearest mB
     */
    private static void consumeEnergy(ItemStack stack) {
        if (hasEnoughEnergy(stack)) {
            switch (getEnergyType(stack)) {
            case FLUID: {
                FluidFuelItemHelper.setAmount(stack,
                        FluidFuelItemHelper.getAmount(stack) - 81 * getMbRequired(stack));
                break;
            }
            case ELECTRIC: {
                SimpleEnergyItem.setStoredEnergyUnchecked(stack,
                        SimpleEnergyItem.getStoredEnergyUnchecked(stack) - getConsumedEnergy(stack));
                break;
            }
            default: {
                throw new IllegalStateException("Must have a known energy type to consume any energy");
            }
            }
        }
    }

    private static boolean hasEnoughEnergy(ItemStack stack) {
        if (getConsumedEnergy(stack) > getConverterProps(stack).map(props -> props.maxEu()).orElse(0L)) {
            return false;
        }

        switch (getEnergyType(stack)) {
        case FLUID: {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            if (fluid.getFluid() == Fluids.EMPTY) {
                return false;
            }
            return FluidFuelItemHelper.getAmount(stack) > getMbRequired(stack);
        }
        case ELECTRIC: {
            return SimpleEnergyItem.getStoredEnergyUnchecked(stack) > getConsumedEnergy(stack);
        }
        default: {
            return false;
        }
        }
    }

    private static long getMbRequired(ItemStack stack) {
        FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
        long euPerMb = FluidFuelRegistry.getEu(fluid.getFluid());
        return (getConsumedEnergy(stack) + euPerMb - 1) / euPerMb; // do ceiling division
    }

    private static ToolType getToolType(ItemStack stack) {
        return getHeadProps(stack).map(props -> props.toolType()).orElse(ToolType.NONE);
    }

    /**
     * report consumed energy for one operation, in EU
     */
    private static long getConsumedEnergy(ItemStack stack) {
        double multiplier = 1;
        for (var addon : getAddonProps(stack)) {
            if (addon.isPresent()) {
                multiplier *= Math.pow(addon.get().getFirst().multiplier(), addon.get().getSecond());
            }
        }
        return Math.round(BASE_ENERGY_CONSUMPTION * multiplier);
    }

    /**
     * get energy capacity, in EU or in mB, depending on energy storage module
     */
    private static long getCapacity(ItemStack stack) {
        return getStorageProps(stack).map(props -> props.capacity()).orElse(0L);
    }

    public static enum EnergyType {
        NONE,
        FLUID,
        ELECTRIC
    }

    public static enum ToolType {
        NONE,
        DRILL,
        CHAINSAW
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

    public static class FluidStorage extends SingleVariantItemStorage<FluidVariant> {
        private ContainerItemContext context;

        public FluidStorage(ContainerItemContext context) {
            super(context);
            this.context = context;
        }

        @Override
        protected FluidVariant getBlankResource() {
            return FluidVariant.blank();
        }

        @Override
        protected FluidVariant getResource(ItemVariant currentVariant) {
            return FluidFuelItemHelper.getFluid(currentVariant.toStack());
        }

        @Override
        protected long getAmount(ItemVariant currentVariant) {
            return FluidFuelItemHelper.getAmount(currentVariant.toStack());
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            if (ModularToolItem.getEnergyType(context.getItemVariant().toStack()) == EnergyType.FLUID) {
                return ModularToolItem.getCapacity(context.getItemVariant().toStack()) / 1000;
            } else {
                return 0;
            }
        }

        @Override
        protected ItemVariant getUpdatedVariant(ItemVariant currentVariant, FluidVariant newResource, long newAmount) {
            ItemStack stack = currentVariant.toStack();
            FluidFuelItemHelper.setFluid(stack, newResource);
            FluidFuelItemHelper.setAmount(stack, newAmount);
            return ItemVariant.of(stack);
        }
    }

    public static class EnergyStorage implements team.reborn.energy.api.EnergyStorage {
        private ContainerItemContext context;

        public EnergyStorage(ContainerItemContext context) {
            this.context = context;
        }

        @Override
        public long insert(long maxAmount, TransactionContext transaction) {
            long toInsert = Math.min(maxAmount, getCapacity() - getAmount());
            if (trySetEnergy(getAmount() + toInsert, transaction)) {
                return toInsert;
            } else {
                return 0;
            }
        }

        @Override
        public long extract(long maxAmount, TransactionContext transaction) {
            long toExtract = Math.min(maxAmount, getAmount());
            if (trySetEnergy(getAmount() - toExtract, transaction)) {
                return toExtract;
            } else {
                return 0;
            }
        }

        private boolean trySetEnergy(long amount, TransactionContext transaction) {
            ItemStack newStack = context.getItemVariant().toStack();
            SimpleEnergyItem.setStoredEnergyUnchecked(newStack, amount);
            ItemVariant newVariant = ItemVariant.of(newStack);
            try (Transaction nested = transaction.openNested()) {
                if (context.extract(context.getItemVariant(), 1, nested) == 1
                        && context.insert(newVariant, 1, nested) == 1) {
                    nested.commit();
                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        public long getAmount() {
            if (ModularToolItem.getEnergyType(context.getItemVariant().toStack()) == EnergyType.ELECTRIC) {
                return SimpleEnergyItem.getStoredEnergyUnchecked(context.getItemVariant().toStack());
            } else {
                return 0;
            }
        }

        @Override
        public long getCapacity() {
            if (ModularToolItem.getEnergyType(context.getItemVariant().toStack()) == EnergyType.ELECTRIC) {
                return ModularToolItem.getCapacity(context.getItemVariant().toStack());
            } else {
                return 0;
            }
        }
    }
}
