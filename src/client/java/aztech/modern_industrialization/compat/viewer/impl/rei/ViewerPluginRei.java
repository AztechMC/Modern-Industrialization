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
package aztech.modern_industrialization.compat.viewer.impl.rei;

import aztech.modern_industrialization.compat.viewer.usage.ViewerSetup;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;

@REIPluginClient
public class ViewerPluginRei implements REIClientPlugin {

    private final List<ViewerCategoryRei<?>> categories = new ArrayList<>();

    @Override
    public void registerCategories(CategoryRegistry registry) {
        categories.clear(); // needed for reloads

        for (var category : ViewerSetup.setup()) {
            categories.add(new ViewerCategoryRei<>(category));
        }

        registry.add(categories.toArray(new DisplayCategory[0]));

        for (var category : categories) {
            category.wrapped.buildWorkstations(items -> {
                for (var item : items) {
                    registry.addWorkstations(category.identifier, EntryStacks.of(item));
                }
            });
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (var category : categories) {
            category.registerRecipes(registry);
        }
    }
}
