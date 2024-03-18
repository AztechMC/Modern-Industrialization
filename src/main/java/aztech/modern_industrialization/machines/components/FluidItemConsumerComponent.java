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
import aztech.modern_industrialization.api.datamaps.FluidFuel;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.IComponent;
import aztech.modern_industrialization.util.ItemStackHelper;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.CommonHooks;

/**
 * A component that turns fluids and/or item into energy.
 */
public class FluidItemConsumerComponent implements IComponent.ServerOnly {

    protected long euBuffer = 0;
    /**
     * The maximum EU that can be produced by one production operation, to limit the
     * maximum conversion rate of the machine.
     */
    public final long maxEuProduction;

    public final EUProductionMap<Item> itemEUProductionMap;
    public final EUProductionMap<Fluid> fluidEUProductionMap;

    public FluidItemConsumerComponent(long maxEuProduction,
            EUProductionMap<Item> itemEUProductionMap,
            EUProductionMap<Fluid> fluidEUProductionMap) {

        this.itemEUProductionMap = itemEUProductionMap;
        this.fluidEUProductionMap = fluidEUProductionMap;
        this.maxEuProduction = maxEuProduction;

    }

    public boolean doAllowMoreThanOne() {
        return itemEUProductionMap.getNumberOfFuel() == NumberOfFuel.MANY
                || fluidEUProductionMap.getNumberOfFuel() == NumberOfFuel.MANY;
    }

    public static FluidItemConsumerComponent ofSingleFluid(long maxEuProduction,
            FluidDefinition acceptedFluid,
            long fluidEUperMb) {
        return ofFluid(maxEuProduction, new EuProductionMapBuilder<>(BuiltInRegistries.FLUID).add(acceptedFluid.getId(), fluidEUperMb).build());
    }

    public static FluidItemConsumerComponent ofFluidFuels(long maxEuProduction) {
        return new FluidItemConsumerComponent(maxEuProduction,
                EUProductionMap.empty(),
                fluidFuels());
    }

    public static FluidItemConsumerComponent ofFluid(long maxEuProduction, EUProductionMap<Fluid> fluidEUProductionMap) {
        return new FluidItemConsumerComponent(maxEuProduction,
                EUProductionMap.empty(),
                fluidEUProductionMap);
    }

    @Override
    public void writeNbt(CompoundTag tag) {
        tag.putLong("euBuffer", euBuffer);
    }

    @Override
    public void readNbt(CompoundTag tag, boolean isUpgradingMachine) {
        euBuffer = tag.getLong("euBuffer");
    }

    public long getEuProduction(List<ConfigurableFluidStack> fluidInputs,
            List<ConfigurableItemStack> itemInputs,
            long maxEnergyInsertable) {

        long maxEuProduced = Math.min(maxEnergyInsertable, maxEuProduction);

        if (maxEuProduced == 0) {
            return 0;
        }

        if (euBuffer >= maxEuProduced) {
            euBuffer -= maxEuProduced;
            return maxEuProduced;
        }

        long euProduced = 0;

        for (ConfigurableFluidStack stack : fluidInputs) {
            Fluid fluid = stack.getResource().getFluid();
            if (fluidEUProductionMap.accept(fluid) && stack.getAmount() > 0) {
                long fuelEu = fluidEUProductionMap.getEuProduction(fluid);
                long usedDroplets = Math.min((maxEuProduced - euProduced + fuelEu - 1) / fuelEu, stack.getAmount());
                euProduced += usedDroplets * fuelEu;
                stack.decrement(usedDroplets);

                if (euProduced >= maxEuProduced) {
                    euBuffer += euProduced - maxEuProduced;
                    return maxEuProduced;
                }
            }
        }

        for (ConfigurableItemStack stack : itemInputs) {
            Item fuel = stack.getResource().getItem();
            if (itemEUProductionMap.accept(fuel) && stack.getAmount() > 0) {
                if (!itemEUProductionMap.isStandardFuels() || ItemStackHelper.consumeFuel(stack, true)) {
                    long fuelEU = itemEUProductionMap.getEuProduction(fuel);
                    long usedItem = Math.min((maxEuProduced - euProduced + fuelEU - 1) / fuelEU, stack.getAmount());
                    euProduced += fuelEU * usedItem;

                    if (itemEUProductionMap.isStandardFuels()) {
                        ItemStackHelper.consumeFuel(stack, false);
                    } else {
                        stack.decrement(usedItem);
                    }

                    if (euProduced >= maxEuProduced) {
                        euBuffer += euProduced - maxEuProduced;
                        return maxEuProduced;
                    }
                }
            }
        }

        return euProduced;
    }

