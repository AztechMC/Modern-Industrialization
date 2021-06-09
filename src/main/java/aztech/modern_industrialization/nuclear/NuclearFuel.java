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
import aztech.modern_industrialization.machines.blockentities.hatches.NuclearHatch;
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class NuclearFuel extends NuclearComponent implements DurabilityBarItem {

    public final double neutronByDesintegration;
    public final int euByDesintegration;
    public final int desintegrationMax;
    public final int desintegrationByNeutron;
    public final String depleted;

    public NuclearFuel(Settings settings, int maxTemperature, int desintegrationByNeutron, double neutronByDesintegration, double neutronAbs_,
            int euByDesintegration, int desintegrationMax, String depleted) {
        super(settings, maxTemperature, 0, new INeutronBehaviour() {
            @Override
            public double getNeutronAbs() {
                return neutronAbs_;
            }

            @Override
            public double getNeutronDiff(int angle) {
                return angle == 2 ? 1.0 : 0;
            }
        });
        this.neutronByDesintegration = neutronByDesintegration;
        this.euByDesintegration = euByDesintegration;
        this.desintegrationMax = desintegrationMax;
        this.desintegrationByNeutron = desintegrationByNeutron;
        this.depleted = depleted;
    }

    public static NuclearFuel of(String id, int maxTemperature, int desintegrationByNeutron, double neutronByDesintegration, double neutronAbs,
            int euByDesintegration, int desintegrationMax, String depleted) {
        return (NuclearFuel) MIItem.of((Settings settings) -> new NuclearFuel(settings, maxTemperature, desintegrationByNeutron,
                neutronByDesintegration, neutronAbs, euByDesintegration, desintegrationMax, depleted), id, 1);
    }

    public Item getDepleted() {
        return Registry.ITEM.getOrEmpty(new MIIdentifier(depleted)).get();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        tooltip.add(new TranslatableText("text.modern_industrialization.neutrons_by_desintegration", String.format("%.2f", neutronByDesintegration))
                .setStyle(TextHelper.NEUTRONS));
        tooltip.add(new TranslatableText("text.modern_industrialization.desintegrations_by_neutron", desintegrationByNeutron)
                .setStyle(TextHelper.NEUTRONS));
        tooltip.add(new TranslatableText("text.modern_industrialization.heat_by_desintegration",
                String.format("%.2f", (double) euByDesintegration / NuclearHatch.EU_PER_DEGREE)).setStyle(TextHelper.HEAT_CONDUCTION));
        tooltip.add(new TranslatableText("text.modern_industrialization.eu_by_desintegration", euByDesintegration).setStyle(TextHelper.EU_TEXT));

        long totalEu = euByDesintegration * desintegrationMax;
        tooltip.add(new TranslatableText("text.modern_industrialization.base_eu_total_double", TextHelper.getEuString(totalEu),
                TextHelper.getEuUnit(totalEu)).setStyle(TextHelper.EU_TEXT));

        tooltip.add(new TranslatableText("text.modern_industrialization.rem_desintegration", getRemDes(stack), desintegrationMax)
                .setStyle(TextHelper.GRAY_TEXT));

    }

    public int getRemDes(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        if (tag == null || !tag.contains("desRem")) {
            return desintegrationMax;
        }
        return tag.getInt("desRem");
    }

    @Override
    public double getDurabilityBarProgress(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        if (tag == null || !tag.contains("desRem")) {
            return 0.0d;
        } else {
            return 1.0 - (double) tag.getInt("desRem") / desintegrationMax;
        }
    }

    @Override
    public boolean hasDurabilityBar(ItemStack stack) {
        NbtCompound tag = stack.getTag();
        return tag != null && tag.contains("desRem");
    }
}
