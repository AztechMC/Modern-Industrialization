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
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.util.MobSpawning;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BarrelBlock extends MIBlock implements BlockEntityProvider {

    public final BlockEntityProvider factory;

    public BarrelBlock(String id, Function<MIBlock, BlockItem> blockItemCtor, BlockEntityProvider factory) {
        super(id, FabricBlockSettings.of(METAL_MATERIAL).hardness(4.0f).breakByTool(FabricToolTags.PICKAXES).requiresTool()
                .allowsSpawning(MobSpawning.NO_SPAWN), blockItemCtor);

        this.asColumn();
        this.factory = factory;
        ResourceUtil.appendToItemTag(new MIIdentifier("barrels"), new MIIdentifier(id));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return factory.createBlockEntity(pos, state);
    }

    private ItemStack getStack(BlockEntity entity) {
        BarrelBlockEntity blockEntity = (BarrelBlockEntity) entity;
        ItemStack stack = new ItemStack(asItem());
        if (!blockEntity.isEmpty()) {
            NbtCompound tag = new NbtCompound();
            tag.put("BlockEntityTag", blockEntity.toClientTag(new NbtCompound()));
            stack.setTag(tag);
        }
        return stack;
    }

    @Override
    public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
        LootContext lootContext = builder.parameter(LootContextParameters.BLOCK_STATE, state).build(LootContextTypes.BLOCK);
        return Collections.singletonList(getStack(lootContext.get(LootContextParameters.BLOCK_ENTITY)));
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        return getStack(world.getBlockEntity(pos));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.isSneaking() && ModernIndustrialization.WRENCHES.contains(player.getMainHandStack().getItem())) {
            world.spawnEntity(new ItemEntity(world, hit.getPos().x, hit.getPos().y, hit.getPos().z, getStack(world.getBlockEntity(pos))));
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return ActionResult.success(world.isClient);
        }
        return ActionResult.PASS;
    }

    static {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
            if (blockEntity instanceof BarrelBlockEntity barrel) {
                if (!player.isSneaking()) {
                    if (StorageUtil.move(PlayerInventoryStorage.of(player).getSlots().get(player.getInventory().selectedSlot), barrel,
                            (itemVariant) -> true, Long.MAX_VALUE, null) > 0) {
                        return ActionResult.success(world.isClient);
                    }

                } else {
                    ItemVariant currentInHand = ItemVariant.of(player.getMainHandStack());
                    if (StorageUtil.move(PlayerInventoryStorage.of(player), barrel, (itemVariant) -> itemVariant.equals(currentInHand),
                            Long.MAX_VALUE, null) > 0) {
                        return ActionResult.success(world.isClient);
                    }

                }

            }
            return ActionResult.PASS;
        });

        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BarrelBlockEntity barrel) {
                if (!barrel.isEmpty()) {
                    try (Transaction transaction = Transaction.openOuter()) {
                        ItemVariant extractedResource = barrel.getResource();

                        long extracted = barrel.extract(barrel.getResource(), player.isSneaking() ? 1 : barrel.getResource().getItem().getMaxCount(),
                                transaction);

                        PlayerInventoryStorage.of(player).offerOrDrop(extractedResource, extracted, transaction);

                        transaction.commit();
                    }

                }
            }
            return ActionResult.PASS;
        });

    }
}
