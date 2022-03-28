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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.blocks.storage.AbstractStorageBlock;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BarrelBlock extends AbstractStorageBlock implements EntityBlock {

    public final EntityBlock factory;

    public BarrelBlock(String id, Function<MIBlock, BlockItem> blockItemCtor, EntityBlock factory) {
        super(id, FabricBlockSettings.of(METAL_MATERIAL).destroyTime(4.0f).requiresCorrectToolForDrops()
                .isValidSpawn(MobSpawning.NO_SPAWN), blockItemCtor);
        setPickaxeMineable();

        this.asColumn();
        this.factory = factory;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.newBlockEntity(pos, state);
    }

    static {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.getBlockEntity(hitResult.getBlockPos()) instanceof BarrelBlockEntity barrel
                    && hitResult.getDirection().getAxis().isHorizontal()) {
                if (!player.isShiftKeyDown()) {
                    if (StorageUtil.move(PlayerInventoryStorage.of(player).getSlots().get(player.getInventory().selected), barrel,
                            (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
                        return InteractionResult.sidedSuccess(world.isClientSide);
                    }
                } else {
                    ItemVariant currentInHand = ItemVariant.of(player.getMainHandItem());
                    if (StorageUtil.move(PlayerInventoryStorage.of(player), barrel, (itemVariant) -> itemVariant.equals(currentInHand),
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
                    try (Transaction transaction = Transaction.openOuter()) {
                        ItemVariant extractedResource = barrel.getResource();

                        long extracted = barrel.extract(barrel.getResource(),
                                player.isShiftKeyDown() ? 1 : barrel.getResource().getItem().getMaxStackSize(),
                                transaction);

                        PlayerInventoryStorage.of(player).offerOrDrop(extractedResource, extracted, transaction);

                        transaction.commit();
                        if (world.isClientSide()) {
                            updateDestroyDelay();
                        }
                    }
                    return InteractionResult.sidedSuccess(world.isClientSide);
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static void updateDestroyDelay() {
        // Add a 5 tick delay like vanilla.
        Minecraft.getInstance().gameMode.destroyDelay = 5;
    }
}
