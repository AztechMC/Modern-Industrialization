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

import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

class ViewerCategoryRei<D> implements DisplayCategory<ViewerCategoryRei.ViewerDisplay<D>> {
    public final ViewerCategory<D> wrapped;
    public final CategoryIdentifier<ViewerDisplay<D>> identifier;
    private final Renderer icon;

    public ViewerCategoryRei(ViewerCategory<D> wrapped) {
        this.wrapped = wrapped;
        this.identifier = CategoryIdentifier.of(wrapped.id);

        this.icon = wrapped.icon instanceof ViewerCategory.Icon.Stack stack ? EntryStacks.of(stack.stack())
                : (guiGraphics, bounds, mouseX, mouseY, delta) -> {
                    var texture = (ViewerCategory.Icon.Texture) wrapped.icon;
                    guiGraphics.blit(texture.loc(), bounds.x - 1, bounds.y - 1, texture.u(), texture.v(), 18, 18, 256, 256);
                };
    }

    @Override
    public CategoryIdentifier<? extends ViewerDisplay<D>> getCategoryIdentifier() {
        return identifier;
    }

    @Override
    public Component getTitle() {
        return wrapped.title;
    }

    @Override
    public int getDisplayWidth(ViewerDisplay display) {
        return wrapped.width;
    }

    @Override
    public int getDisplayHeight() {
        return wrapped.height;
    }

    @Override
    public Renderer getIcon() {
        return icon;
    }

    public void registerRecipes(DisplayRegistry registry) {
        wrapped.buildRecipes(registry.getRecipeManager(), Minecraft.getInstance().level.registryAccess(), r -> registry.add(makeDisplay(r)));
    }

    private void processLayout(D recipe, List<IngredientBuilder> inputs, List<IngredientBuilder> outputs) {
        wrapped.buildLayout(recipe, new ViewerCategory.LayoutBuilder() {
            @Override
            public ViewerCategory.SlotBuilder inputSlot(int x, int y) {
                var ing = new IngredientBuilder(x, y, true);
                inputs.add(ing);
                return ing;
            }

            @Override
            public ViewerCategory.SlotBuilder outputSlot(int x, int y) {
                var ing = new IngredientBuilder(x, y, false);
                outputs.add(ing);
                return ing;
            }

            @Override
            public void invisibleOutput(ItemStack item) {
                var ing = new IngredientBuilder(0, 0, false);
                ing.item(item);
                ing.isVisible = false;
                outputs.add(ing);
            }
        });
    }

    private ViewerDisplay<D> makeDisplay(D recipe) {
        List<IngredientBuilder> inputs = new ArrayList<>();
        List<IngredientBuilder> outputs = new ArrayList<>();

        processLayout(recipe, inputs, outputs);

        return new ViewerDisplay<>(identifier, convert(inputs), convert(outputs), recipe);
    }

    private static class IngredientBuilder implements ViewerCategory.SlotBuilder {
        private final int x, y;
        private final boolean input;
        private final List<EntryStack<?>> ing;
        private boolean isFluid = false;
        private boolean hasBackground = true;
        private boolean isVisible = true;

        IngredientBuilder(int x, int y, boolean input) {
            this.x = x;
            this.y = y;
            this.input = input;
            this.ing = new ArrayList<>();
        }

