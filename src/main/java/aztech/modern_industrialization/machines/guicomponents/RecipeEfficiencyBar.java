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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class RecipeEfficiencyBar implements GuiComponentServer<RecipeEfficiencyBar.Params, RecipeEfficiencyBar.Data> {
    public static final Type<Params, Data> TYPE = new Type<>(MI.id("recipe_efficiency_bar"), Params.STREAM_CODEC, Data.STREAM_CODEC);

    private final Params params;
    private final CrafterComponent crafter;

    public RecipeEfficiencyBar(Params params, CrafterComponent crafter) {
        this.params = params;
        this.crafter = crafter;
    }

    @Override
    public Params getParams() {
        return params;
    }

    @Override
    public Data extractData() {
        if (crafter.hasActiveRecipe()) {
            return new Data(true,
                    crafter.getEfficiencyTicks(), crafter.getMaxEfficiencyTicks(),
                    crafter.getCurrentRecipeEu(), crafter.getBaseRecipeEu(), crafter.getBehavior().getMaxRecipeEu());
        } else {
            return new Data(false, 0, 0, 0, 0, crafter.getBehavior().getMaxRecipeEu());
        }
    }

    @Override
    public Type<Params, Data> getType() {
        return TYPE;
    }

    public record Data(boolean hasActiveRecipe, int efficiencyTicks, int maxEfficiencyTicks, long currentRecipeEu, long baseRecipeEu, long maxRecipeEu) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL,
                Data::hasActiveRecipe,
                ByteBufCodecs.VAR_INT,
                Data::efficiencyTicks,
                ByteBufCodecs.VAR_INT,
                Data::maxEfficiencyTicks,
                ByteBufCodecs.VAR_LONG,
                Data::currentRecipeEu,
                ByteBufCodecs.VAR_LONG,
                Data::baseRecipeEu,
                ByteBufCodecs.VAR_LONG,
                Data::maxRecipeEu,
                Data::new);
    }

    public record Params(int renderX, int renderY) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Params> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT,
                Params::renderX,
                ByteBufCodecs.VAR_INT,
                Params::renderY,
                Params::new);
    }
}
