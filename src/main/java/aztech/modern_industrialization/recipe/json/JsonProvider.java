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
package aztech.modern_industrialization.recipe.json;

import com.google.gson.JsonObject;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class JsonProvider implements RecipeJsonProvider {

    private final RecipeSerializer<?> serializer;
    private final Identifier recipeId;
    private final RecipeJson recipe;

    public JsonProvider(RecipeSerializer<?> serializer, Identifier recipeId, RecipeJson recipe) {
        this.serializer = serializer;
        this.recipeId = recipeId;
        this.recipe = recipe;
    }

    @Override
    public void serialize(JsonObject json) {
        throw new UnsupportedOperationException("We override toJson()");
    }

    @Override
    public JsonObject toJson() {
        return recipe.toJsonObject();
    }

    @Override
    public Identifier getRecipeId() {
        return recipeId;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return serializer;
    }

    @Nullable
    @Override
    public JsonObject toAdvancementJson() {
        return null;
    }

    @Nullable
    @Override
    public Identifier getAdvancementId() {
        return null;
    }
}
