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
import aztech.modern_industrialization.items.ContainerItem;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.util.MobSpawning;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BarrelBlock extends AbstractStorageBlock<ItemVariant> implements EntityBlock {

    public BarrelBlock(EntityBlock factory, StorageBehaviour<ItemVariant> behaviour) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).isValidSpawn(MobSpawning.NO_SPAWN), factory, behaviour);
    }

    public static void setupBarrelEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getBlockEntity(hitResult.getBlockPos()) instanceof BarrelBlockEntity barrel
                    && hitResult.getDirection().getAxis().isHorizontal()) {

                if (barrel.behaviour.isCreative()) {
                    ItemVariant currentInHand = ItemVariant.of(player.getMainHandItem());
                    if (!currentInHand.isBlank() && barrel.isResourceBlank()) {
                        barrel.setResource(currentInHand);
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }

                // Build a special storage that ignores the lock for manual player insertion.
                InsertionOnlyStorage<ItemVariant> barrelInserter = (res, max, tx) -> barrel.insert(res, max, tx, true);

                if (!player.isShiftKeyDown()) {

                    ItemStack stack = player.getItemInHand(hand);

                    if (stack.getItem() instanceof BarrelItem barrelItem) {
                        var storage = ContainerItem.GenericItemStorage.of(stack, barrelItem);
                        if (StorageUtil.move(storage, barrelInserter, (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }
                    }
                    if (StorageUtil.move(PlayerInventoryStorage.of(player).getSlots().get(player.getInventory().selected), barrelInserter,
                            (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                } else {
                    ItemVariant currentInHand = ItemVariant.of(player.getMainHandItem());
                    if (StorageUtil.move(PlayerInventoryStorage.of(player), barrelInserter, (itemVariant) -> itemVariant.equals(currentInHand),
                            Long.MAX_VALUE, null) > 0) {
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                }
            }
            return InteractionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.getBlockEntity(pos) instanceof BarrelBlockEntity barrel && direction.getAxis().isHorizontal()) {
                if (!barrel.isEmpty()) {

                    ItemStack stack = player.getItemInHand(hand);

                    if (stack.getItem() instanceof BarrelItem barrelItem) {
                        var storage = ContainerItem.GenericItemStorage.of(stack, barrelItem);
                        if (StorageUtil.move(barrel, storage, (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
                            return InteractionResult.sidedSuccess(world.isClientSide);
                        }
                    }

                    try (Transaction transaction = Transaction.openOuter()) {
                        ItemVariant extractedResource = barrel.getResource();

                        long extracted = barrel.extract(barrel.getResource(),
                                player.isShiftKeyDown() ? 1 : barrel.getResource().getItem().getMaxStackSize(),
                                transaction);

                        PlayerInventoryStorage.of(player).offerOrDrop(extractedResource, extracted, transaction);

                        transaction.commit();
                        CommonProxy.INSTANCE.delayNextBlockAttack(player);
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            return InteractionResult.PASS;
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
