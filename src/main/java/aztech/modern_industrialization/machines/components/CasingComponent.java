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
package aztech.modern_industrialization.machines.components;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public class CasingComponent implements IComponent, DropableComponent {

    private ItemStack casingStack = ItemStack.EMPTY;
    private CableTier currentTier = CableTier.LV;

    /**
     * Sets the current casing stack and update {@link #currentTier} accordingly.
     */
    private void setCasingStack(ItemStack stack) {
        casingStack = stack;

        // Compute tier
        currentTier = getCasingTier(stack.getItem());
        if (currentTier == null) {
            currentTier = CableTier.LV;
        }
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.put("casing", casingStack.save(new CompoundTag()));
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        // legacy saving by CableTier name
        // TODO 1.21: remove
        if (tag.contains("casing", Tag.TAG_STRING)) {
            var tier = CableTier.getTier(tag.getString("casing"));
            setCasingStack(BuiltInRegistries.ITEM.get(tier.itemKey).getDefaultInstance());
        } else {
            setCasingStack(ItemStack.of(tag.getCompound("casing")));
        }
    }

    @Override
    public void writeClientNbt(CompoundTag tag) {
        tag.putString("casing", currentTier.name);
    }

    @Override
    public void readClientNbt(CompoundTag tag) {
        currentTier = CableTier.getTier(tag.getString("casing"));
    }

    public void dropCasing(Level world, BlockPos pos) {
        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), casingStack);
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.getCount() >= 1) {
            var newTier = getCasingTier(stackInHand.getItem());
            if (newTier != null && newTier != currentTier) {
                if (currentTier != CableTier.LV) {
                    dropCasing(be.getLevel(), be.getBlockPos());
                }
                setCasingStack(stackInHand.copyWithCount(1));
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();
                }
                be.getLevel().updateNeighborsAt(be.getBlockPos(), Blocks.AIR);
                // Play a nice sound :)
                playCasingPlaceSound(be);
                return InteractionResult.sidedSuccess(be.getLevel().isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    private void playCasingPlaceSound(MachineBlockEntity be) {
        var blockKey = currentTier.itemKey;
        if (blockKey == null) {
            return; // no sound for LV
        }

        // Look for corresponding block
        BuiltInRegistries.BLOCK.getOptional(blockKey).ifPresent(block -> {
            var casingState = block.defaultBlockState();
            var group = casingState.getSoundType();
            var sound = group.getBreakSound();
            be.getLevel().playSound(null, be.getBlockPos(), sound, SoundSource.BLOCKS, (group.getVolume() + 1.0F) / 4.0F, group.getPitch() * 0.8F);
        });

    }

    @Nullable
    public static CableTier getCasingTier(Item item) {
        var itemKey = BuiltInRegistries.ITEM.getKey(item);
        for (var tier : CableTier.allTiers()) {
            if (tier.itemKey != null && tier.itemKey.equals(itemKey)) {
                return tier;
            }
        }
        return null;
    }

    @Override
    public ItemStack getDrop() {
        return casingStack;
    }

    public void setCasingServer(MachineBlockEntity be, ItemStack casing) {
        setCasingStack(casing);
        be.setChanged();
        be.sync();
        be.getLevel().updateNeighborsAt(be.getBlockPos(), Blocks.AIR);
        playCasingPlaceSound(be);
    }

    public MachineCasing getCasing() {
        return currentTier.casing;
    }

    public boolean canInsertEu(CableTier tier) {
        return tier == currentTier;
    }

    public long getEuCapacity() {
        return currentTier.getEu() * 100;
    }
}
