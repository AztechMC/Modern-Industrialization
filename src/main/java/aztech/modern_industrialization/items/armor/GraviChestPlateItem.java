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
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GraviChestPlateItem extends ArmorItem implements Wearable, TickableArmor, ActivatableChestItem {
    public GraviChestPlateItem(Settings settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.maxCount(1).rarity(Rarity.EPIC));
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
        return stack.hasNbt() ? stack.getNbt().getLong("energy") : 0;
    }

    public void setEnergy(ItemStack stack, long energy) {
        if (energy == 0) {
            stack.removeSubNbt("energy");
        } else {
            stack.getOrCreateNbt().putLong("energy", energy);
        }
    }

    public static final long FLIGHT_COST = 1024;
    public static final long ENERGY_CAPACITY = 1 << 24;

    @Override
    public void tickArmor(ItemStack stack, PlayerEntity player) {
        if (player.world.isClient())
            return;
        if (MIArmorEffects.SRC.grants(player, VanillaAbilities.ALLOW_FLYING) && player.getAbilities().flying) {
            setEnergy(stack, Math.max(0, getEnergy(stack) - FLIGHT_COST));
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        boolean didSomething = false;
        for (Direction direction : Direction.values()) {
            if (EnergyApi.MOVEABLE.find(context.getWorld(), context.getBlockPos(), context.getSide()) instanceof EnergyExtractable extractable) {
                ItemStack stack = context.getStack();
                long extracted = extractable.extractEnergy(ENERGY_CAPACITY - getEnergy(stack), Simulation.ACT);
                setEnergy(stack, getEnergy(stack) + extracted);
                didSomething = true;
            }
        }
        return didSomething ? ActionResult.success(context.getWorld().isClient()) : ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(
                new LiteralText(TextHelper.formatEu(getEnergy(stack)) + " / " + TextHelper.formatEu(ENERGY_CAPACITY)).setStyle(TextHelper.GRAY_TEXT));
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(getEnergy(stack) / (double) ENERGY_CAPACITY * 13);
    }
}
