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

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.TextHelper;
import io.github.ladysnake.pal.VanillaAbilities;
import java.util.List;
import me.shedaniel.cloth.api.armor.v1.TickableArmor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class GraviChestPlateItem extends ArmorItem implements Wearable, TickableArmor, ActivatableChestItem {
    public GraviChestPlateItem(Properties settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.stacksTo(1).rarity(Rarity.EPIC));
    }

    private static ArmorMaterial buildMaterial() {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForSlot(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getDefenseForSlot(EquipmentSlot slot) {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 0;
            }

            @Override
            public SoundEvent getEquipSound() {
                return SoundEvents.ARMOR_EQUIP_GENERIC;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return null;
            }

            @Override
            public String getName() {
                return "modern_industrialization/gravichestplate";
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

    public long getEnergy(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getLong("energy") : 0;
    }

    public void setEnergy(ItemStack stack, long energy) {
        if (energy == 0) {
            stack.removeTagKey("energy");
        } else {
            stack.getOrCreateTag().putLong("energy", energy);
        }
    }

    public static final long FLIGHT_COST = 1024;
    public static final long ENERGY_CAPACITY = 1 << 24;

    @Override
    public void tickArmor(ItemStack stack, Player player) {
        if (player.level.isClientSide())
            return;
        if (MIArmorEffects.SRC.grants(player, VanillaAbilities.ALLOW_FLYING) && player.getAbilities().flying) {
            setEnergy(stack, Math.max(0, getEnergy(stack) - FLIGHT_COST));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        boolean didSomething = false;
        for (Direction direction : Direction.values()) {
            if (EnergyApi.MOVEABLE.find(context.getLevel(), context.getClickedPos(),
                    context.getClickedFace()) instanceof EnergyExtractable extractable) {
                ItemStack stack = context.getItemInHand();
                long extracted = extractable.extractEnergy(ENERGY_CAPACITY - getEnergy(stack), Simulation.ACT);
                setEnergy(stack, getEnergy(stack) + extracted);
                didSomething = true;
            }
        }
        return didSomething ? InteractionResult.sidedSuccess(context.getLevel().isClientSide()) : InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        tooltip.add(TextHelper.getEuTextMaxed(getEnergy(stack), ENERGY_CAPACITY, true));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getEnergy(stack) / (double) ENERGY_CAPACITY * 13);
    }
}