        @Override
        public ViewerCategory.SlotBuilder variant(TransferVariant<?> variant) {
            if (variant instanceof ItemVariant item) {
                item(item.toStack());
            } else if (variant instanceof FluidVariant fluid) {
                isFluid = true;
                hasBackground = false;
                if (!fluid.isBlank()) {
                    ing.add(ReiSlotUtil.createFluidNoAmount(fluid));
                }
            } else {
                throw new IllegalArgumentException("Unknown variant type: " + variant.getClass());
            }
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder fluid(FluidVariant fluid, long amount, float probability) {
            isFluid = true;
            hasBackground = false;
            ing.add(ReiSlotUtil.createFluidEntryStack(fluid, amount, probability, input));
            return this;
        }

        private ViewerCategory.SlotBuilder items(List<ItemStack> stacks, float probability) {
            for (var stack : stacks) {
                ing.add(EntryStacks.of(stack).tooltip(ReiSlotUtil.getProbabilitySetting(probability, input)));
            }
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder item(ItemStack stack, float probability) {
            return items(List.of(stack), probability);
        }

        @Override
        public ViewerCategory.SlotBuilder ingredient(Ingredient ingredient, long amount, float probability) {
            return items(Stream.of(ingredient.getItems()).map(i -> {
                var cp = i.copy();
                cp.setCount((int) amount);
                return cp;
            }).toList(), probability);
        }

        @Override
        public ViewerCategory.SlotBuilder removeBackground() {
            hasBackground = false;
            return this;
        }

        @Override
        public ViewerCategory.SlotBuilder markCatalyst() {
            return this;
        }
    }

    private static List<EntryIngredient> convert(List<IngredientBuilder> ings) {
        List<EntryIngredient> ingredients = new ArrayList<>();
        for (var ing : ings) {
            ingredients.add(EntryIngredient.of(ing.ing));
        }
        return ingredients;
    }

    public static class ViewerDisplay<D> implements Display {
        private final CategoryIdentifier<ViewerDisplay<D>> identifier;
        private final List<EntryIngredient> inputs;
        private final List<EntryIngredient> outputs;
        final D recipe;

        public ViewerDisplay(CategoryIdentifier<ViewerDisplay<D>> identifier, List<EntryIngredient> inputs, List<EntryIngredient> outputs, D recipe) {
            this.identifier = identifier;
            this.inputs = inputs;
            this.outputs = outputs;
            this.recipe = recipe;
        }

        @Override
        public List<EntryIngredient> getInputEntries() {
            return inputs;
        }

        @Override
        public List<EntryIngredient> getOutputEntries() {
            return outputs;
        }

        @Override
        public CategoryIdentifier<?> getCategoryIdentifier() {
            return identifier;
        }

        @Override
        public Optional<ResourceLocation> getDisplayLocation() {
            return recipe instanceof RecipeHolder<?>r ? Optional.of(r.id()) : Optional.empty();
        }
    }

    @Override
    public List<Widget> setupDisplay(ViewerDisplay<D> display, Rectangle bounds) {
        List<Widget> widgets = new ArrayList<>();

        // Recipe base
        widgets.add(Widgets.createRecipeBase(bounds));

        // Extra widgets
        wrapped.buildWidgets(display.recipe, new ViewerCategory.WidgetList() {
            @Override
            public void text(Component text, float x, float y, ViewerCategory.TextAlign align, boolean shadow, boolean overrideColor,
                    @Nullable Component tooltip) {
                var label = Widgets.createLabel(new Point(bounds.x + x, bounds.y + y), text);
                switch (align) {
                case LEFT -> label.leftAligned();
                case CENTER -> label.centered();
                case RIGHT -> label.rightAligned();
                }
                label.shadow(shadow);
                if (overrideColor) {
                    label.color(0xFF404040, 0xFFBBBBBB);
                }
                if (tooltip != null) {
                    label.tooltip(tooltip);
                }
                widgets.add(label);
            }

            @Override
            public void arrow(int x, int y) {
                widgets.add(Widgets.createArrow(new Point(bounds.x + x, bounds.y + y)));
            }

            @Override
            public void texture(ResourceLocation loc, int x, int y, int u, int v, int width, int height) {
                widgets.add(Widgets.createTexturedWidget(loc, bounds.x + x, bounds.y + y, u, v, width, height));
            }

            @Override
            public void drawable(Consumer<GuiGraphics> widget) {
                widgets.add(Widgets.createDrawableWidget((guiGraphics, mouseX, mouseY, delta) -> {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(bounds.x, bounds.y, 0);
                    widget.accept(guiGraphics);
                    guiGraphics.pose().popPose();
                }));
            }

            @Override
            public void item(double x, double y, double w, double h, ItemLike item) {
                widgets.add(Widgets.createSlot(new Rectangle(bounds.x + x, bounds.y + y, w, h))
                        .entry(EntryStacks.of(item))
                        .disableTooltips()
                        .disableHighlight()
                        .disableBackground());
            }

            @Override
            public void tooltip(int x, int y, int w, int h, List<Component> tooltip) {
                widgets.add(Widgets.createDrawableWidget((matrices, reiMouseX, reiMouseY, delta) -> {
                    int mouseX = reiMouseX - bounds.x;
                    int mouseY = reiMouseY - bounds.y;
                    if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                        Tooltip.create(tooltip).queue();
                    }
                }));
            }
        });

        // Inputs and outputs
        {
            List<IngredientBuilder> inputs = new ArrayList<>();
            List<IngredientBuilder> outputs = new ArrayList<>();
            processLayout(display.recipe, inputs, outputs);

            for (var input : inputs) {
                processIngredient(widgets, input, true, bounds);
            }

            for (var output : outputs) {
                processIngredient(widgets, output, false, bounds);
            }
        }

        return widgets;
    }

    private static Widget createFluidSlotBackground(Point point) {
        return Widgets.createDrawableWidget((guiGraphics, mouseX, mouseY, delta) -> {
            guiGraphics.blit(MachineScreen.SLOT_ATLAS, point.x - 1, point.y - 1, 18, 0, 18, 18);
        });
    }

    private static void processIngredient(List<Widget> widgets, IngredientBuilder ing, boolean isInput, Rectangle bounds) {
        if (!ing.isVisible) {
            return;
        }

        var point = new Point(bounds.x + ing.x, bounds.y + ing.y);
        var slot = Widgets.createSlot(point).entries(ing.ing);
        if (isInput) {
            slot.markInput();
        } else {
            slot.markOutput();
        }
        if (!ing.hasBackground) {
            slot.disableBackground();
        }
        if (ing.isFluid) {
            widgets.add(createFluidSlotBackground(point));
        }
        widgets.add(slot);
    }
}
