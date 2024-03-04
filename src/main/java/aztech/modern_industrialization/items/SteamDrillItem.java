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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.GeometryHelper;
import aztech.modern_industrialization.util.NbtHelper;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.items.ItemHandlerHelper;
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
        implements DynamicToolItem, ItemContainingItemHelper {

    public static final StorageBehaviour<ItemVariant> DRILL_BEHAVIOUR = new StorageBehaviour<>() {
        @Override
        public long getCapacityForResource(ItemVariant resource) {
            return resource.getItem().getMaxStackSize();
        }

        public boolean canInsert(ItemVariant item) {
            int burnTicks = CommonHooks.getBurnTime(item.toStack(), null);
            return burnTicks > 0;
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
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !newStack.is(this) || slotChanged;
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !newStack.is(this) || !canUse(newStack) || CommonProxy.INSTANCE.shouldSteamDrillForceBreakReset();
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (isSupportedBlock(stack, state) && canUse(stack) && TierSortingRegistry.isCorrectTierForDrops(Tiers.NETHERITE, state)) {
            return true;
        }
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (canUse(stack)) {
            if (isCorrectToolForDrops(stack, state)) {
                float speed = 4.0f;

                Player player = CommonProxy.INSTANCE.findUser(stack);

                if (player != null && player.isShiftKeyDown()) {
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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND && canUse(stack)) {
            return ItemHelper.createToolModifiers(5);
        }
        return ImmutableMultimap.of();
    }

    public record Area(BlockPos center, BlockPos corner1, BlockPos corner2) {
    }

    @Nullable
    public static Area getArea(BlockGetter level, Player player) {
        if (player.isShiftKeyDown()) {
            return null; // No area mining on sneak.
        }

        HitResult rayTraceResult = rayTraceSimple(level, player, 0);
        if (rayTraceResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockResult = (BlockHitResult) rayTraceResult;
            Direction facing = blockResult.getDirection();
            return getArea(blockResult.getBlockPos(), facing);
        }
        return null;
    }

    private static Area getArea(BlockPos pos, Direction hitFace) {
        int face = hitFace.get3DDataValue();
        var right = GeometryHelper.FACE_RIGHT[face];
        int rx = (int) right.x(), ry = (int) right.y(), rz = (int) right.z();
        var up = GeometryHelper.FACE_UP[face];
        int ux = (int) up.x(), uy = (int) up.y(), uz = (int) up.z();
        return new Area(
                pos,
                pos.offset(rx + ux, ry + uy, rz + uz),
                pos.offset(-rx - ux, -ry - uy, -rz - uz));
    }

    public static void forEachMineableBlock(BlockGetter world, Area area, LivingEntity miner, BiConsumer<BlockPos, BlockState> callback) {
        BlockPos.betweenClosed(area.corner1(), area.corner2()).forEach(blockPos -> {
            if (world.getBlockEntity(blockPos) != null && !area.center().equals(blockPos)) {
                return; // No block entities unless it's the center block.
            }
            if (!(miner instanceof Player)) {
                return;
            }

            BlockState tempState = world.getBlockState(blockPos);
            if (tempState.isAir())
                return;
            if (!tempState.is(BlockTags.MINEABLE_WITH_PICKAXE) && !tempState.is(BlockTags.MINEABLE_WITH_SHOVEL))
                return;
            if (tempState.getDestroySpeed(world, blockPos) < 0)
                return;
            callback.accept(blockPos, tempState);
        });
    }

    private static HitResult rayTraceSimple(BlockGetter world, Player living, float partialTicks) {
        double blockReachDistance = living.getBlockReach();
        Vec3 vec3d = living.getEyePosition(partialTicks);
        Vec3 vec3d1 = living.getViewVector(partialTicks);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return world.clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, living));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        useFuel(stack, miner);

        if (!(miner instanceof Player p)) {
            return false;
        }

        var area = getArea(world, p);
        if (area == null) {
            return false;
        }

        List<ItemStack> totalDrops = new ArrayList<>();
        forEachMineableBlock(world, area, miner, (blockPos, tempState) -> {
            Block block = tempState.getBlock();
            int xp = CommonHooks.onBlockBreakEvent(world, ((ServerPlayer) miner).gameMode.getGameModeForPlayer(), (ServerPlayer) miner,
                    blockPos);
            if (xp >= 0 && block.onDestroyedByPlayer(tempState, world, blockPos, (Player) miner, true, tempState.getFluidState())) {
                block.destroy(world, blockPos, tempState);
                Block.getDrops(tempState, (ServerLevel) world, blockPos, null, miner, stack).forEach(itemStack -> {
                    boolean combined = false;
                    for (ItemStack drop : totalDrops) {
                        if (ItemHandlerHelper.canItemStacksStack(drop, itemStack)) {
                            drop.setCount(drop.getCount() + itemStack.getCount());
                            combined = true;
                            break;
                        }
                    }
                    if (!combined) {
                        totalDrops.add(itemStack);
                    }
                });
                block.popExperience((ServerLevel) world, blockPos, xp);
            }
        });
        totalDrops.forEach(itemStack -> {
            Block.popResource(world, miner.blockPosition(), itemStack);
        });
        world.getEntitiesOfClass(ExperienceOrb.class,
                new AABB(Vec3.atLowerCornerOf(area.corner1()), Vec3.atLowerCornerOf(area.corner2())).inflate(1))
                .forEach(entityXPOrb -> entityXPOrb.teleportTo(miner.blockPosition().getX(), miner.blockPosition().getY(),
                        miner.blockPosition().getZ()));

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
        int burnTicks = CommonHooks.getBurnTime(getResource(stack).toStack(), null);
        if (burnTicks > 0) {
            if (simulation.isActing()) {
                var burnt = getResource(stack).toStack();
                setAmount(stack, getAmount(stack) - 1);

                if (burnt.hasCraftingRemainingItem()) {
                    new ItemHandler(stack, this)
                            .insertItem(0, burnt.getCraftingRemainingItem(), false, true, true);
                }
            }
            return burnTicks;
        }
        return 0;
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        return getAllEnchantments(stack).getOrDefault(enchantment, 0);
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Reference2IntMap<Enchantment> map = new Reference2IntOpenHashMap<>();
        if (!isNotSilkTouch(stack)) {
            map.put(Enchantments.SILK_TOUCH, 1);
        }
        return map;
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return !getAllEnchantments(pStack).isEmpty();
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
        var otherStorage = fillSource.getCapability(Capabilities.FluidHandler.ITEM);

        if (otherStorage != null) {
            long totalWater = 0;
            for (int tank = 0; tank < otherStorage.getTanks(); ++tank) {
                if (otherStorage.getFluidInTank(tank).getFluid() == Fluids.WATER) {
                    totalWater += otherStorage.getFluidInTank(tank).getAmount();
                }
            }

            if (totalWater * fillSource.getCount() >= FluidType.BUCKET_VOLUME) {
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

        if (!isNotSilkTouch(stack)) {
            tooltip.add(Enchantments.SILK_TOUCH.getFullname(1));
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
