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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

public class NuclearComponentItem extends Item implements INuclearComponent {

    public final int maxTemperature;
    public final double heatConduction;
    public final INeutronBehaviour neutronBehaviour;

    public NuclearComponentItem(Settings settings, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour) {
        super(settings);
        this.maxTemperature = maxTemperature;
        this.heatConduction = heatConduction;
        this.neutronBehaviour = neutronBehaviour;
    }

    public static NuclearComponentItem of(String id, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour) {
        return (NuclearComponentItem) MIItem
                .of((Settings settings) -> new NuclearComponentItem(settings, maxTemperature, heatConduction, neutronBehaviour), id, 1);
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {

        if (Screen.hasShiftDown()) {
            tooltip.add(new LiteralText(""));
            tooltip.add(new TranslatableText("text.modern_industrialization.max_temp", maxTemperature).setStyle(TextHelper.MAX_TEMP_TEXT));
            tooltip.add(new TranslatableText("text.modern_industrialization.heat_conduction", String.format("%.2f", heatConduction))
                    .setStyle(TextHelper.HEAT_CONDUCTION));
            tooltip.add(new LiteralText(""));
        } else {
            tooltip.add(new TranslatableText("text.modern_industrialization.maj_to_tooltip").setStyle(TextHelper.YELLOW));
        }

    }

    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public double getHeatConduction() {
        return heatConduction;
    }

    @Override
    public INeutronBehaviour getNeutronBehaviour() {
        return neutronBehaviour;
    }
}