    public List<Component> getTooltips() {

        List<Component> returnList = new ArrayList<>();

        returnList.add(new MITooltips.Line(MIText.MaxEuProduction).arg(
                this.maxEuProduction,
                MITooltips.EU_PER_TICK_PARSER).build());

        if (this.fluidEUProductionMap.getNumberOfFuel() != NumberOfFuel.NONE) {
            if (this.fluidEUProductionMap.isStandardFuels()) {
                returnList.add(new MITooltips.Line(MIText.AcceptAnyFluidFuels).build());
            } else {
                var informationEntries = this.fluidEUProductionMap.getAllAcceptedWithEU();

                if (informationEntries.size() == 1) {
                    var entry = informationEntries.iterator().next();
                    returnList.add(new MITooltips.Line(MIText.AcceptSingleFluid)
                            .arg(entry.variant).arg(entry.eu, MITooltips.EU_PARSER).build());
                } else if (informationEntries.size() > 1) {
                    returnList.add(new MITooltips.Line(MIText.ConsumesTheFollowing).build());
                    for (var entry : informationEntries) {
                        returnList.add(
                                new MITooltips.Line(MIText.AcceptFollowingFluidEntry)
                                        .arg(entry.variant).arg(entry.eu, MITooltips.EU_PARSER).build());
                    }
                }

            }
        }

        if (this.itemEUProductionMap.getNumberOfFuel() != NumberOfFuel.NONE) {
            if (this.itemEUProductionMap.isStandardFuels()) {
                returnList.add(new MITooltips.Line(MIText.AcceptAnyItemFuels).build());
            } else {
                var informationEntries = this.itemEUProductionMap.getAllAcceptedWithEU();
                if (informationEntries.size() == 1) {
                    var entry = informationEntries.iterator().next();
                    returnList.add(new MITooltips.Line(MIText.AcceptSingleItem)
                            .arg(entry.variant).arg(entry.eu, MITooltips.EU_PARSER).build());
                } else {
                    returnList.add(new MITooltips.Line(MIText.ConsumesTheFollowing).build());
                    for (var entry : informationEntries) {
                        returnList.add(
                                new MITooltips.Line(MIText.AcceptFollowingItemEntry)
                                        .arg(entry.variant).arg(entry.eu, MITooltips.EU_PARSER).build());
                    }
                }

            }
        }

        return returnList;
    }

    public interface EUProductionMap<T> {

        record InformationEntry<T> (long eu, T variant) {
        }

        long getEuProduction(T variant);

        default boolean isStandardFuels() {
            return false;
        }

        default boolean accept(T variant) {
            return getEuProduction(variant) != 0;
        }

        default NumberOfFuel getNumberOfFuel() {
            if (isStandardFuels()) {
                return NumberOfFuel.MANY;
            } else {
                if (getAllAccepted().size() == 1) {
                    return NumberOfFuel.SINGLE;
                } else if (getAllAccepted().size() == 0) {
                    return NumberOfFuel.NONE;
                } else {
                    return NumberOfFuel.MANY;
                }
            }
        }

        // create an empty EUProductionMap
        static <T> EUProductionMap<T> empty() {
            return new EUProductionMap<>() {
                @Override
                public long getEuProduction(T variant) {
                    return 0;
                }

                @Override
                public List<T> getAllAccepted() {
                    return List.of();
                }
            };
        }

        List<T> getAllAccepted();

        default List<InformationEntry<T>> getAllAcceptedWithEU() {
            return getAllAccepted().stream()
                    .map(variant -> new InformationEntry<>(getEuProduction(variant), variant)).sorted(Comparator.comparingLong(InformationEntry::eu))
                    .collect(Collectors.toList());
        }

    }

    public static class EuProductionMapBuilder<T> {

        private final Map<ResourceLocation, Long> map = new HashMap<>(); // Must Stores as string, because KubeJS could add not loader yet resource
                                                                         // location
        private final DefaultedRegistry<T> registryAccess;

        public EuProductionMapBuilder(DefaultedRegistry<T> registryAccess) {
            this.registryAccess = registryAccess;
        }

        public EuProductionMapBuilder<T> add(ResourceLocation resourceLocation,
                long eu) {
            map.put(resourceLocation, eu);
            return this;
        }

        public EUProductionMap<T> build() {
            return new EUProductionMap<>() {
                @Override
                public long getEuProduction(T variant) {
                    return map.getOrDefault(registryAccess.getKey(variant), 0L);
                }

                @Override
                public List<T> getAllAccepted() {
                    return map.keySet().stream().map(registryAccess::get).collect(Collectors.toList());
                }
            };
        }
    }

    public static EUProductionMap<Item> itemFuels() {

        return new EUProductionMap<>() {
            @Override
            public long getEuProduction(Item variant) {
                // TODO NEO NBT-aware fuels
                int burnTime = CommonHooks.getBurnTime(variant.getDefaultInstance(), null);
                return burnTime <= 0 ? 0 : burnTime * FuelBurningComponent.EU_PER_BURN_TICK;
            }

            @Override
            public boolean isStandardFuels() {
                return true;
            }

            @Override
            public List<Item> getAllAccepted() {
                throw new UnsupportedOperationException("The list of accepted items is not available for standard fuels");
            }

        };
    }

    public static EUProductionMap<Fluid> fluidFuels() {

        return new EUProductionMap<>() {
            @Override
            public long getEuProduction(Fluid variant) {
                return FluidFuel.getEu(variant);
            }

            @Override
            public boolean isStandardFuels() {
                return true;
            }

            @Override
            public List<Fluid> getAllAccepted() {
                throw new UnsupportedOperationException("The list of accepted fluids is not available for fluid fuels");
            }
        };

    }

    public enum NumberOfFuel {
        NONE(),
        SINGLE(),
        MANY(),
    }
}
