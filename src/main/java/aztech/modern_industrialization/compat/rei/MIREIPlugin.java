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
package aztech.modern_industrialization.compat.rei;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeHelper;
import me.shedaniel.rei.api.plugins.REIPluginV0;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class MIREIPlugin implements REIPluginV0 {
    private static final Identifier PATHING = new Identifier("minecraft", "plugins/pathing");
    private static final Identifier STRIPPING = new Identifier("minecraft", "plugins/stripping");

    @Override
    public Identifier getPluginIdentifier() {
        return new MIIdentifier("plugin");
    }

    @Override
    public void registerOthers(RecipeHelper recipeHelper) {
        for (Item item : Registry.ITEM) {
            if (item instanceof DieselToolItem) {
                if (FabricToolTags.AXES.contains(item)) {
                    recipeHelper.registerWorkingStations(STRIPPING, EntryStack.create(item));
                }
                if (FabricToolTags.SHOVELS.contains(item)) {
                    recipeHelper.registerWorkingStations(PATHING, EntryStack.create(item));
                }
            }
        }
    }
}
