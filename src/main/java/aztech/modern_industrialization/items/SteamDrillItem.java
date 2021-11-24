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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.api.DynamicEnchantmentItem;
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
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.tool.attribute.v1.DynamicAttributeTool;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.tag.Tag;
import net.minecraft.text.*;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * The steam drill. The item stack contains the following information:
 * burnTicks: integer, the remaining burn ticks of the fuel (as many as if the
 * fuel was used in a furnace). water: integer, the remaining ticks of water
 * (when full: 18000 ticks i.e. 15 minutes).
 */
public class SteamDrillItem extends Item implements DynamicAttributeTool, MagnaTool, DynamicEnchantmentItem {
    private static final int FULL_WATER = 18000;

    public SteamDrillItem(Settings settings) {
        super(settings.maxCount(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
        if (tag.contains(this) && canUse(stack, user)) {
            return 2;
        }
        return 0;
    }

    @Override
    public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {

        float speed = 1.0f;
        if (tag.contains(this) && canUse(stack, user)) {
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
        if (slot == EquipmentSlot.MAINHAND && canUse(stack, user)) {
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
        useFuel(stack, miner);
        return true;
    }

    @Override
    public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        useFuel(stack, attacker);
        return true;
    }

    private static void useFuel(ItemStack stack, LivingEntity user) {
        NbtCompound tag = stack.getNbt();
        if (tag != null && tag.getInt("water") > 0) {
            if (tag.getInt("burnTicks") == 0) {
                int burnTicks = consumeFuel(stack, user, Simulation.ACT);
                tag.putInt("burnTicks", burnTicks);
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
        // Flip NBT every tick to ensure that the attribute modifiers get updated if the
        // fuel next to the drill changes.
        tag.putBoolean("flip", !tag.getBoolean("flip"));
    }

    public static boolean canUse(ItemStack stack, @Nullable LivingEntity user) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || tag.getInt("water") == 0) {
            return false;
        }
        return tag.getInt("burnTicks") > 0 || consumeFuel(stack, user, Simulation.SIMULATE) > 0;
    }

    private static int consumeFuel(ItemStack stack, @Nullable LivingEntity user, Simulation simulation) {
        PlayerEntity player = CommonProxy.INSTANCE.findUser(user);
        if (player != null) {
            PlayerInventory inv = player.getInventory();
            int drillSlot = -1;
            for (int i = 0; i < 9; ++i) {
                if (inv.getStack(i) == stack) {
                    drillSlot = i;
                }
            }
            if (drillSlot == -1)
                return 0;
            for (int offset = -1; offset <= 1; offset += 2) {
                int adjSlot = drillSlot + offset;
                if (adjSlot < 0 || adjSlot >= 9)
                    continue;
                ItemStack adjStack = inv.getStack(adjSlot);
                Integer burnTicks = FuelRegistry.INSTANCE.get(adjStack.getItem());
                if (burnTicks != null && burnTicks > 0 && isFuelAllowed(adjStack)) {
                    if (simulation.isActing()) {
                        Item adjItem = adjStack.getItem();
                        adjStack.decrement(1);
                        if (adjItem.hasRecipeRemainder()) {
                            inv.setStack(adjSlot, new ItemStack(adjItem.getRecipeRemainder()));
                        }
                    }
                    return burnTicks;
                }
            }
        }
        return 0;
    }

    private static boolean isFuelAllowed(ItemStack fuelStack) {
        return !fuelStack.isDamageable();
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
            return Optional.of(new SteamDrillTooltipData(tag.getInt("water") * 100 / FULL_WATER, tag.getInt("burnTicks")));
        } else {
            return Optional.of(new SteamDrillTooltipData(0, 0));
        }

    }

    public record SteamDrillTooltipData(int waterLevel, int burnTicks) implements TooltipData {
    }

    public static class SteamDrillTooltipComponent implements TooltipComponent {

        final List<Text> text;
        final SteamDrillTooltipData data;

        public SteamDrillTooltipComponent(SteamDrillTooltipData data) {
            this.data = data;
            Text waterText = new TranslatableText("text.modern_industrialization.water_percent", data.waterLevel).setStyle(TextHelper.WATER_TEXT);
            int barWater = (int) Math.ceil(data.waterLevel / 5d);
            int barVoid = 20 - barWater;

            Text waterBar = new LiteralText("|".repeat(barWater)).setStyle(TextHelper.WATER_TEXT)
                    .append(new LiteralText("|".repeat(barVoid)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x6b6b6b))));

            Text burnTicks = new TranslatableText("text.modern_industrialization.seconds_left", data.burnTicks / 100).setStyle(TextHelper.GRAY_TEXT);

            Text line1 = new TranslatableText("text.modern_industrialization.steam_drill_water_help").setStyle(TextHelper.UPGRADE_TEXT);
            Text line2 = new TranslatableText("text.modern_industrialization.steam_drill_fuel_help").setStyle(TextHelper.UPGRADE_TEXT);
            Text line3 = new TranslatableText("text.modern_industrialization.steam_drill_profit").setStyle(TextHelper.UPGRADE_TEXT);

            if (data.burnTicks > 0) {
                text = List.of(waterText, waterBar, burnTicks, line1, line2, line3);
            } else {
                text = List.of(waterText, waterBar, line1, line2, line3);
            }

        }

        @Override
        public int getHeight() {
            return text.size() * 10;
        }

        @Override
        public int getWidth(TextRenderer textRenderer) {
            int max = 0;
            for (Text line : text) {
                max = Math.max(max, 5 + textRenderer.getWidth(line));
            }
            return max;
        }

        @Override
        public void drawText(TextRenderer textRenderer, int x, int y, Matrix4f matrix4f, VertexConsumerProvider.Immediate immediate) {
            int i = 0;
            for (Text line : text) {
                textRenderer.draw(line, x, y + i * 10, -1, true, matrix4f, immediate, false, 0, 15728880);
                i++;
            }

        }

        private static final Identifier texturePath = new MIIdentifier("textures/gui/progress_bar/furnace.png");

        @Override
        public void drawItems(TextRenderer textRenderer, int x, int y, MatrixStack matrices, ItemRenderer itemRenderer, int z,
                TextureManager textureManager) {

            if (data.burnTicks > 0) {
                RenderSystem.setShaderTexture(0, texturePath);
                int cx = 2 + Math.max(Math.max(textRenderer.getWidth(text.get(0)), textRenderer.getWidth(text.get(1))),
                        textRenderer.getWidth(text.get(2)));
                DrawableHelper.drawTexture(matrices, x + cx, y + 10, 0, 20, 20, 20, 20, 40);
            }
        }

    }
}
