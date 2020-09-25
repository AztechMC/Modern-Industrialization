package aztech.modern_industrialization.items.armor;

import alexiil.mc.lib.attributes.AttributeProviderItem;
import alexiil.mc.lib.attributes.ItemAttributeList;
import alexiil.mc.lib.attributes.fluid.FluidInsertable;
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount;
import alexiil.mc.lib.attributes.fluid.volume.FluidKey;
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import alexiil.mc.lib.attributes.misc.LimitedConsumer;
import alexiil.mc.lib.attributes.misc.Reference;
import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.mixin.ServerPlayNetworkHandlerAccessor;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import me.shedaniel.cloth.api.armor.v1.TickableArmor;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.math.RoundingMode;
import java.util.List;

public class JetpackItem extends ArmorItem implements Wearable, AttributeProviderItem, TickableArmor, DurabilityBarItem {
    private static int CAPACITY = 1000;
    public JetpackItem(Settings settings) {
        super(buildMaterial(), EquipmentSlot.CHEST, settings.maxCount(1));
    }

    public FluidKey getFluid(ItemStack stack) {
        CompoundTag fluidTag = stack.getSubTag("fluid");
        return fluidTag == null ? FluidKeys.EMPTY : FluidKey.fromTag(fluidTag);
    }

    public void setFluid(ItemStack stack, FluidKey fluid) {
        stack.getOrCreateTag().put("fluid", fluid.toTag());
    }

    public int getAmount(ItemStack stack) {
        try {
            return stack.getTag().getInt("amount");
        } catch (NullPointerException ignored) {
            return 0;
        }
    }

    public void setAmount(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("amount", amount);
        if(amount == 0) {
            setFluid(stack, FluidKeys.EMPTY);
        }
    }

    public int getCapacity() {
        return CAPACITY;
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
        to.offer((FluidInsertable) (fluidVolume, simulation) -> {
            FluidKey storedFluid = getFluid(stack.get());
            if(storedFluid.isEmpty() && FluidFuelRegistry.getBurnTicks(fluidVolume.getFluidKey()) != 0) {
                int inserted = Math.min(CAPACITY - getAmount(stack.get()), fluidVolume.amount().asInt(1000, RoundingMode.FLOOR));
                ItemStack copy = stack.get();
                setFluid(copy, fluidVolume.getFluidKey());
                setAmount(copy, inserted);
                if(!stack.set(copy, simulation)) {
                    return fluidVolume;
                }
                return fluidVolume.getFluidKey().withAmount(fluidVolume.amount().sub(FluidAmount.of(inserted, 1000)));
            }
            // TODO: implement partial filling
            return fluidVolume;
        });
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
        if(isActivated(stack)) {
            int amount = getAmount(stack);
            if (MIKeyMap.isHoldingUp(player) && amount > 0) {
                showParticles = true;
                double maxSpeed = Math.sqrt(FluidFuelRegistry.getBurnTicks(getFluid(stack))) / 5;
                double acceleration = 0.25;
                setAmount(stack, amount-1);
                Vec3d v = player.getVelocity();
                if(v.y < maxSpeed) {
                    player.setVelocity(v.x, Math.min(maxSpeed, v.y + acceleration), v.z);
                }
                if(!player.world.isClient()) {
                    player.fallDistance = 0;
                    if(player instanceof ServerPlayerEntity) {
                        ((ServerPlayNetworkHandlerAccessor) ((ServerPlayerEntity) player).networkHandler).setFloatingTicks(0);
                    }
                }
            }
        }

        if(!player.world.isClient()) {
            setParticles(stack, showParticles);
        }
    }

    @Override
    public double getDurabilityBarProgress(ItemStack stack) {
        return 1.0 - (double) getAmount(stack) / CAPACITY;
    }

    @Override
    public boolean hasDurabilityBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        Style style = Style.EMPTY.withColor(TextColor.fromRgb(0xa9a9a9)).withItalic(true);
        FluidKey fluid = getFluid(stack);
        if (!fluid.isEmpty()) {
            tooltip.add(getFluid(stack).name);
            String quantity = getAmount(stack) + " / " + getCapacity();
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_quantity", quantity).setStyle(style));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.fluid_slot_empty").setStyle(style));
        }
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return ImmutableMultimap.of();
    }
}
