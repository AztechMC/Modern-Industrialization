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
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CasingComponent implements IComponent {

    private CableTier tierCasing;
    private final CableTier defaultCasing;
    private static final BiMap<Block, CableTier> blockCasing = HashBiMap.create();

    static {
        blockCasing.put(MIBlock.BASIC_MACHINE_HULL, CableTier.LV);
        blockCasing.put(MIBlock.ADVANCED_MACHINE_HULL, CableTier.MV);
        blockCasing.put(MIBlock.TURBO_MACHINE_HULL, CableTier.HV);
        blockCasing.put(MIBlock.HIGHLY_ADVANCED_MACHINE_HULL, CableTier.EV);
        blockCasing.put(MIBlock.QUANTUM_MACHINE_HULL, CableTier.SUPRACONDUCTOR);
    }

    public CasingComponent(CableTier defaultCasing) {
        this.defaultCasing = defaultCasing;
        this.tierCasing = defaultCasing;

    }

    @Override
    public void writeNbt(NbtCompound tag) {
        tag.putString("casing", tierCasing.name);
    }

    @Override
    public void readNbt(NbtCompound tag) {
        tierCasing = CableTier.getTier(tag.getString("casing"));
        if (tierCasing == null) {
            tierCasing = defaultCasing;
        }

    }

    @Override
    public void writeClientNbt(NbtCompound tag) {
        tag.putString("casing", tierCasing.name);
    }

    @Override
    public void readClientNbt(NbtCompound tag) {
        tierCasing = CableTier.getTier(tag.getString("casing"));
        if (tierCasing == null) {
            tierCasing = defaultCasing;
        }
    }

    public void dropCasing(World world, BlockPos pos) {
        ItemStack stack = new ItemStack(blockCasing.inverse().get(tierCasing).asItem(), 1);
        ItemScatterer.spawn(world, pos.getX(), pos.getY(), pos.getZ(), stack);

    }

    public ActionResult onUse(MachineBlockEntity be, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() == MIItem.ITEM_CROWBAR && !player.isSneaking()) {
            if (tierCasing != defaultCasing) {
                dropCasing(be.getWorld(), be.getPos());
                tierCasing = defaultCasing;
                be.markDirty();
                if (!be.getWorld().isClient()) {
                    be.sync();
                }
            }
            return ActionResult.success(be.getWorld().isClient);

        } else {
            if (stackInHand.getItem() instanceof BlockItem && stackInHand.getCount() >= 1) {
                BlockItem blockItem = (BlockItem) stackInHand.getItem();
                if (blockCasing.containsKey(blockItem.getBlock())) {
                    CableTier newTier = blockCasing.get(blockItem.getBlock());
                    if (newTier != defaultCasing && newTier != tierCasing) {
                        if (defaultCasing != tierCasing) {
                            dropCasing(be.getWorld(), be.getPos());
                        }
                        tierCasing = newTier;
                        if (!player.isCreative()) {
                            stackInHand.decrement(1);
                        }
                        be.markDirty();
                        if (!be.getWorld().isClient()) {
                            be.sync();
                        }
                        return ActionResult.success(be.getWorld().isClient);
                    }

                }
            }

        }
        return ActionResult.PASS;
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
