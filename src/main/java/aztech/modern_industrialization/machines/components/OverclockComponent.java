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
import java.util.*;
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

    private List<Catalyst> catalysts;

    private NavigableMap<Double, MutableTickCount> TickMap = new TreeMap<>();

    public OverclockComponent(List<Catalyst> catalysts) {
        this.catalysts = catalysts;
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        for (var entry : TickMap.entrySet()) {
            var multiplierKey = String.format("overclock%.2f", entry.getKey().doubleValue());
            tag.putInt(multiplierKey, entry.getValue().value);
        }
    }

    @Override
    public void readNbt(CompoundTag tag) {
        for (Catalyst catalyst : catalysts) {
            var multiplierKey = String.format("overclock%.2f", catalyst.multiplier.doubleValue());
            if (tag.contains(multiplierKey) && !TickMap.containsKey(catalyst.multiplier)) {
                TickMap.put(catalyst.multiplier, new MutableTickCount(tag.getInt(multiplierKey)));
            }
        }

        // TODO 1.20: Remove upgrade code
        if (tag.contains("overclockGunpowderTick")) {
            TickMap.put(Double.valueOf(2D), new MutableTickCount(tag.getInt("overclockGunpowderTick")));
        }
    }

    public int getTicks() {
        var lastEntry = TickMap.lastEntry();
        if (lastEntry != null) {
            return lastEntry.getValue().value;
        }
        return 0;
    }

    public InteractionResult onUse(MachineBlockEntity be, Player player, InteractionHand hand) {
        ItemStack stackInHand = player.getItemInHand(hand);
        var resourceInHand = Registry.ITEM.getKey(stackInHand.getItem());

        for (Catalyst catalyst : catalysts) {
            if (resourceInHand.equals(catalyst.resourceLocation) && stackInHand.getCount() >= 1) {
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }

                if (TickMap.containsKey(catalyst.multiplier)) {
                    var overclockTicks = TickMap.get(catalyst.multiplier);
                    overclockTicks.value += catalyst.ticks;
                } else {
                    TickMap.put(catalyst.multiplier, new MutableTickCount(catalyst.ticks));
                }

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
        var lastEntry = TickMap.lastEntry();
        if (lastEntry != null) {
            var overclockTicks = lastEntry.getValue();
            overclockTicks.value--;
            if (overclockTicks.value <= 0) {
                TickMap.remove(lastEntry.getKey());
            } else {
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

            var multiplierText = Component.literal("" + catalyst.multiplier).setStyle(MITooltips.NUMBER_TEXT);
            var tickText = Component.literal("" + catalyst.ticks).setStyle(MITooltips.NUMBER_TEXT);
            tooltips.add(MIText.OverclockMachine.text(catalystName, multiplierText, tickText));
        }
        return tooltips;
    }

    public long getRecipeEu(int eu) {
        var lastEntry = TickMap.lastEntry();
        if (lastEntry != null && lastEntry.getValue().value > 0) {
            return Math.round(lastEntry.getKey().doubleValue() * eu);
        } else {
            return eu;
        }
    }

    public static OverclockComponent createDefaultGunpowderOverclock() {
        return new OverclockComponent(
                List.of(new OverclockComponent.Catalyst(2D, new ResourceLocation("minecraft:gunpowder"), 120 * 20)));
    }

    public static class Catalyst {
        public final ResourceLocation resourceLocation;
        public final int ticks;
        public final Double multiplier;

        public Catalyst(double multiplier, ResourceLocation location, int ticks) {
            this.multiplier = Double.valueOf(multiplier);
            this.resourceLocation = location;
            this.ticks = ticks;
        }
    }

    private static class MutableTickCount {
        public int value;

        public MutableTickCount(int value) {
            this.value = value;
        }
    }

}
