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
package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

class ViewerCategoryJei<D> implements IRecipeCategory<D> {
    private final IJeiHelpers helpers;
    public final ViewerCategory<D> wrapped;
    public final RecipeType<D> recipeType;
    private final IDrawable background, icon, itemSlot, fluidSlot;

    public ViewerCategoryJei(IJeiHelpers helpers, ViewerCategory<D> wrapped) {
        this.helpers = helpers;
        this.wrapped = wrapped;
        this.recipeType = RecipeType.create(wrapped.id.getNamespace(), wrapped.id.getPath(), wrapped.dataClass);

        this.background = helpers.getGuiHelper().createBlankDrawable(wrapped.width - 8, wrapped.height - 8);
        this.icon = wrapped.icon instanceof ViewerCategory.Icon.Stack stack ? helpers.getGuiHelper().createDrawableItemStack(stack.stack())
                : new IDrawable() {
                    @Override
                    public int getWidth() {
                        return 18;
                    }

                    @Override
                    public int getHeight() {
                        return 18;
                    }

                    @Override
                    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
                        var texture = (ViewerCategory.Icon.Texture) wrapped.icon;
                        guiGraphics.blit(texture.loc(), xOffset - 1, yOffset - 1, 0, texture.u(), texture.v(), 18, 18, 256, 256);
                    }
                };
        this.itemSlot = helpers.getGuiHelper().getSlotDrawable();
        this.fluidSlot = helpers.getGuiHelper().createDrawable(MachineScreen.SLOT_ATLAS, 18, 0, 18, 18);
    }

    @Override
    public RecipeType<D> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return wrapped.title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, D recipe, IFocusGroup focuses) {
        wrapped.buildLayout(recipe, new ViewerCategory.LayoutBuilder() {
            @Override
            public ViewerCategory.SlotBuilder inputSlot(int x, int y) {
                return slot(RecipeIngredientRole.INPUT, x, y);
            }

            @Override
            public ViewerCategory.SlotBuilder outputSlot(int x, int y) {
                return slot(RecipeIngredientRole.OUTPUT, x, y);
            }

            @Override
            public void invisibleOutput(ItemStack stack) {
                builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                        .addItemStack(stack);
            }

            private ViewerCategory.SlotBuilder slot(RecipeIngredientRole role, int x, int y) {
                var slotBuilder = builder.addSlot(role, x - 4, y - 4);
                slotBuilder.setBackground(itemSlot, -1, -1);
                return new ViewerCategory.SlotBuilder() {
                    @Override
                    public ViewerCategory.SlotBuilder variant(TransferVariant<?> variant) {
                        if (variant instanceof ItemVariant item) {
                            item(item.toStack());
                        } else if (variant instanceof FluidVariant fluid) {
                            if (!fluid.isBlank()) {
                                slotBuilder.addFluidStack(fluid.getFluid(), 1, fluid.copyNbt());
                            }
                            JeiSlotUtil.overrideFluidRenderer(slotBuilder);
                            slotBuilder.setBackground(fluidSlot, -1, -1);
                        } else {
                            throw new IllegalArgumentException("Unknown variant type: " + variant.getClass());
                        }
                        return this;
                    }

                    @Override
                    public ViewerCategory.SlotBuilder fluid(FluidVariant fluid, long amount, float probability) {
                        slotBuilder.addFluidStack(fluid.getFluid(), amount, fluid.copyNbt());
                        JeiSlotUtil.overrideFluidRenderer(slotBuilder);
                        JeiSlotUtil.customizeTooltip(slotBuilder, probability);
                        slotBuilder.setBackground(fluidSlot, -1, -1);
                        return this;
                    }

                    private ViewerCategory.SlotBuilder items(List<ItemStack> stacks, float probability) {
                        slotBuilder.addItemStacks(stacks);
                        JeiSlotUtil.customizeTooltip(slotBuilder, probability);
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
                        slotBuilder.setBackground(helpers.getGuiHelper().createBlankDrawable(0, 0), 0, 0);
                        return this;
                    }

                    @Override
                    public ViewerCategory.SlotBuilder markCatalyst() {
                        return this;
                    }
                };
            }
        });
    }

    @Override
    public void draw(D recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.pose().translate(-4, -4, 0);

        wrapped.buildWidgets(recipe, new ViewerCategory.WidgetList() {
            @Override
            public void text(Component text, float x, float y, ViewerCategory.TextAlign align, boolean shadow, boolean overrideColor,
                    @Nullable Component tooltip) {
                var font = Minecraft.getInstance().font;
                var width = font.width(text);
                var alignedX = switch (align) {
                case LEFT -> x;
                case CENTER -> x - width / 2f;
                case RIGHT -> x - width;
                };

                guiGraphics.drawString(font, text, (int) alignedX, (int) y, overrideColor ? 0xFF404040 : -1, shadow);
            }

            @Override
            public void arrow(int x, int y) {
                texture(new MIIdentifier("textures/gui/jei/arrow.png"), x, y, 0, 17, 24, 17);
            }

            @Override
            public void texture(ResourceLocation loc, int x, int y, int u, int v, int width, int height) {
                IDrawableStatic drawable = helpers.getGuiHelper().createDrawable(loc, u, v, width, height);
                drawable.draw(guiGraphics, x, y);
            }

            @Override
            public void drawable(Consumer<GuiGraphics> widget) {
                widget.accept(guiGraphics);
            }

            @Override
            public void item(double x, double y, double w, double h, ItemLike item) {
                guiGraphics.pose().pushPose();
                var drawable = helpers.getGuiHelper().createDrawableItemStack(item.asItem().getDefaultInstance());
                guiGraphics.pose().translate(x, y, 0);
                guiGraphics.pose().scale((float) w / 16, (float) h / 16, 0);
                drawable.draw(guiGraphics);
                guiGraphics.pose().popPose();
            }

            @Override
            public void tooltip(int x, int y, int w, int h, List<Component> tooltip) {
            }
        });
    }

    @Override
    public List<Component> getTooltipStrings(D recipe, IRecipeSlotsView recipeSlotsView, double jeiMouseX, double jeiMouseY) {
        double mouseX = jeiMouseX + 4;
        double mouseY = jeiMouseY + 4;

        var tooltips = new ArrayList<Component>();

        wrapped.buildWidgets(recipe, new ViewerCategory.WidgetList() {
            @Override
            public void text(Component text, float x, float y, ViewerCategory.TextAlign align, boolean shadow, boolean overrideColor,
                    @Nullable Component tooltip) {
                if (tooltip != null) {
                    var font = Minecraft.getInstance().font;
                    // TOOD: should use alignedX
                    if (x <= mouseX && y <= mouseY && mouseX <= x + font.width(text) && mouseY <= y + font.lineHeight) {
                        tooltips.add(tooltip);
                    }
                }
            }

            @Override
            public void arrow(int x, int y) {
            }

            @Override
            public void texture(ResourceLocation loc, int x, int y, int u, int v, int width, int height) {
            }

            @Override
            public void drawable(Consumer<GuiGraphics> widget) {
            }

            @Override
            public void item(double x, double y, double w, double h, ItemLike item) {
            }

            @Override
            public void tooltip(int x, int y, int w, int h, List<Component> tooltip) {
                if (x <= mouseX && y <= mouseY && mouseX <= x + w && mouseY <= y + h) {
                    tooltips.addAll(tooltip);
                }
            }
        });

        return tooltips;
    }
}
