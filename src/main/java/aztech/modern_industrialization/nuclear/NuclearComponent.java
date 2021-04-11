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
import aztech.modern_industrialization.util.TextHelper;
import java.util.List;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class NuclearComponent extends Item {

    public final int maxTemperature;
    public final double heatConduction;
    public final INeutronBehaviour neutronBehaviour;

    public NuclearComponent(Settings settings, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour) {
        super(settings);
        this.maxTemperature = maxTemperature;
        this.heatConduction = heatConduction;
        this.neutronBehaviour = neutronBehaviour;
    }

    public static NuclearComponent of(String id, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour) {
        return (NuclearComponent) MIItem.of((Settings settings) -> new NuclearComponent(settings, maxTemperature, heatConduction, neutronBehaviour),
                id, 1);
    }

    public static NuclearComponent of(String id) {
        return of(id, 2500, 0, new INeutronBehaviour() {

            @Override
            public double getNeutronAbs() {
                return 1;
            }

            @Override
            public double getNeutronDiff(int angle) {
                return 0;
            }
        });
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("text.modern_industrialization.max_temp", maxTemperature).setStyle(TextHelper.MAX_TEMP_TEXT));
        tooltip.add(new TranslatableText("text.modern_industrialization.heat_conduction", heatConduction).setStyle(TextHelper.HEAT_CONDUCTION));
        tooltip.add(new TranslatableText("text.modern_industrialization.neutron_abs", Math.round(100 * neutronBehaviour.getNeutronAbs()))
                .setStyle(TextHelper.NEUTRONS));
    }
}
