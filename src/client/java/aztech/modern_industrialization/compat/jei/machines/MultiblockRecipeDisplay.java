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
package aztech.modern_industrialization.compat.jei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockRecipeDisplay {
    public final String controller;
    public final Shape shape;

    public MultiblockRecipeDisplay(String controller, ShapeTemplate shapeTemplate) {
        this.controller = controller;
        this.shape = new Shape(controller, shapeTemplate);
    }

    public static class Shape {
        public final ItemStack controller;
        public final List<ItemStack> materials = new ArrayList<>();

        public Shape(String controller, ShapeTemplate shape) {
            this.controller = Registry.ITEM.get(new MIIdentifier(controller)).getDefaultInstance();
            this.materials.add(this.controller);
            SortedMap<Item, Integer> materials = new TreeMap<>(Comparator.comparing(Registry.ITEM::getKey));

            for (var entry : shape.simpleMembers.entrySet()) {
                BlockState state = entry.getValue().getPreviewState();
                Item item = state.getBlock().asItem();
                if (item != Items.AIR) {
                    materials.put(item, 1 + materials.getOrDefault(item, 0));
                }
            }

            for (var entry : materials.entrySet()) {
                this.materials.add(new ItemStack(entry.getKey(), entry.getValue()));
            }
        }
    }
}
