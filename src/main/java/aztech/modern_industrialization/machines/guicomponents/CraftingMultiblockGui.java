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

package aztech.modern_industrialization.machines.guicomponents;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;

public class CraftingMultiblockGui implements GuiComponentServer<Unit, CraftingMultiblockGui.Data> {
    public static final Type<Unit, Data> TYPE = new Type<>(MI.id("crafting_multiblock_gui"), StreamCodec.unit(Unit.INSTANCE), Data.STREAM_CODEC);

    private final CrafterComponent crafter;
    private final Supplier<Boolean> isShapeValid;
    private final Supplier<Float> progressSupplier;
    private final IntSupplier remainingOverclockTicks;

    public CraftingMultiblockGui(Supplier<Boolean> isShapeValid, Supplier<Float> progressSupplier, CrafterComponent crafter,
            IntSupplier remainingOverclockTicks) {
        this.isShapeValid = isShapeValid;
        this.crafter = crafter;
        this.progressSupplier = progressSupplier;
        this.remainingOverclockTicks = remainingOverclockTicks;
    }

    @Override
    public Unit getParams() {
        return Unit.INSTANCE;
    }

    @Override
    public Data extractData() {
        boolean shapeValid = isShapeValid.get();
        Optional<RecipeData> activeRecipe;
        if (shapeValid && crafter.hasActiveRecipe()) {
            activeRecipe = Optional.of(new RecipeData(progressSupplier.get(), crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(),
                    crafter.getCurrentRecipeEu(), crafter.getBaseRecipeEu()));
        } else {
            activeRecipe = Optional.empty();
        }
        return new Data(shapeValid, activeRecipe, remainingOverclockTicks.getAsInt());
    }

    @Override
    public Type<Unit, Data> getType() {
        return TYPE;
    }

    public record Data(boolean isShapeValid, Optional<RecipeData> activeRecipe, int remainingOverclockTicks) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::isShapeValid,
                ByteBufCodecs.optional(RecipeData.STREAM_CODEC),
                Data::activeRecipe,
                ByteBufCodecs.VAR_INT,
                Data::remainingOverclockTicks,
                Data::new);
    }

    public record RecipeData(float progress, int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu) {
        public static final StreamCodec<RegistryFriendlyByteBuf, RecipeData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT,
                RecipeData::progress,
                ByteBufCodecs.VAR_INT,
                RecipeData::efficiencyTicks,
                ByteBufCodecs.VAR_INT,
                RecipeData::maxEfficiencyTicks,
                ByteBufCodecs.VAR_LONG,
                RecipeData::currentRecipeEu,
                ByteBufCodecs.VAR_LONG,
                RecipeData::baseRecipeEu,
                RecipeData::new);
    }

    public static final int X = 5;
    public static final int Y = 16;
    public static final int W = 166;
    public static final int H = 80;
}
