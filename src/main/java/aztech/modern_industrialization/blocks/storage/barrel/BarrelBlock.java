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
package aztech.modern_industrialization.blocks.storage.barrel;

import aztech.modern_industrialization.blocks.storage.AbstractStorageBlock;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.transaction.Transaction;
import aztech.modern_industrialization.util.MobSpawning;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class BarrelBlock extends AbstractStorageBlock<ItemVariant> implements EntityBlock {

    public BarrelBlock(EntityBlock factory, StorageBehaviour<ItemVariant> behaviour) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).isValidSpawn(MobSpawning.NO_SPAWN), factory, behaviour);
    }

    private static boolean useBlock(BlockHitResult hitResult, InteractionHand hand, Player player, Level world) {
        if (world.getBlockEntity(hitResult.getBlockPos()) instanceof BarrelBlockEntity barrel
                && hitResult.getDirection().getAxis().isHorizontal()) {

            if (barrel.behaviour.isCreative()) {
                ItemVariant currentInHand = ItemVariant.of(player.getMainHandItem());
                if (!currentInHand.isBlank() && barrel.isResourceBlank()) {
                    barrel.setResource(currentInHand);
                    return true;
                }
            }

            if (!player.isShiftKeyDown()) {
                // TODO NEO implement item-item storage API support
//                if (stack.getItem() instanceof BarrelItem barrelItem) {
//                    var storage = ContainerItem.GenericItemStorage.of(stack, barrelItem);
//                    if (StorageUtil.move(storage, barrelInserter, (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
//                        return true;
//                    }
//                }
                var handItem = player.getItemInHand(hand);
                if (!handItem.isEmpty()) {
                    try (var tx = Transaction.openOuter()) {
                        long inserted = barrel.insert(ItemVariant.of(handItem), handItem.getCount(), tx, true);
                        if (inserted > 0) {
                            tx.commit();
                            handItem.shrink((int) inserted);
                            return true;
                        }
                    }
                }
            } else {
                ItemVariant currentInHand = ItemVariant.of(player.getMainHandItem());
                if (!currentInHand.isBlank()) {
                    try (var tx = Transaction.openOuter()) {
                        long inserted = 0;
                        for (int i = 0; i < Inventory.INVENTORY_SIZE; ++i) {
                            ItemStack stack = player.getInventory().getItem(i);
                            if (!currentInHand.matches(stack)) {
                                continue;
                            }

                            long thisIter = barrel.insert(currentInHand, stack.getCount(), tx, true);
                            inserted += thisIter;
                            stack.shrink((int) thisIter);
                        }
                        if (inserted > 0) {
                            tx.commit();
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean attackBlock(BlockPos pos, Direction direction, InteractionHand hand, Player player, Level world) {
        if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel && direction.getAxis().isHorizontal()) {
            if (!barrel.isEmpty()) {
                ItemStack stack = player.getItemInHand(hand);

                // TODO NEO implement item-item storage API support
//                if (stack.getItem() instanceof BarrelItem barrelItem) {
//                    var storage = ContainerItem.GenericItemStorage.of(stack, barrelItem);
//                    if (StorageUtil.move(barrel, storage, (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
//                        return InteractionResult.sidedSuccess(world.isClientSide);
//                    }
//                }

                try (Transaction transaction = Transaction.openOuter()) {
                    ItemVariant extractedResource = barrel.getResource();

                    long extracted = barrel.extract(extractedResource,
                            player.isShiftKeyDown() ? 1 : extractedResource.toStack().getMaxStackSize(),
                            transaction);
                    transaction.commit();

                    player.getInventory().placeItemBackInInventory(extractedResource.toStack((int) extracted));
                }
                return true;
            }
        }
        return false;
    }

    public static void setupBarrelEvents() {
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, event -> {
            if (event.getUseBlock() == Event.Result.DENY) {
                return;
            }

            if (useBlock(event.getHitVec(), event.getHand(), event.getEntity(), event.getLevel())) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.sidedSuccess(event.getSide().isClient()));
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.LeftClickBlock.class, event -> {
            if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) {
                return;
            }

            if (attackBlock(event.getPos(), event.getFace(), event.getHand(), event.getEntity(), event.getLevel())) {
                event.setCanceled(true);
            }
        });
    }

    public static BarrelStorage withStackCapacity(long stackCapacity) {
        return new BarrelStorage(stackCapacity);
    }

    public static class BarrelStorage extends StorageBehaviour<ItemVariant> {

        public final long stackCapacity;

        public BarrelStorage(long stackCapacity) {
            this.stackCapacity = stackCapacity;
        }

        @Override
        public boolean isLockable() {
            return true;
        }

        @Override
        public long getCapacityForResource(ItemVariant resource) {
            if (resource.isBlank()) {
                return stackCapacity * 64;
            } else {
                return stackCapacity * resource.getItem().getMaxStackSize();
            }
        }

        @Override
        public boolean canInsert(ItemVariant resource) {
            return resource.getItem().canFitInsideContainerItems();
        }
    }

}
