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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class OverclockComponent implements IComponent {

    private int overclockTicks;
    private double overclockMultiplier;
    private List<Catalyst> catalysts;

    public OverclockComponent(double multiplier, List<Catalyst> catalysts) {
        this.overclockMultiplier = multiplier;
        this.catalysts = catalysts;
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putInt("overclockTicks", overclockTicks);
    }

    @Override
    public void readNbt(CompoundTag tag) {
        overclockTicks = tag.getInt("overclockTicks");
    }

    public int getTicks() {
        return overclockTicks;
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        var resourceInHand = Registry.ITEM.getKey(stackInHand.getItem());

        for (Catalyst catalyst : catalysts) {
            if (resourceInHand.equals(catalyst.resourceLocation) && stackInHand.getCount() >= 1) {
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }
                overclockTicks += catalyst.ticks;
                be.setChanged();
                if (!be.getLevel().isClientSide()) {
                    be.sync();
                }
                return InteractionResult.sidedSuccess(be.getLevel().isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    public void tick(MachineBlockEntity be) {
        overclockTicks--;
        if (overclockTicks < 0) {
            overclockTicks = 0;
        } else if (overclockTicks > 0) {
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
        var tooltips = new ArrayList<Component>();
        for (Catalyst catalyst : catalysts) {
            Component catalystName;
            if (Registry.BLOCK.containsKey(catalyst.resourceLocation)) {
                catalystName = Component
                        .translatable("block.%s.%s".formatted(catalyst.resourceLocation.getNamespace(), catalyst.resourceLocation.getPath()));
            } else if (Registry.ITEM.containsKey(catalyst.resourceLocation)) {
                catalystName = Component
                        .translatable("item.%s.%s".formatted(catalyst.resourceLocation.getNamespace(), catalyst.resourceLocation.getPath()));
            } else {
                throw new RuntimeException("Invalid block or item as catalyst: " + catalyst.resourceLocation);
            }

            var multiplierText = Component.literal("" + overclockMultiplier).setStyle(MITooltips.NUMBER_TEXT);
            var tickText = Component.literal("" + catalyst.ticks).setStyle(MITooltips.NUMBER_TEXT);
            tooltips.add(MIText.OverclockMachine.text(catalystName, multiplierText, tickText));
        }
        return tooltips;
    }

    public long getRecipeEu(int eu) {
        if (overclockTicks > 0) {
            return Math.round(overclockMultiplier * eu);
        } else {
            return eu;
        }
    }

    public static OverclockComponent createDefaultGunpowderOverclock() {
        return new OverclockComponent(2D,
                List.of(new OverclockComponent.Catalyst(new ResourceLocation("minecraft:gunpowder"), 120 * 20)));
    }

    public static class Catalyst {
        public final ResourceLocation resourceLocation;
        public final int ticks;

        public Catalyst(ResourceLocation location, int ticks) {
            this.resourceLocation = location;
            this.ticks = ticks;
        }
    }
}
