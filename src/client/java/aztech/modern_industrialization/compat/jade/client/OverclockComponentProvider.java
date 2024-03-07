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
package aztech.modern_industrialization.compat.jade.client;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIText;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;

public class OverclockComponentProvider implements IBlockComponentProvider {
    @Override
    public ResourceLocation getUid() {
        return MI.id("overclock");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag tag = accessor.getServerData();
        if (tag.contains("efficiencyTicks") && tag.contains("maxEfficiencyTicks") && tag.contains("baseRecipeEu")
                && tag.contains("currentRecipeEu")) {

            int efficiencyTicks = tag.getInt("efficiencyTicks");
            int maxEfficiencyTicks = tag.getInt("maxEfficiencyTicks");
            long baseRecipeEu = tag.getLong("baseRecipeEu");
            long currentEu = tag.getLong("currentRecipeEu");

            double mult = (double) currentEu / baseRecipeEu;
            var helper = IElementHelper.get();

            tooltip.add(helper.progress(
                    MIJadeClientPlugin.ratio(efficiencyTicks, maxEfficiencyTicks),
                    MIJadeClientPlugin.textAndRatio(
                            MIText.Efficiency.text(),
                            String.valueOf(efficiencyTicks),
                            String.valueOf(maxEfficiencyTicks)),
                    helper.progressStyle().color(0xFF61C928, 0xFF438C1C).textColor(-1),
                    BoxStyle.getNestedBox(),
                    true));

            tooltip.add(helper.text(MIText.EuTOverclocked.text(
                    String.format("%.1f", mult), String.format("%d", currentEu))));
        }
    }
}
