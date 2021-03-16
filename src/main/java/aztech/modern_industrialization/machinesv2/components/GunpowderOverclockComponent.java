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
package aztech.modern_industrialization.machinesv2.components;

import aztech.modern_industrialization.machinesv2.IComponent;
import aztech.modern_industrialization.machinesv2.MachineBlockEntity;
import java.util.Random;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class GunpowderOverclockComponent implements IComponent {

    public int overclockGunpowderTick;

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

    public ActionResult onUse(MachineBlockEntity be, PlayerEntity player, Hand hand) {
        ItemStack stackInHand = player.getStackInHand(hand);
        if (stackInHand.getItem() == Items.GUNPOWDER && stackInHand.getCount() >= 1) {
            if (!player.isCreative()) {
                stackInHand.decrement(1);
            }
            overclockGunpowderTick += 120 * 20;
            be.markDirty();
            if (!be.getWorld().isClient()) {
                be.sync();
            }
            return ActionResult.success(be.getWorld().isClient);

        }
        return ActionResult.PASS;
    }

    public boolean isOverclocked() {
        return overclockGunpowderTick > 0;
    }

    public void tick(MachineBlockEntity be) {
        overclockGunpowderTick--;
        if (overclockGunpowderTick < 0) {
            overclockGunpowderTick = 0;
        } else if (overclockGunpowderTick > 0) {
            if (be.getWorld().isClient()) {
                for (int iter = 0; iter < 3; iter++) {
                    Random random = be.getWorld().getRandom();
                    double d = be.getPos().getX() + 0.5D;
                    double e = be.getPos().getY();
                    double f = be.getPos().getZ() + 0.5D;
                    double i = random.nextDouble() * 0.6D - 0.3D;
                    double k = random.nextDouble() * 0.6D - 0.3D;
                    be.getWorld().addParticle(ParticleTypes.SMOKE, d + i, e + 1.05, f + k, 0.15 * (random.nextDouble() - 0.5), 0.15D,
                            0.15 * (random.nextDouble() - 0.5));
                }
            }
        }
    }
}
