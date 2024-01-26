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
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import net.minecraft.world.item.Item;

public class NuclearComponentItem extends Item implements INuclearComponent<ItemVariant> {

    public final int maxTemperature;
    public final double heatConduction;
    public final INeutronBehaviour neutronBehaviour;

    public NuclearComponentItem(Properties settings, int maxTemperature, double heatConduction, INeutronBehaviour neutronBehaviour) {
        super(settings);
        this.maxTemperature = maxTemperature;
        this.heatConduction = heatConduction;
        this.neutronBehaviour = neutronBehaviour;
    }

    public static ItemDefinition<NuclearComponentItem> of(String englishName, String id, int maxTemperature, double heatConduction,
            INeutronBehaviour neutronBehaviour) {
        return MIItem
                .item(englishName, id, (settings) -> new NuclearComponentItem(settings.stacksTo(1), maxTemperature, heatConduction, neutronBehaviour),
                        SortOrder.ITEMS_OTHER);
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

    @Override
    public ItemVariant getVariant() {
        return ItemVariant.of(this);
    }
}
