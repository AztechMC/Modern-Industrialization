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
package aztech.modern_industrialization.compat.rei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import java.util.*;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.RecipeDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

public class MultiblockRecipeDisplay implements RecipeDisplay {
    public final String controller;
    public final Shape shape;

    public MultiblockRecipeDisplay(String controller, ShapeTemplate shapeTemplate) {
        this.controller = controller;
        this.shape = new Shape(controller, shapeTemplate);
    }

    @Override
    public @NotNull List<List<EntryStack>> getInputEntries() {
        return shape.materials;
    }

    @Override
    public @NotNull List<List<EntryStack>> getResultingEntries() {
        return Collections.singletonList(shape.materials.get(0));
    }

    @Override
    public @NotNull Identifier getRecipeCategory() {
        return MultiblockRecipeCategory.ID;
    }

    public static class Shape {
        public final List<EntryStack> controller;
        public final List<List<EntryStack>> materials = new ArrayList<>();

        public Shape(String controller, ShapeTemplate shape) {
            this.controller = Collections.singletonList(EntryStack.create(Registry.ITEM.get(new MIIdentifier(controller))));
            this.materials.add(this.controller);
            SortedMap<Item, Integer> materials = new TreeMap<>(Comparator.comparing(Registry.ITEM::getId));

            for (Map.Entry<BlockPos, SimpleMember> entry : shape.simpleMembers.entrySet()) {
                BlockState state = entry.getValue().getPreviewState();
                Item item = state.getBlock().asItem();
                if (item != Items.AIR) {
                    materials.put(item, 1 + materials.getOrDefault(item, 0));
                }
            }

            for (Map.Entry<Item, Integer> entry : materials.entrySet()) {
                ItemStack stack = new ItemStack(entry.getKey(), entry.getValue());
                this.materials.add(Collections.singletonList(EntryStack.create(stack)));
            }
        }
    }
}
