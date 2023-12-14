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
package aztech.modern_industrialization.items;

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.draylar.magna.Magna;
import dev.draylar.magna.api.MagnaTool;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.mutable.Mutable;
import org.jetbrains.annotations.Nullable;

/**
 * The steam drill. The item stack contains the following information:
 * burnTicks: integer, the remaining burn ticks of the fuel (as many as if the
 * fuel was used in a furnace). water: integer, the remaining ticks of water
 * (when full: 18000 ticks i.e. 15 minutes).
 */
public class SteamDrillItem
        extends Item
        implements DynamicToolItem, MagnaTool, DynamicEnchantmentItem, ItemContainingItemHelper {

    public static final StorageBehaviour<ItemVariant> DRILL_BEHAVIOUR = new StorageBehaviour<>() {
        @Override
        public long getCapacityForResource(ItemVariant resource) {
            return resource.getItem().getMaxStackSize();
        }

        public boolean canInsert(ItemVariant item) {
            Integer burnTicks = FuelRegistry.INSTANCE.get(item.getItem());
            return burnTicks != null && burnTicks > 0;
        }

    };

    private static final int FULL_WATER = 18000;

    public SteamDrillItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    private static boolean isNotSilkTouch(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("nosilk");
    }

    private static void setSilkTouch(ItemStack stack, boolean silkTouch) {
        if (silkTouch) {
            stack.removeTagKey("nosilk");
        } else {
            stack.getOrCreateTag().putBoolean("nosilk", true);
        }
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(Player player, ItemStack oldStack, ItemStack newStack) {
        return canUse(newStack);
    }

    @Override
    public boolean isSuitableFor(ItemStack stack, BlockState state) {
        int requiredLevel = MiningLevelManager.getRequiredMiningLevel(state);
        return requiredLevel <= 4 && canUse(stack) && isSupportedBlock(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (canUse(stack)) {
            if (isSuitableFor(stack, state)) {
                float speed = 4.0f;

                Player player = CommonProxy.INSTANCE.findUser(stack);

                if (Magna.CONFIG.breakSingleBlockWhenSneaking && player != null && player.isShiftKeyDown()) {
                    speed *= 4f;
                }
                return speed;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(ItemStack stack, EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND && canUse(stack)) {
            return ItemHelper.createToolModifiers(5);
        }
        return ImmutableMultimap.of();
    }

    @Override
    public int getRadius(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean playBreakEffects() {
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        useFuel(stack, miner);
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        useFuel(stack, attacker);
        return true;
    }

    private void useFuel(ItemStack stack, @Nullable LivingEntity entity) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getInt("water") > 0) {
            if (tag.getInt("burnTicks") == 0) {
                int burnTicks = consumeFuel(stack, Simulation.ACT);
                tag = stack.getOrCreateTag(); // consumeFuel might cause the tag to change
                tag.putInt("burnTicks", burnTicks);
                tag.putInt("maxBurnTicks", burnTicks);

                if (burnTicks > 0 && entity != null) {
                    // Play cool sound
                    entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f,
                            1.0f);
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        // Enable or disable silk touch
        if (hand == InteractionHand.MAIN_HAND && user.isShiftKeyDown()) {
            ItemStack stack = user.getItemInHand(hand);
            setSilkTouch(stack, isNotSilkTouch(stack));
            if (!world.isClientSide) {
                user.displayClientMessage(
                        isNotSilkTouch(stack) ? MIText.ToolSwitchedNoSilkTouch.text() : MIText.ToolSwitchedSilkTouch.text(), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
        }

        // Refill water
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(itemStack);
        FluidState fluidState = world.getFluidState(hitResult.getBlockPos());
        if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER) {
            fillWater(user, itemStack);
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        }

        return super.use(world, user, hand);
    }

    private void fillWater(Player player, ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (tag.getInt("water") != FULL_WATER) {
            tag.putInt("water", FULL_WATER);
            player.playNotifySound(SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        CompoundTag tag = stack.getOrCreateTag();
        int burnTicks = tag.getInt("burnTicks");
        if (burnTicks > 0) {
            NbtHelper.putNonzeroInt(tag, "burnTicks", Math.max(0, burnTicks - 5));
            NbtHelper.putNonzeroInt(tag, "water", Math.max(0, tag.getInt("water") - 5));
        }
        if (tag.getInt("burnTicks") == 0) {
            tag.remove("maxBurnTicks");
        }
        if (tag.getInt("water") == 0) {
            if (entity instanceof Player player) {
                var inv = player.getInventory();
                for (int i = 0; i < inv.getContainerSize(); ++i) {
                    if (tryFillWater(player, stack, inv.getItem(i))) {
                        break;
                    }
                }
            }
        }
    }

    public boolean canUse(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getInt("water") == 0) {
            return false;
        }
        return tag.getInt("burnTicks") > 0 || consumeFuel(stack, Simulation.SIMULATE) > 0;
    }

    private int consumeFuel(ItemStack stack, Simulation simulation) {
        Integer burnTicks = FuelRegistry.INSTANCE.get(getResource(stack).getItem());
        if (burnTicks != null && burnTicks > 0) {
            if (simulation.isActing()) {
                Item burnt = getResource(stack).getItem();
                setAmount(stack, getAmount(stack) - 1);

                if (burnt.hasCraftingRemainingItem()) {
                    try (Transaction tx = Transaction.openOuter()) {
                        var storage = GenericItemStorage.of(stack, this);
                        storage.insert(
                                ItemVariant.of(burnt.getCraftingRemainingItem()),
                                1,
                                tx,
                                true,
                                true);
                        tx.commit();
                    }

                }
            }
            return burnTicks;
        }
        return 0;
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        Reference2IntMap<Enchantment> map = new Reference2IntOpenHashMap<>();
        if (!isNotSilkTouch(stack)) {
            map.put(Enchantments.SILK_TOUCH, 1);
        }
        return map;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return Optional.of(new SteamDrillTooltipData(tag.getInt("water") * 100 / FULL_WATER, tag.getInt("burnTicks"), tag.getInt("maxBurnTicks"),
                    getResource(stack), getAmount(stack)));
        } else {
            return Optional.of(new SteamDrillTooltipData(0, 0, 1, ItemVariant.blank(), 0));
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stackBarrel, Slot slot, ClickAction clickType, Player player) {
        return handleStackedOnOther(stackBarrel, slot, clickType, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stackBarrel, ItemStack itemStack, Slot slot, ClickAction clickType, Player player,
            SlotAccess cursorStackReference) {
        return handleOtherStackedOnMe(stackBarrel, itemStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public boolean handleClick(Player player, ItemStack barrelLike, Mutable<ItemStack> otherStack) {
        // Try to refill water first if it's contained in the other stack
        if (tryFillWater(player, barrelLike, otherStack.getValue())) {
            return true;
        }

        return ItemContainingItemHelper.super.handleClick(player, barrelLike, otherStack);
    }

    private boolean tryFillWater(Player player, ItemStack barrelLike, ItemStack fillSource) {
        var otherStorage = ContainerItemContext.withConstant(fillSource).find(FluidStorage.ITEM);

        if (otherStorage != null) {
            long totalWater = 0;
            for (var view : otherStorage) {
                if (view.getResource().isOf(Fluids.WATER)) {
                    totalWater += view.getAmount();
                }
            }

            if (totalWater * fillSource.getCount() >= FluidConstants.BUCKET) {
                fillWater(player, barrelLike);
                return true;
            }
        }

        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        var data = (SteamDrillTooltipData) getTooltipImage(stack).get();

        // Water %
        tooltip.add(MIText.WaterPercent.text(data.waterLevel).setStyle(TextHelper.WATER_TEXT));
        int barWater = (int) Math.ceil(data.waterLevel / 5d);
        int barVoid = 20 - barWater;
        // Water bar
        tooltip.add(Component.literal("|".repeat(barWater)).setStyle(TextHelper.WATER_TEXT)
                .append(Component.literal("|".repeat(barVoid)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6b6b6b)))));
        // Fuel left
        if (data.burnTicks > 0) {
            tooltip.add(MIText.SecondsLeft.text(data.burnTicks / 100).setStyle(TextHelper.GRAY_TEXT));
        }
    }

    @Override
    public StorageBehaviour<ItemVariant> getBehaviour() {
        return DRILL_BEHAVIOUR;
    }

    public record SteamDrillTooltipData(int waterLevel, int burnTicks, int maxBurnTicks, ItemVariant variant, long amount)
            implements TooltipComponent {
    }
}
