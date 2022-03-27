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

import aztech.modern_industrialization.api.DynamicEnchantmentItem;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.components.sync.ProgressBar;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import draylar.magna.Magna;
import draylar.magna.api.MagnaTool;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import org.jetbrains.annotations.Nullable;

/**
 * The steam drill. The item stack contains the following information:
 * burnTicks: integer, the remaining burn ticks of the fuel (as many as if the
 * fuel was used in a furnace). water: integer, the remaining ticks of water
 * (when full: 18000 ticks i.e. 15 minutes).
 */
public class SteamDrillItem extends Item implements DynamicToolItem, MagnaTool, DynamicEnchantmentItem, ItemContainingItemHelper, FabricItem {
    private static final int FULL_WATER = 18000;

    public SteamDrillItem(Properties settings) {
        super(settings.stacksTo(1).rarity(Rarity.UNCOMMON));
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
        useFuel(stack);
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        useFuel(stack);
        return true;
    }

    private void useFuel(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getInt("water") > 0) {
            if (tag.getInt("burnTicks") == 0) {
                int burnTicks = consumeFuel(stack, Simulation.ACT);
                tag.putInt("burnTicks", burnTicks);
                tag.putInt("maxBurnTicks", burnTicks);
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        BlockHitResult hitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return InteractionResultHolder.pass(itemStack);
        FluidState fluidState = world.getFluidState(hitResult.getBlockPos());
        if (fluidState.getType() == Fluids.WATER || fluidState.getType() == Fluids.FLOWING_WATER) {
            itemStack.getOrCreateTag().putInt("water", FULL_WATER);
            return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
        }
        return super.use(world, user, hand);
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
    }

    public boolean canUse(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || tag.getInt("water") == 0) {
            return false;
        }
        return tag.getInt("burnTicks") > 0 || consumeFuel(stack, Simulation.SIMULATE) > 0;
    }

    private int consumeFuel(ItemStack stack, Simulation simulation) {
        Integer burnTicks = FuelRegistry.INSTANCE.get(getItemVariant(stack).getItem());
        if (burnTicks != null && burnTicks > 0) {
            if (simulation.isActing()) {
                Item burnt = getItemVariant(stack).getItem();
                setAmount(stack, getAmount(stack) - 1);

                if (burnt.hasCraftingRemainingItem()) {
                    insert(stack, ItemVariant.of(burnt.getCraftingRemainingItem()), 1);
                }
            }
            return burnTicks;
        }
        return 0;
    }

    @Override
    public Reference2IntMap<Enchantment> getEnchantments(ItemStack stack) {
        Reference2IntMap<Enchantment> map = new Reference2IntOpenHashMap<>();
        map.put(Enchantments.SILK_TOUCH, 1);
        return map;
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            return Optional.of(new SteamDrillTooltipData(tag.getInt("water") * 100 / FULL_WATER, tag.getInt("burnTicks"), tag.getInt("maxBurnTicks"),
                    getItemVariant(stack), getAmount(stack)));
        } else {
            return Optional.of(new SteamDrillTooltipData(0, 0, 1, ItemVariant.blank(), 0));
        }
    }

    @Override
    public long getStackCapacity() {
        return 1;
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
    public boolean canDirectInsert(ItemStack stack) {
        Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
        return fuelTime != null && fuelTime > 0 && ItemContainingItemHelper.super.canDirectInsert(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        var data = (SteamDrillTooltipData) getTooltipImage(stack).get();

        // Water %
        tooltip.add(new TranslatableComponent("text.modern_industrialization.water_percent", data.waterLevel).setStyle(TextHelper.WATER_TEXT));
        int barWater = (int) Math.ceil(data.waterLevel / 5d);
        int barVoid = 20 - barWater;
        // Water bar
        tooltip.add(new TextComponent("|".repeat(barWater)).setStyle(TextHelper.WATER_TEXT)
                .append(new TextComponent("|".repeat(barVoid)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6b6b6b)))));
        // Fuel left
        if (data.burnTicks > 0) {
            tooltip.add(new TranslatableComponent("text.modern_industrialization.seconds_left", data.burnTicks / 100).setStyle(TextHelper.GRAY_TEXT));
        }
        // Usage guide
        tooltip.add(new TranslatableComponent("text.modern_industrialization.steam_drill_water_help").setStyle(TextHelper.UPGRADE_TEXT));
        tooltip.add(new TranslatableComponent("text.modern_industrialization.steam_drill_fuel_help").setStyle(TextHelper.UPGRADE_TEXT));
        tooltip.add(new TranslatableComponent("text.modern_industrialization.steam_drill_profit").setStyle(TextHelper.UPGRADE_TEXT));
    }

    public record SteamDrillTooltipData(int waterLevel, int burnTicks, int maxBurnTicks, ItemVariant variant, long amount)
            implements TooltipComponent {
    }

    public static class SteamDrillTooltipComponent implements ClientTooltipComponent {
        final SteamDrillTooltipData data;

        public SteamDrillTooltipComponent(SteamDrillTooltipData data) {
            this.data = data;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public int getWidth(Font textRenderer) {
            return 40;
        }

        @Override
        public void renderImage(Font textRenderer, int x, int y, PoseStack matrices, ItemRenderer itemRenderer, int z) {
            // Slot background
            RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
            GuiComponent.blit(matrices, x, y, 0, 0, 18, 18, 256, 256);
            // Stack itself
            ItemStack stack = data.variant.toStack((int) data.amount);
            itemRenderer.renderAndDecorateItem(stack, x + 1, y + 1);
            itemRenderer.renderGuiItemDecorations(textRenderer, stack, x + 1, y + 1);
            // Burning flame next to the stack
            var progressParams = new ProgressBar.Parameters(0, 0, "furnace", true);
            ProgressBar.RenderHelper.renderProgress(0, matrices, x + 20, y, progressParams, (float) data.burnTicks / data.maxBurnTicks);
        }
    }
}
