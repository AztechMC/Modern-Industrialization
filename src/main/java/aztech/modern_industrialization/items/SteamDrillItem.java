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
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.systems.RenderSystem;
import draylar.magna.Magna;
import draylar.magna.api.MagnaTool;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.Tag;
import net.minecraft.text.*;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The steam drill. The item stack contains the following information:
 * burnTicks: integer, the remaining burn ticks of the fuel (as many as if the
 * fuel was used in a furnace). water: integer, the remaining ticks of water
 * (when full: 18000 ticks i.e. 15 minutes).
 */
public class SteamDrillItem extends Item implements DynamicAttributeTool, MagnaTool, DynamicEnchantmentItem, ItemContainingItemHelper, FabricItem {
    private static final int FULL_WATER = 18000;

    public SteamDrillItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean allowNbtUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

    @Override
    public boolean allowContinuingBlockBreaking(PlayerEntity player, ItemStack oldStack, ItemStack newStack) {
        return canUse(newStack);
    }

    @Override
    public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.contains(this) && canUse(stack)) {
            return 2;
        }
        return 0;
    }

    @Override
    public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        float speed = 1.0f;
        if (tag.contains(this) && canUse(stack)) {
            speed = 4.0f;
        }

        PlayerEntity player = CommonProxy.INSTANCE.findUser(user);

        if (Magna.CONFIG.breakSingleBlockWhenSneaking && player != null && player.isSneaking()) {
            speed *= 4f;
        }
        return speed;
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
        if (slot == EquipmentSlot.MAINHAND && canUse(stack)) {
            return ItemHelper.createToolModifiers(5);
        }
        return EMPTY;
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
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        useFuel(stack);
        return true;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        useFuel(stack);
        return true;
    }

    private void useFuel(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.getInt("water") > 0) {
            if (tag.getInt("burnTicks") == 0) {
                int burnTicks = consumeFuel(stack, Simulation.ACT);
                tag.putInt("burnTicks", burnTicks);
                tag.putInt("maxBurnTicks", burnTicks);
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return TypedActionResult.pass(itemStack);
        FluidState fluidState = world.getFluidState(hitResult.getBlockPos());
        if (fluidState.getFluid() == Fluids.WATER || fluidState.getFluid() == Fluids.FLOWING_WATER) {
            itemStack.getOrCreateNbt().putInt("water", FULL_WATER);
            return TypedActionResult.success(itemStack, world.isClient());
        }
        return super.use(world, user, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        NbtCompound tag = stack.getOrCreateNbt();
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
        NbtCompound tag = stack.getNbt();
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

                if (burnt.hasRecipeRemainder()) {
                    insert(stack, ItemVariant.of(burnt.getRecipeRemainder()), 1);
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

    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
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
    public boolean onStackClicked(ItemStack stackBarrel, Slot slot, ClickType clickType, PlayerEntity player) {
        return handleOnStackClicked(stackBarrel, slot, clickType, player);
    }

    @Override
    public boolean onClicked(ItemStack stackBarrel, ItemStack itemStack, Slot slot, ClickType clickType, PlayerEntity player,
            StackReference cursorStackReference) {
        return handleOnClicked(stackBarrel, itemStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public boolean canDirectInsert(ItemStack stack) {
        Integer fuelTime = FuelRegistry.INSTANCE.get(stack.getItem());
        return fuelTime != null && fuelTime > 0 && ItemContainingItemHelper.super.canDirectInsert(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        var data = (SteamDrillTooltipData) getTooltipData(stack).get();

        // Water %
        tooltip.add(new TranslatableText("text.modern_industrialization.water_percent", data.waterLevel).setStyle(TextHelper.WATER_TEXT));
        int barWater = (int) Math.ceil(data.waterLevel / 5d);
        int barVoid = 20 - barWater;
        // Water bar
        tooltip.add(new LiteralText("|".repeat(barWater)).setStyle(TextHelper.WATER_TEXT)
                .append(new LiteralText("|".repeat(barVoid)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6b6b6b)))));
        // Fuel left
        if (data.burnTicks > 0) {
            tooltip.add(new TranslatableText("text.modern_industrialization.seconds_left", data.burnTicks / 100).setStyle(TextHelper.GRAY_TEXT));
        }
        // Usage guide
        tooltip.add(new TranslatableText("text.modern_industrialization.steam_drill_water_help").setStyle(TextHelper.UPGRADE_TEXT));
        tooltip.add(new TranslatableText("text.modern_industrialization.steam_drill_fuel_help").setStyle(TextHelper.UPGRADE_TEXT));
        tooltip.add(new TranslatableText("text.modern_industrialization.steam_drill_profit").setStyle(TextHelper.UPGRADE_TEXT));
    }

    public record SteamDrillTooltipData(int waterLevel, int burnTicks, int maxBurnTicks, ItemVariant variant, long amount) implements TooltipData {
    }

    public static class SteamDrillTooltipComponent implements TooltipComponent {
        final SteamDrillTooltipData data;

        public SteamDrillTooltipComponent(SteamDrillTooltipData data) {
            this.data = data;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public int getWidth(TextRenderer textRenderer) {
            return 40;
        }

        @Override
        public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z) {
            // Slot background
            RenderSystem.setShaderTexture(0, MachineScreenHandlers.SLOT_ATLAS);
            DrawableHelper.drawTexture(matrices, x, y, 0, 0, 18, 18, 256, 256);
            // Stack itself
            ItemStack stack = data.variant.toStack((int) data.amount);
            itemRenderer.renderInGuiWithOverrides(stack, x + 1, y + 1);
            itemRenderer.renderGuiItemOverlay(textRenderer, stack, x + 1, y + 1);
            // Burning flame next to the stack
            var progressParams = new ProgressBar.Parameters(0, 0, "furnace", true);
            ProgressBar.RenderHelper.renderProgress(0, matrices, x + 20, y, progressParams, (float) data.burnTicks / data.maxBurnTicks);
        }
    }
}
