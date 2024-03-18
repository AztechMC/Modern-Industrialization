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

import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.fluids.FluidType;

public class JetpackItem extends ArmorItem implements ActivatableChestItem {
    public static final int CAPACITY = 8 * FluidType.BUCKET_VOLUME;

    public JetpackItem(Properties settings) {
        super(buildMaterial(), Type.CHESTPLATE, settings.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public boolean canElytraFly(ItemStack stack, LivingEntity entity) {
        return isActivated(stack) && FluidFuelItemHelper.getAmount(stack) > 0;
    }

    @Override
    public boolean elytraFlightTick(ItemStack stack, LivingEntity entity, int flightTicks) {
        // Fluid consumption is handled in the armor tick.
        return true;
    }

    private static ArmorMaterial buildMaterial() {
        return new ArmorMaterial() {
            @Override
            public int getDurabilityForType(Type type) {
                return 0;
            }

            @Override
            public int getDefenseForType(Type type) {
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
                return "modern_industrialization:diesel_jetpack";
            }

            @Override
            public float getToughness() {
                return 0;
            }

            @Override
            public float getKnockbackResistance() {
                return 0;
            }

            @Override
            public String toString() {
                return getName().replace("/", ":");
            }
        };
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && stack == player.getItemBySlot(EquipmentSlot.CHEST)) {
            tickArmor(stack, player);
        }
    }

    private void tickArmor(ItemStack stack, Player player) {
        if (isActivated(stack) && !player.onGround()) {
            FluidVariant fluid = FluidFuelItemHelper.getFluid(stack);
            long amount = FluidFuelItemHelper.getAmount(stack);
            if (amount > 0) {
                // Always consume one mb of fuel
                FluidFuelItemHelper.decrement(stack);
                if (MIKeyMap.isHoldingUp(player)) {
                    // Consume one more mb when pressing space
                    FluidFuelItemHelper.decrement(stack);
                    if (player.isFallFlying()) {
                        // Boost forward if fall flying
                        Vec3 playerFacing = player.getLookAngle();
                        Vec3 playerVelocity = player.getDeltaMovement();
                        double maxSpeed = Math.sqrt(FluidFuel.getEu(fluid.getFluid()) / 200.0);
                        double attenuationFactor = 0.5;
                        player.setDeltaMovement(playerVelocity.scale(attenuationFactor).add(playerFacing.scale(maxSpeed)));
                    } else {
                        // Otherwise boost vertically
                        double maxSpeed = Math.sqrt(FluidFuel.getEu(fluid.getFluid()) / 200.0);
                        double acceleration = 0.25;
                        Vec3 v = player.getDeltaMovement();
                        if (v.y < maxSpeed) {
                            player.setDeltaMovement(v.x, Math.min(maxSpeed, v.y + acceleration), v.z);
                        }
                        // Reset fall distance (but not in elytra mode)
                        if (!player.level().isClientSide()) {
                            player.fallDistance = 0;
                        }
                    }
                    if (player instanceof ServerPlayer serverPlayer) {
                        ObfuscationReflectionHelper.setPrivateValue(ServerGamePacketListenerImpl.class, serverPlayer.connection, 0,
                                "aboveGroundTickCount");
                    }
                }
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) FluidFuelItemHelper.getAmount(stack) / CAPACITY;
    }

    public boolean hasDurabilityBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        Fluid fluid = FluidFuelItemHelper.getFluid(stack).getFluid();

        if (fluid instanceof MIFluid cf) {
            return cf.color;
        } else {
            return 0;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        FluidFuelItemHelper.appendTooltip(stack, tooltip, CAPACITY);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player,
            SlotAccess cursorStackReference) {
        // TODO NEO jetpack filling in inventory
//        if (clickType == ClickAction.SECONDARY) {
//            Storage<FluidVariant> jetpackStorage = getStackStorage(stack, player);
//            Storage<FluidVariant> cursorStorage = ContainerItemContext.ofPlayerCursor(player, player.containerMenu).find(FluidStorage.ITEM);
//
//            return StorageUtil.move(cursorStorage, jetpackStorage, fk -> true, Long.MAX_VALUE, null) > 0;
//        }
        return false;
    }

    // TODO NEO
//    @Nullable
//    private static Storage<FluidVariant> getStackStorage(ItemStack stack, Player player) {
//        Inventory inventory = player.getInventory();
//        ContainerItemContext context = null;
//
//        for (int i = 0; i < inventory.getContainerSize(); ++i) {
//            if (inventory.getItem(i) == stack) {
//                InventoryStorage wrapper = PlayerInventoryStorage.of(inventory);
//                context = ContainerItemContext.ofPlayerSlot(player, wrapper.getSlots().get(i));
//                break;
//            }
//        }
//
//        if (context != null) {
//            return context.find(FluidStorage.ITEM);
//        } else {
//            return null;
//        }
//    }
}
