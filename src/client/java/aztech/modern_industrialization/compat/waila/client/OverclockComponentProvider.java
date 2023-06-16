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
package aztech.modern_industrialization.compat.waila.client;

import aztech.modern_industrialization.MIText;
import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IBlockComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.component.BarComponent;
import mcp.mobius.waila.api.component.PairComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.nbt.CompoundTag;

public class OverclockComponentProvider implements IBlockComponentProvider {

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        CompoundTag tag = accessor.getData().raw();
        if (tag.contains("efficiencyTicks") && tag.contains("maxEfficiencyTicks") && tag.contains("baseRecipeEu")
                && tag.contains("currentRecipeEu")) {

            int efficiencyTicks = tag.getInt("efficiencyTicks");
            int maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
            long baseRecipeEu = tag.getLong("baseRecipeEu");
            long currentEu = tag.getLong("currentRecipeEu");

            double mult = (double) currentEu / baseRecipeEu;

            tooltip.addLine(new PairComponent(
                    new WrappedComponent(MIText.Efficiency.text()),
                    new BarComponent(MIWailaClientPlugin.ratio(efficiencyTicks, maxEfficiencyTicks), 0xFF61C928,
                            MIWailaClientPlugin.fraction(efficiencyTicks, maxEfficiencyTicks))));

            tooltip.addLine(new WrappedComponent(MIText.EuTOverclocked.text(
                    String.format("%.1f", mult), String.format("%d", currentEu))));
        }
    }
}
