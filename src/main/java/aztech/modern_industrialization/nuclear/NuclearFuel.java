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
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class NuclearFuel extends NuclearComponentItem {

    public final double directEnergyFactor;
    public final double neutronMultiplicationFactor;
    public final int desintegrationMax;
    public final String depletedVersionId;

    public final int size;

    public final int directEUbyDesintegration;
    public final int totalEUbyDesintegration;

    public final static record NuclearFuelParams(int desintegrationMax, int maxTemperature, double neutronMultiplicationFactor,
            double directEnergyFactor, int size) {
    }

    public NuclearFuel(Settings settings, NuclearFuelParams params, INeutronBehaviour neutronBehaviour, String depletedVersionId) {

        this(settings, params.desintegrationMax, params.maxTemperature, params.neutronMultiplicationFactor, params.directEnergyFactor,
                neutronBehaviour, params.size, depletedVersionId);

    }

    private NuclearFuel(Settings settings, int desintegrationMax, int maxTemperature, double neutronMultiplicationFactor, double directEnergyFactor,
            INeutronBehaviour neutronBehaviour, int size, String depletedVersionId) {

        super(settings, maxTemperature, 0, neutronBehaviour);

        this.desintegrationMax = desintegrationMax;
        this.size = size;
        this.directEnergyFactor = directEnergyFactor;
        this.neutronMultiplicationFactor = neutronMultiplicationFactor;
        this.depletedVersionId = depletedVersionId;

        this.directEUbyDesintegration = (int) (NuclearConstant.EU_FOR_FAST_NEUTRON * directEnergyFactor * neutronMultiplicationFactor);
        this.totalEUbyDesintegration = (int) (NuclearConstant.EU_FOR_FAST_NEUTRON * (1.0 + directEnergyFactor) * neutronMultiplicationFactor);

    }

    public static NuclearFuel of(String id, NuclearFuelParams params, INeutronBehaviour neutronBehaviour, String depletedVersionId) {

        return (NuclearFuel) MIItem.of((Settings settings) -> new NuclearFuel(settings, params, neutronBehaviour, depletedVersionId), id, 1);
    }

    public Item getDepleted() {
        return Registry.ITEM.getOrEmpty(new MIIdentifier(depletedVersionId)).get();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

        super.appendTooltip(stack, world, tooltip, context);

        if (Screen.hasShiftDown()) {

            tooltip.add(new TranslatableText("text.modern_industrialization.direct_heat_by_desintegration",
                    String.format("%.2f", (double) directEUbyDesintegration / NuclearConstant.EU_PER_DEGREE)).setStyle(TextHelper.HEAT_CONDUCTION));

            tooltip.add(new TranslatableText("text.modern_industrialization.total_heat_by_desintegration",
                    String.format("%.2f", (double) totalEUbyDesintegration / NuclearConstant.EU_PER_DEGREE)).setStyle(TextHelper.HEAT_CONDUCTION));

            long totalEu = (long) totalEUbyDesintegration * desintegrationMax;
            tooltip.add(new LiteralText(""));
            tooltip.add(new TranslatableText("text.modern_industrialization.base_eu_total_double", TextHelper.getEuString(totalEu),
                    TextHelper.getEuUnit(totalEu)).setStyle(TextHelper.EU_TEXT));

            tooltip.add(
                    new TranslatableText("text.modern_industrialization.rem_desintegration", getRemainingDesintegrations(stack), desintegrationMax));
        }

    }

    public int getRemainingDesintegrations(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        if (tag == null || !tag.contains("desRem")) {
            return desintegrationMax;
        }
        return tag.getInt("desRem");
    }

    public void setRemainingDesintegrations(ItemStack stack, int value) {
        Preconditions.checkArgument(value >= 0 & value <= desintegrationMax,
                String.format("Remaining desintegration %d must be between 0 and max desintegration = %d", value, desintegrationMax));
        NbtCompound tag = stack.getOrCreateTag();
        tag.putInt("desRem", value);
    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) getRemainingDesintegrations(stack) / desintegrationMax;

    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = (float) getRemainingDesintegrations(stack) / desintegrationMax;
        return MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return getRemainingDesintegrations(stack) != desintegrationMax;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    public int simulateDesintegration(double neutronsReceived, ItemStack stack, Random rand) {
        int desintegration = Math.min(randIntFromDouble(neutronsReceived, rand), getRemainingDesintegrations(stack));

        setRemainingDesintegrations(stack, getRemainingDesintegrations(stack) - desintegration);
        return randIntFromDouble(desintegration * neutronMultiplicationFactor, rand);

    }

    private static int randIntFromDouble(double value, Random rand) {
        return (int) Math.floor(value) + (rand.nextDouble() < (value % 1) ? 1 : 0);
    }

}
