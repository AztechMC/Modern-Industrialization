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
package aztech.modern_industrialization.compat.viewer.usage;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.state.BlockState;

public class MultiblockCategory extends ViewerCategory<MultiblockCategory.Recipe> {
    private static final int SLOTS = 6;
    private static final int MARGIN = 10;
    private static final int H = 18 + 2 * MARGIN;
    private static final int W = SLOTS * 20 - 2 + 2 * MARGIN;

    protected MultiblockCategory() {
        super(Recipe.class, new MIIdentifier("multiblock_shapes"), MIText.MultiblockMaterials.text(), MIItem.WRENCH.stack(), W, H);
    }

    @Override
    public void buildWorkstations(WorkstationConsumer consumer) {
    }

    @Override
    public void buildRecipes(RecipeManager recipeManager, Consumer<Recipe> consumer) {
        for (Tuple<String, ShapeTemplate> entry : ReiMachineRecipes.multiblockShapes) {
            consumer.accept(new Recipe(entry.getA(), entry.getB()));
        }
    }

    @Override
    public void buildLayout(Recipe recipe, LayoutBuilder builder) {
        for (int i = 0; i < SLOTS; ++i) {
            var slot = builder.inputSlot(MARGIN + i * 20, MARGIN);
            if (i < recipe.materials.size()) {
                slot.items(recipe.materials.get(i));
            }
        }

        builder.invisibleOutput(recipe.materials.get(0));
    }

    @Override
    public void buildWidgets(Recipe recipe, WidgetList widgets) {
    }

    protected static class Recipe {
        public final ItemStack controller;
        public final List<ItemStack> materials = new ArrayList<>();

        public Recipe(String controller, ShapeTemplate shapeTemplate) {
            this.controller = Registry.ITEM.get(new MIIdentifier(controller)).getDefaultInstance();
            this.materials.add(this.controller);
            SortedMap<Item, Integer> materials = new TreeMap<>(Comparator.comparing(Registry.ITEM::getKey));

            for (var entry : shapeTemplate.simpleMembers.entrySet()) {
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
