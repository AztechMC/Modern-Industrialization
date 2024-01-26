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
package aztech.modern_industrialization.compat.viewer.abstraction;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant;
import aztech.modern_industrialization.util.Rectangle;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

/**
 * @param <D> Data of each "recipe" in the category.
 */
public abstract class ViewerCategory<D> {
    public final Class<D> dataClass;
    public final ResourceLocation id;
    public final Component title;
    public final Icon icon;
    public final int width;
    public final int height;

    protected ViewerCategory(Class<D> dataClass, ResourceLocation id, Component title, ItemStack icon, int width, int height) {
        this(dataClass, id, title, new Icon.Stack(icon), width, height);
    }

    protected ViewerCategory(Class<D> dataClass, ResourceLocation id, Component title, Icon icon, int width, int height) {
        this.dataClass = dataClass;
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.width = width;
        this.height = height;
    }

    public abstract void buildWorkstations(WorkstationConsumer consumer);

    public abstract void buildRecipes(RecipeManager recipeManager, RegistryAccess registryAccess, Consumer<D> consumer);

    /**
     * Add input and outputs items/fluids.
     */
    public abstract void buildLayout(D recipe, LayoutBuilder builder);

    /**
     * Add additional widgets.
     */
    public abstract void buildWidgets(D recipe, WidgetList widgets);

    public sealed interface Icon {
        record Stack(ItemStack stack) implements Icon {
        }

        record Texture(ResourceLocation loc, int u, int v) implements Icon {
        }
    }

    public interface WorkstationConsumer {
        void accept(ItemLike... item);

        default void accept(String... itemPath) {// only items in the MI namespace!
            for (var item : itemPath) {
                accept(BuiltInRegistries.ITEM.get(new MIIdentifier(item)));
            }
        }
    }

    public interface LayoutBuilder {
        SlotBuilder inputSlot(int x, int y);

        SlotBuilder outputSlot(int x, int y);

        void invisibleOutput(ItemStack item);
    }

    public interface SlotBuilder {
        SlotBuilder variant(TransferVariant<?> variant); // no amount is shown

        SlotBuilder fluid(FluidVariant fluid, long amount, float probability);

        default SlotBuilder item(ItemStack stack) {
            return item(stack, 1);
        }

        SlotBuilder item(ItemStack stack, float probability);

        SlotBuilder ingredient(Ingredient ingredient, long amount, float probability);

        SlotBuilder removeBackground();

        SlotBuilder markCatalyst();
    }

    public interface WidgetList {
        void text(Component text, float x, float y, TextAlign align, boolean shadow, boolean overrideColor, @Nullable Component tooltip);

        default void secondaryText(Component text, float x, float y) {
            text(text, x, y, TextAlign.LEFT, false, true, null);
        }

        void arrow(int x, int y);

        void texture(ResourceLocation loc, int x, int y, int u, int v, int width, int height);

        default void rectangle(Rectangle rectangle, int fillColor) {
            drawable(guiGraphics -> {
                guiGraphics.fill(rectangle.x(), rectangle.y(), rectangle.x() + rectangle.w(), rectangle.y() + rectangle.h(), fillColor);
            });
        }

        void drawable(Consumer<GuiGraphics> widget);

        void item(double x, double y, double w, double h, ItemLike item);

        void tooltip(int x, int y, int w, int h, List<Component> tooltip);
    }

    public enum TextAlign {
        LEFT,
        CENTER,
        RIGHT
    }
}
