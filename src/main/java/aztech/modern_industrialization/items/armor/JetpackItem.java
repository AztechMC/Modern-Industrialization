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
package aztech.modern_industrialization.items.armor;

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.IElytraItem;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.mixin.ServerPlayNetworkHandlerAccessor;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import me.shedaniel.cloth.api.armor.v1.TickableArmor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidKey;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class JetpackItem extends ArmorItem implements Wearable, TickableArmor, IElytraItem {
    public static final int CAPACITY = 4 * 81000;

    public JetpackItem(Settings settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.maxCount(1).rarity(Rarity.UNCOMMON));
    }

    public boolean isActivated(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("activated");
    }

    public void setActivated(ItemStack stack, boolean activated) {
        stack.getOrCreateTag().putBoolean("activated", activated);
    }

    @Override
    public boolean allowElytraFlight(ItemStack stack, LivingEntity user) {
        return isActivated(stack) && FluidFuelItemHelper.getAmount(stack) > 0;
    }

    private static ArmorMaterial buildMaterial() {
        return new ArmorMaterial() {
            @Override
            public int getDurability(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getProtectionAmount(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getEnchantability() {
                return 0;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }

            @Override
            public String getName() {
                return "modern_industrialization/diesel_jetpack";
            }

            @Override
            public float getToughness() {
                return 0;
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }
        };
    }

    @Override
    public void tickArmor(ItemStack stack, PlayerEntity player) {
        if (isActivated(stack)) {
            FluidKey fluid = FluidFuelItemHelper.getFluid(stack);
            long amount = FluidFuelItemHelper.getAmount(stack);
            if (amount > 0) {
                // Always consume one mb of fuel
                FluidFuelItemHelper.decrement(stack);
                if (MIKeyMap.isHoldingUp(player)) {
                    // Consume one more mb when pressing space
                    FluidFuelItemHelper.decrement(stack);
                    if (player.isFallFlying()) {
                        // Boost forward if fall flying
                        Vec3d playerFacing = player.getRotationVector();
                        Vec3d playerVelocity = player.getVelocity();
                        double maxSpeed = Math.sqrt(FluidFuelRegistry.getEu(fluid.getFluid())) / 10;
                        double attenuationFactor = 0.5;
                        player.setVelocity(playerVelocity.multiply(attenuationFactor).add(playerFacing.multiply(maxSpeed)));
                    } else {
                        // Otherwise boost vertically
                        double maxSpeed = Math.sqrt(FluidFuelRegistry.getEu(fluid.getFluid())) / 10;
                        double acceleration = 0.25;
                        Vec3d v = player.getVelocity();
                        if (v.y < maxSpeed) {
                            player.setVelocity(v.x, Math.min(maxSpeed, v.y + acceleration), v.z);
                        }
                        // Reset fall distance (but not in elytra mode)
                        if (!player.world.isClient()) {
                            player.fallDistance = 0;
                        }
                    }
                    if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) player).networkHandler).setFloatingTicks(0);
                    }
                }
            }
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    public boolean hasDurabilityBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }
}
