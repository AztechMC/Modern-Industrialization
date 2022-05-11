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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class CasingComponent implements IComponent {

    private CableTier tierCasing;
    private final CableTier defaultCasing;
    private static final BiMap<Block, CableTier> blockCasing = HashBiMap.create();

    static {
        blockCasing.put(MIBlock.BASIC_MACHINE_HULL.asBlock(), CableTier.LV);
        blockCasing.put(MIBlock.ADVANCED_MACHINE_HULL.asBlock(), CableTier.MV);
        blockCasing.put(MIBlock.TURBO_MACHINE_HULL.asBlock(), CableTier.HV);
        blockCasing.put(MIBlock.HIGHLY_ADVANCED_MACHINE_HULL.asBlock(), CableTier.EV);
        blockCasing.put(MIBlock.QUANTUM_MACHINE_HULL.asBlock(), CableTier.SUPERCONDUCTOR);
    }

    public CasingComponent(CableTier defaultCasing) {
        this.defaultCasing = defaultCasing;
        this.tierCasing = defaultCasing;

    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putString("casing", tierCasing.name);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        tierCasing = CableTier.getTier(tag.getString("casing"));
        if (tierCasing == null) {
            tierCasing = defaultCasing;
        }

    }

    @Override
    public void writeClientNbt(CompoundTag tag) {
        tag.putString("casing", tierCasing.name);
    }

    @Override
    public void readClientNbt(CompoundTag tag) {
        tierCasing = CableTier.getTier(tag.getString("casing"));
        if (tierCasing == null) {
            tierCasing = defaultCasing;
        }
    }

    public void dropCasing(Level world, BlockPos pos) {
        ItemStack stack = new ItemStack(blockCasing.inverse().get(tierCasing).asItem(), 1);
        Containers.dropItemStack(world, pos.getX(), pos.getY(), pos.getZ(), stack);

    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.getItem() == MIItem.ITEM_CROWBAR.asItem() && !player.isShiftKeyDown()) {
            if (tierCasing != defaultCasing) {
                dropCasing(be.getLevel(), be.getBlockPos());
                tierCasing = defaultCasing;
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();
                }
            }
            return InteractionResult.sidedSuccess(be.getLevel().isClientSide);

        } else {
            if (stackInHand.getItem() instanceof BlockItem && stackInHand.getCount() >= 1) {
                BlockItem blockItem = (BlockItem) stackInHand.getItem();
                if (blockCasing.containsKey(blockItem.getBlock())) {
                    CableTier newTier = blockCasing.get(blockItem.getBlock());
                    if (newTier != defaultCasing && newTier != tierCasing) {
                        if (defaultCasing != tierCasing) {
                            dropCasing(be.getLevel(), be.getBlockPos());
                        }
                        tierCasing = newTier;
                        if (!player.isCreative()) {
                            stackInHand.shrink(1);
                        }
                        be.setChanged();
                        if (!be.getLevel().isClientSide()) {
                            be.sync();
                        }
                        return InteractionResult.sidedSuccess(be.getLevel().isClientSide);
                    }

                }
            }

        }
        return InteractionResult.PASS;
    }

    public ItemStack getDrop() {
        if (tierCasing != defaultCasing) {
            return new ItemStack(blockCasing.inverse().get(tierCasing).asItem(), 1);
        }
        return ItemStack.EMPTY;
    }

    public MachineCasing getCasing() {
        return MachineCasings.casingFromCableTier(tierCasing);
    }

    public boolean canInsertEu(CableTier tier) {
        return tier == tierCasing;
    }

    public long getEuCapacity() {
        return tierCasing.getEu() * 100;
    }
}
