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

import aztech.modern_industrialization.MIComponents;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.util.GeometryHelper;
import aztech.modern_industrialization.util.Simulation;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
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
        implements DynamicToolItem, ItemContainingItemHelper, ActivatableItem {
    public static final StorageBehaviour<ItemVariant> DRILL_BEHAVIOUR = new StorageBehaviour<>() {
        @Override
        public long getCapacityForResource(ItemVariant resource) {
            return resource.getMaxStackSize();
        }

        public boolean canInsert(ItemVariant item) {
            int burnTicks = item.toStack().getBurnTime(null);
            return burnTicks > 0;
        }
    };

    private static final int FULL_WATER = 18000;

    public SteamDrillItem(Properties settings) {
        super(settings
                .stacksTo(1)
                .rarity(Rarity.UNCOMMON)
                .component(MIComponents.SILK_TOUCH, true)
                .component(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY)
                .component(MIComponents.WATER, 0));
    }

    private static boolean isNotSilkTouch(ItemStack stack) {
        return !stack.getOrDefault(MIComponents.SILK_TOUCH, true);
    }

    private static void setSilkTouch(ItemStack stack, boolean silkTouch) {
        stack.set(MIComponents.SILK_TOUCH, silkTouch);
    }

    @Override
    public boolean getDefaultActivatedState() {
        return true;
    }

    private boolean should3by3(ItemStack stack, Player player) {
        return this.isActivated(stack) &&
                !player.isShiftKeyDown();
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
        if (isSupportedBlock(stack, state) && canUse(stack) && !state.is(Tiers.NETHERITE.getIncorrectBlocksForDrops())) {
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

                if (player != null && !should3by3(stack, player)) {
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
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        if (canUse(stack)) {
            return ItemHelper.getToolModifiers(5);
        }
        return ItemAttributeModifiers.EMPTY;
    }

    public record Area(BlockPos center, BlockPos corner1, BlockPos corner2) {}

    @Nullable
    public Area getArea(BlockGetter level, Player player, ItemStack stack, boolean rayTraceOnly) {
        if (!should3by3(stack, player)) {
            return null;
        }

        if (!rayTraceOnly) {
            ClickedBlock clickedBlock = lastClickedBlock.get(player);
            if (clickedBlock != null) {
                return getArea(clickedBlock.pos(), clickedBlock.face());
            }
            // If for some reason we can't get a last clicked block, default to the raytrace check
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

    private static boolean isAreaMineableBlock(BlockGetter level, BlockState state, BlockPos pos) {
        return !state.isAir() &&
                (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) &&
                state.getDestroySpeed(level, pos) > 0;
    }

    public static void forEachMineableBlock(BlockGetter world, Area area, LivingEntity miner, BiConsumer<BlockPos, BlockState> callback) {
        if (!(miner instanceof Player)) {
            return;
        }

        BlockState centerState = world.getBlockState(area.center());
        if (!isAreaMineableBlock(world, centerState, area.center())) {
            return;
        }

        callback.accept(area.center(), centerState);

        BlockPos.betweenClosed(area.corner1(), area.corner2()).forEach(blockPos -> {
            if (world.getBlockEntity(blockPos) != null || // No block entities
                    area.center().equals(blockPos)) { // Ignore center block since it is handled above
                return;
            }

            BlockState tempState = world.getBlockState(blockPos);
            if (isAreaMineableBlock(world, tempState, blockPos)) {
                callback.accept(blockPos, tempState);
            }
        });
    }

    private static HitResult rayTraceSimple(BlockGetter world, Player living, float partialTicks) {
        double blockReachDistance = living.blockInteractionRange();
        Vec3 vec3d = living.getEyePosition(partialTicks);
        Vec3 vec3d1 = living.getViewVector(partialTicks);
        Vec3 vec3d2 = vec3d.add(vec3d1.x * blockReachDistance, vec3d1.y * blockReachDistance, vec3d1.z * blockReachDistance);
        return world.clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, living));
    }

    // Use this little trick to cancel the drops for blocks broken by the steam drill, and instead merge them into a global list.
    @Nullable
    private static List<ItemStack> totalDrops = null;

    // Use this trick to use a more accurate block face (especially when on a remote server).
    private static final WeakHashMap<Player, ClickedBlock> lastClickedBlock = new WeakHashMap<>();

    static {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SteamDrillItem::mergeDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, SteamDrillItem::trackClickedFace);
    }

    private static void mergeDrops(BlockDropsEvent event) {
        if (totalDrops == null) {
            return;
        }

        outer:
        for (var entity : event.getDrops()) {
            if (entity.getItem().isEmpty()) {
                continue;
            }
            for (ItemStack drop : totalDrops) {
                if (ItemStack.isSameItemSameComponents(entity.getItem(), drop)) {
                    drop.grow(entity.getItem().getCount());
                    continue outer;
                }
            }
            totalDrops.add(entity.getItem());
        }
        event.getDrops().clear();
    }

    private static void trackClickedFace(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            PlayerInteractEvent.LeftClickBlock.Action action = event.getAction();
            if (action == PlayerInteractEvent.LeftClickBlock.Action.START ||
                    action == PlayerInteractEvent.LeftClickBlock.Action.STOP) {
                lastClickedBlock.put(player, new ClickedBlock(event.getPos().immutable(), event.getFace()));
            } else if (action == PlayerInteractEvent.LeftClickBlock.Action.ABORT) {
                lastClickedBlock.remove(player);
            }
        }
    }

    private record ClickedBlock(BlockPos pos, Direction face) {}

    private static final ThreadLocal<Boolean> recursiveMineBlock = ThreadLocal.withInitial(() -> false);

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity miner) {
        useFuel(stack, miner);

        if (!(miner instanceof ServerPlayer p)) {
            return false;
        }

        if (recursiveMineBlock.get()) {
            // Avoid endless recursion
            return false;
        }

        var area = getArea(world, p, stack, false);
        if (area == null) {
            return false;
        }
        lastClickedBlock.remove(p);

        totalDrops = new ArrayList<>();
        recursiveMineBlock.set(true);
        try {
            forEachMineableBlock(world, area, miner, (blockPos, tempState) -> {
                p.gameMode.destroyBlock(blockPos);
            });
        } finally {
            recursiveMineBlock.set(false);
        }
        totalDrops.forEach(itemStack -> {
            // Create an ItemEntity for the stack, and fire the pickup event.
            // This lets Sophisticated Backpacks (SB) and similar mods try to intercept the stack.
            // The stacks can have more than the max stack size, let's hope it won't cause issues.
            var itemEntity = new ItemEntity(world, p.getX(), p.getY(), p.getZ(), itemStack);
            // Set delay to 0 since we will pick up the item immediately. Otherwise, SB won't process the entity.
            itemEntity.setNoPickUpDelay();
            var pickupEvent = new ItemEntityPickupEvent.Pre(p, itemEntity);
            NeoForge.EVENT_BUS.post(pickupEvent);

            // Ignore canPickup and assume that mods correctly updated the item entity.
            // Let's at least check isRemoved() just in case, but I am not sure if it's needed.
            if (!itemEntity.isRemoved()) {
                ItemHandlerHelper.giveItemToPlayer(p, itemEntity.getItem());
            }
        });
        totalDrops = null;
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
        if (stack.getOrDefault(MIComponents.WATER, 0) > 0) {
            if (stack.getOrDefault(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY).burnTicks() == 0) {
                int burnTicks = consumeFuel(stack, Simulation.ACT);
                stack.set(MIComponents.STEAM_DRILL_FUEL, new SteamDrillFuel(burnTicks, burnTicks));

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
        if (stack.getOrDefault(MIComponents.WATER, 0) != FULL_WATER) {
            stack.set(MIComponents.WATER, FULL_WATER);
            player.playNotifySound(SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 1, 1);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected) {
        var fuel = stack.getOrDefault(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY);
        if (fuel.burnTicks() > 0) {
            stack.set(MIComponents.STEAM_DRILL_FUEL, new SteamDrillFuel(Math.max(0, fuel.burnTicks() - 5), fuel.maxBurnTicks()));
            stack.update(MIComponents.WATER, 0, w -> Math.max(0, w - 5));
        }
        if (fuel.burnTicks() == 0) {
            stack.set(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY);
        }
        if (stack.getOrDefault(MIComponents.WATER.get(), 0) == 0) {
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
        if (stack.getOrDefault(MIComponents.WATER, 0) == 0) {
            return false;
        }
        return stack.getOrDefault(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY).burnTicks() > 0 || consumeFuel(stack, Simulation.SIMULATE) > 0;
    }

    private int consumeFuel(ItemStack stack, Simulation simulation) {
        int burnTicks = getResource(stack).toStack().getBurnTime(null);
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
    public int getEnchantmentLevel(ItemStack stack, Holder<Enchantment> enchantment) {
        return getAllEnchantments(stack, enchantment.unwrapLookup()).getLevel(enchantment);
    }

    @Override
    public ItemEnchantments getAllEnchantments(ItemStack stack, HolderLookup.RegistryLookup<Enchantment> lookup) {
        var map = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        if (!isNotSilkTouch(stack)) {
            lookup.get(Enchantments.SILK_TOUCH).ifPresent(h -> map.set(h, 1));
        }
        return map.toImmutable();
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return !isNotSilkTouch(pStack);
    }

    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var fuel = stack.getOrDefault(MIComponents.STEAM_DRILL_FUEL, SteamDrillFuel.EMPTY);
        return Optional.of(new SteamDrillTooltipData(
                stack.getOrDefault(MIComponents.WATER, 0) * 100 / FULL_WATER,
                fuel.burnTicks(),
                Math.max(1, fuel.maxBurnTicks()),
                getResource(stack),
                getAmount(stack)));
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
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
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
        // 3x3 state
        tooltip.add(MIText.MiningArea
                .text((this.isActivated(stack) ? MIText.MiningArea3x3 : MIText.MiningArea1x1).text().setStyle(TextHelper.NUMBER_TEXT))
                .setStyle(TextHelper.GRAY_TEXT.withItalic(false)));
        // Silk touch
        tooltip.add(MIText.SilkTouchState
                .text((isNotSilkTouch(stack) ? MIText.Deactivated.text().setStyle(TextHelper.RED)
                        : MIText.Activated.text().setStyle(TextHelper.GREEN)))
                .setStyle(TextHelper.GRAY_TEXT.withItalic(false)));
    }

    @Override
    public StorageBehaviour<ItemVariant> getBehaviour() {
        return DRILL_BEHAVIOUR;
    }

    public record SteamDrillTooltipData(int waterLevel, int burnTicks, int maxBurnTicks, ItemVariant variant, long amount)
            implements TooltipComponent {}
}
