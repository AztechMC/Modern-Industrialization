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

import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.MITooltips;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GunpowderOverclockComponent implements IOverclockComponent {

    private int overclockGunpowderTick;

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putInt("overclockGunpowderTick", overclockGunpowderTick);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        overclockGunpowderTick = tag.getInt("overclockGunpowderTick");
    }

    public int getTicks() { return overclockGunpowderTick; }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        if (stackInHand.getItem() == Items.GUNPOWDER && stackInHand.getCount() >= 1) {
            if (!player.isCreative()) {
                stackInHand.shrink(1);
            }
            overclockGunpowderTick += 120 * 20;
            be.setChanged();
            if (!be.getLevel().isClientSide()) {
                be.sync();
            }
            return InteractionResult.sidedSuccess(be.getLevel().isClientSide);

        }
        return InteractionResult.PASS;
    }

    public long getRecipeEu(int eu) {
        if (overclockGunpowderTick > 0) {
            return eu * 2L;
        } else {
            return eu;
        }
    }

    public void tick(MachineBlockEntity be) {
        overclockGunpowderTick--;
        if (overclockGunpowderTick < 0) {
            overclockGunpowderTick = 0;
        } else if (overclockGunpowderTick > 0) {
            if (be.getLevel().isClientSide()) {
                for (int iter = 0; iter < 3; iter++) {
                    var random = be.getLevel().getRandom();
                    double d = be.getBlockPos().getX() + 0.5D;
                    double e = be.getBlockPos().getY();
                    double f = be.getBlockPos().getZ() + 0.5D;
                    double i = random.nextDouble() * 0.6D - 0.3D;
                    double k = random.nextDouble() * 0.6D - 0.3D;
                    be.getLevel().addParticle(ParticleTypes.SMOKE, d + i, e + 1.05, f + k, 0.15 * (random.nextDouble() - 0.5), 0.15D,
                            0.15 * (random.nextDouble() - 0.5));
                }
            }
        }
    }

    public List<Component> getTooltips() {
        return List.of(new MITooltips.Line(MIText.GunpowderUpgradeMachine).build());
    }
}
