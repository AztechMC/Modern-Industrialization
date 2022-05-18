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

import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.util.TextHelper;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class NuclearAbsorbable extends NuclearComponentItem {

    public final int desintegrationMax;

    public NuclearAbsorbable(FabricItemSettings settings, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour,
            int desintegrationMax) {
        super(settings, maxTemperature, heatConduction, neutronBehaviour);
        this.desintegrationMax = desintegrationMax;
    }

    public void setRemainingDesintegrations(ItemStack stack, int value) {
        Preconditions.checkArgument(value >= 0 & value <= desintegrationMax,
                String.format("Remaining desintegration %d must be between 0 and max desintegration = %d", value, desintegrationMax));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("desRem", value);
    }

    public static NuclearComponentItem of(String englishName, String id, int maxTemperature, double heatConduction,
            INeutronBehaviour neutronBehaviour,
            int desintegrationMax) {
        return MIItem.item(englishName, id,
                (settings) -> new NuclearAbsorbable(settings.maxCount(1), maxTemperature, heatConduction, neutronBehaviour, desintegrationMax))
                .asItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag context) {
        super.appendHoverText(stack, world, tooltip, context);

        tooltip.add(MIText.RemAbsorption.text(getRemainingDesintegrations(stack), desintegrationMax).setStyle(TextHelper.GRAY_TEXT));

    }

    public double getDurabilityBarProgress(ItemStack stack) {
        return (double) getRemainingDesintegrations(stack) / desintegrationMax;

    }

    @Override
    public int getBarColor(ItemStack stack) {
        float f = (float) getRemainingDesintegrations(stack) / desintegrationMax;
        return Mth.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getRemainingDesintegrations(stack) != desintegrationMax;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(getDurabilityBarProgress(stack) * 13);
    }

    public int getRemainingDesintegrations(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("desRem")) {
            return desintegrationMax;
        }
        return tag.getInt("desRem");
    }

    protected static int randIntFromDouble(double value, Random rand) {
        return (int) Math.floor(value) + (rand.nextDouble() < (value % 1) ? 1 : 0);
    }

    public int simulateAbsorption(double neutronsReceived, ItemStack stack, Random rand) {
        int absorbNeutrons = Math.min(randIntFromDouble(neutronsReceived, rand), getRemainingDesintegrations(stack));

        setRemainingDesintegrations(stack, getRemainingDesintegrations(stack) - absorbNeutrons);
        return absorbNeutrons;

    }
}
