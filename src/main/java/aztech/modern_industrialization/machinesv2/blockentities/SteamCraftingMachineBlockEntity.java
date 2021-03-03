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
package aztech.modern_industrialization.machinesv2.blockentities;

import aztech.modern_industrialization.machines.impl.MachineTier;
import aztech.modern_industrialization.machines.recipe.MachineRecipeType;
import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.components.MachineInventoryComponent;
import aztech.modern_industrialization.machinesv2.components.sync.ProgressBar;
import aztech.modern_industrialization.machinesv2.gui.MachineGuiParameters;
import aztech.modern_industrialization.machinesv2.helper.SteamHelper;
import aztech.modern_industrialization.machinesv2.models.MachineCasings;
import aztech.modern_industrialization.machinesv2.models.MachineModelClientData;
import aztech.modern_industrialization.util.Simulation;
import java.util.Random;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;

public class SteamCraftingMachineBlockEntity extends AbstractCraftingMachineBlockEntity {

    private int overclockGunpowderTick = 0;

    public SteamCraftingMachineBlockEntity(BlockEntityType<?> type, MachineRecipeType recipeType, MachineInventoryComponent inventory,
            MachineGuiParameters guiParams, ProgressBar.Parameters progressBarParams, MachineTier tier) {
        super(type, recipeType, inventory, guiParams, progressBarParams, tier);
        this.registerComponents(new IComponent() {

            @Override
            public void writeNbt(CompoundTag tag) {
                tag.putInt("overclockGunpowderTick", overclockGunpowderTick);
            }

            @Override
            public void readNbt(CompoundTag tag) {
                overclockGunpowderTick = tag.getInt("overclockGunpowderTick");
            }

            @Override
            public boolean isClientSynced() {
                return true;
            }

            @Override
            public boolean forceRemesh() {
                return false;
            }
        });
    }

    @Override
    public long consumeEu(long max, Simulation simulation) {
        return SteamHelper.consumeSteamEu(getInventory().fluidStacks, max, simulation);
    }

    @Override
    protected MachineModelClientData getModelData() {
        MachineModelClientData data = new MachineModelClientData();
        orientation.writeModelData(data);
        data.isActive = isActiveComponent.isActive;
        return data;
    }

    @Override
    protected ActionResult onUse(PlayerEntity player, Hand hand, Direction face) {
        ActionResult result = super.onUse(player, hand, face);
        if (!result.isAccepted()) {
            ItemStack stackInHand = player.getStackInHand(hand);
            if (stackInHand.getItem() == Items.GUNPOWDER && stackInHand.getCount() >= 1) {
                if (!player.isCreative()) {
                    stackInHand.decrement(1);
                }
                overclockGunpowderTick += 120 * 20;
                markDirty();
                if (world.isClient()) {
                    sync();
                }
                return ActionResult.success(getWorld().isClient);
            }
        }
        return ActionResult.PASS;
    }

    @Override
    public long getMaxRecipeEu() {
        if (overclockGunpowderTick == 0) {
            return tier.getMaxEu();
        } else {
            return tier.getMaxEu() * 2;
        }

    }

    @Override
    public long getBaseRecipeEu() {
        if (overclockGunpowderTick == 0) {
            return tier.getBaseEu();
        } else {
            return tier.getBaseEu() * 2;
        }

    }

    public void tick() {
        super.tick();
        overclockGunpowderTick--;
        if (overclockGunpowderTick < 0) {
            overclockGunpowderTick = 0;
        } else if (overclockGunpowderTick > 0) {
            if (getWorld().isClient()) {
                for (int iter = 0; iter < 3; iter++) {
                    Random random = getWorld().getRandom();
                    double d = pos.getX() + 0.5D;
                    double e = pos.getY();
                    double f = pos.getZ() + 0.5D;
                    double i = random.nextDouble() * 0.6D - 0.3D;
                    double k = random.nextDouble() * 0.6D - 0.3D;
                    world.addParticle(ParticleTypes.SMOKE, d + i, e + 1.05, f + k, 0.15 * (random.nextDouble() - 0.5), 0.15D,
                            0.15 * (random.nextDouble() - 0.5));
                }
            }
        }

    }

}
