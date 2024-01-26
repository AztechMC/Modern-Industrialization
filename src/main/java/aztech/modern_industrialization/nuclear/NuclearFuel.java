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
package aztech.modern_industrialization.nuclear;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.machines.components.NuclearEfficiencyHistoryComponent;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class NuclearFuel extends NuclearAbsorbable {

    public final double directEnergyFactor;
    public final double neutronMultiplicationFactor;

    public final String depletedVersionId;

    public final int size;

    public final int directEUbyDesintegration;
    public final int totalEUbyDesintegration;

    public final int tempLimitLow;
    public final int tempLimitHigh;

    public final static record NuclearFuelParams(int desintegrationMax, int maxTemperature, int tempLimitLow, int tempLimitHigh,
            double neutronMultiplicationFactor, double directEnergyFactor, int size) {
    }

    public NuclearFuel(Properties settings, NuclearFuelParams params, INeutronBehaviour neutronBehaviour, String depletedVersionId) {

        this(settings, params.desintegrationMax, params.maxTemperature, params.tempLimitLow, params.tempLimitHigh, params.neutronMultiplicationFactor,
                params.directEnergyFactor, neutronBehaviour, params.size, depletedVersionId);

    }

    private static int clampTemp(int temperature) {
        return 25 * (int) (temperature / 25d);
    }

    private NuclearFuel(Properties settings, int desintegrationMax, int maxTemperature, int tempLimitLow, int tempLimitHigh,
            double neutronMultiplicationFactor, double directEnergyFactor, INeutronBehaviour neutronBehaviour, int size, String depletedVersionId) {

        super(settings, clampTemp(maxTemperature), 0.8 * NuclearConstant.BASE_HEAT_CONDUCTION, neutronBehaviour, desintegrationMax);

        this.size = size;
        this.directEnergyFactor = directEnergyFactor;
        this.neutronMultiplicationFactor = neutronMultiplicationFactor;
        this.depletedVersionId = depletedVersionId;

        this.tempLimitLow = clampTemp(tempLimitLow);
        this.tempLimitHigh = clampTemp(tempLimitHigh);

        this.directEUbyDesintegration = (int) (NuclearConstant.EU_FOR_FAST_NEUTRON * directEnergyFactor * neutronMultiplicationFactor);
        this.totalEUbyDesintegration = (int) (NuclearConstant.EU_FOR_FAST_NEUTRON * (1.0 + directEnergyFactor) * neutronMultiplicationFactor);

    }

    public static ItemDefinition<NuclearFuel> of(String englishName, String id, NuclearFuelParams params, INeutronBehaviour neutronBehaviour,
            String depletedVersionId) {
        return MIItem
                .item(englishName, id, (settings) -> new NuclearFuel(settings.stacksTo(1), params, neutronBehaviour, depletedVersionId),
                        SortOrder.ITEMS_OTHER);
    }

    @Override
    public ItemVariant getNeutronProduct() {
        return ItemVariant.of(BuiltInRegistries.ITEM.getOptional(new MIIdentifier(depletedVersionId)).get());
    }

    @Override
    public long getNeutronProductAmount() {
        return size;
    }

    public double efficiencyFactor(double temperature) {
        double factor = 1;
        if (temperature > tempLimitLow) {
            factor = Math.max(0, 1 - (temperature - tempLimitLow) / (tempLimitHigh - tempLimitLow));
        }
        return factor;
    }

    public int simulateDesintegration(double neutronsReceived, ItemStack stack, double temperature, RandomSource rand,
            NuclearEfficiencyHistoryComponent efficiencyHistory) {
        int absorption = simulateAbsorption(neutronsReceived, stack, rand);
        double fuelEuConsumed = absorption * totalEUbyDesintegration;
        efficiencyHistory.registerEuFuelConsumption(fuelEuConsumed);
        return randIntFromDouble(efficiencyFactor(temperature) * absorption * neutronMultiplicationFactor, rand);
    }

}
