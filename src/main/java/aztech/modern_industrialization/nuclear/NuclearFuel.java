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
import java.util.List;
import me.shedaniel.cloth.api.durability.bar.DurabilityBarItem;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class NuclearFuel extends NuclearComponent implements DurabilityBarItem {

    private final double neutronAmplification;
    private final double neutronAbs;
    public final double heatByDesintegration;
    public final int desintegrationMax;
    public final String depleted;

    public NuclearFuel(Settings settings, int maxTemperature, double neutronAmplification, double neutronAbs_, double heatByDesintegration,
            int desintegrationMax, String depleted) {
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
        this.neutronAmplification = neutronAmplification;
        this.neutronAbs = neutronAbs_;
        this.heatByDesintegration = heatByDesintegration;
        this.desintegrationMax = desintegrationMax;
        this.depleted = depleted;
    }

    public static NuclearFuel of(String id, int maxTemperature, double neutronAmplification, double neutronAbs, double heatByDesintegration,
            int desintegrationMax, String depleted) {
        return (NuclearFuel) MIItem.of((Settings settings) -> new NuclearFuel(settings, maxTemperature, neutronAmplification, neutronAbs,
                heatByDesintegration, desintegrationMax, depleted), id, 1);
    }

    public Item getDepleted() {
        return Registry.ITEM.getOrEmpty(new MIIdentifier(depleted)).get();
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public double getDurabilityBarProgress(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("remDes")) {
            return 1.0d;
        } else {
            return (double) tag.getInt("remDes") / 1.0d;
        }
    }

    @Override
    public boolean hasDurabilityBar(ItemStack stack) {
        return true;
    }
}
