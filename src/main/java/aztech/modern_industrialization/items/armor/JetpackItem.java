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

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.mixin.ServerPlayNetworkHandlerAccessor;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import me.shedaniel.cloth.api.armor.v1.TickableArmor;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
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

public class JetpackItem extends ArmorItem implements Wearable, AttributeProviderItem, TickableArmor, DurabilityBarItem {
    static final int CAPACITY = 4000;

    public JetpackItem(Settings settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.maxCount(1).rarity(Rarity.UNCOMMON));
    }

    public boolean isActivated(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("activated");
    }

    public void setActivated(ItemStack stack, boolean activated) {
        stack.getOrCreateTag().putBoolean("activated", activated);
    }

    public boolean showParticles(ItemStack stack) {
        return stack.getTag() != null && stack.getTag().getBoolean("showParticles");
    }

    public void setParticles(ItemStack stack, boolean showParticles) {
        stack.getOrCreateTag().putBoolean("showParticles", showParticles);
    }

    @Override
    public void addAllAttributes(Reference<ItemStack> stack, LimitedConsumer<ItemStack> excess, ItemAttributeList<?> to) {
        FluidFuelItemHelper.offerInsertable(stack, to, CAPACITY);
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
                return "modern_industrialization/jetpack";
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
        boolean showParticles = false;
        if (isActivated(stack)) {
            int amount = FluidFuelItemHelper.getAmount(stack);
            if (MIKeyMap.isHoldingUp(player) && amount > 0) {
                showParticles = true;
                double maxSpeed = Math.sqrt(FluidFuelRegistry.getBurnTicks(FluidFuelItemHelper.getFluid(stack))) / 5;
                double acceleration = 0.25;
                FluidFuelItemHelper.setAmount(stack, amount - 1);
                Vec3d v = player.getVelocity();
                if (v.y < maxSpeed) {
                    player.setVelocity(v.x, Math.min(maxSpeed, v.y + acceleration), v.z);
                }
                if (!player.world.isClient()) {
                    player.fallDistance = 0;
                    if (player instanceof ServerPlayerEntity) {
                        ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) player).networkHandler).setFloatingTicks(0);
                    }
                }
            }
        }

        if (!player.world.isClient()) {
            setParticles(stack, showParticles);
        }
    }

    @Override
    public double getDurabilityBarProgress(ItemStack stack) {
        return 1.0 - (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    @Override
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
