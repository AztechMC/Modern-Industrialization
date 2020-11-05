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
package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.mixin.RecipeManagerAccessor;
import com.google.gson.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class MachineRecipeType implements RecipeType, RecipeSerializer {

    public MachineRecipeType(Identifier id) {
        this.id = id;
    }

    /**
     * Never modify or store the result!
     */
    @SuppressWarnings("unchecked")
    public Collection<MachineRecipe> getRecipes(ServerWorld world) {
        return (Collection<MachineRecipe>) (Collection) ((RecipeManagerAccessor) world.getRecipeManager()).modern_industrialization_getAllOfType(this)
                .values();
    }

    public MachineRecipe getRecipe(ServerWorld world, Identifier id) {
        return getRecipes(world).stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }

    /*
     * Smart recipe system to avoid iterating over all available recipes. Every
     * recipe can be accessed by the type of its first input, so we build a cache
     * Item -> MachineRecipe. We also need to store recipes that have fluid inputs
     * but no item inputs. This cache is updated every 20 seconds in case the
     * recipes are sometimes reloaded. TODO: only rebuild when the recipes have been
     * reloaded
     */
    private Map<Item, List<MachineRecipe>> recipeCache = new HashMap<>();
    private List<MachineRecipe> fluidOnlyRecipes = new ArrayList<>();
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 20 * 1000; // 20 seconds

    /**
     * Update recipe cache if necessary
     */
    private void updateRecipeCache(ServerWorld world) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate <= UPDATE_INTERVAL)
            return;

        // Update cache
        lastUpdate = time;
        recipeCache.clear();
        fluidOnlyRecipes.clear();
        for (MachineRecipe recipe : getRecipes(world)) {
            if (recipe.itemInputs.size() == 0) {
                if (recipe.fluidInputs.size() > 0) {
                    fluidOnlyRecipes.add(recipe);
                }
            } else {
                for (Item inputItem : recipe.itemInputs.get(0).getInputItems()) {
                    recipeCache.putIfAbsent(inputItem, new ArrayList<>());
                    recipeCache.get(inputItem).add(recipe);
                }
            }
        }
    }

    /**
     * Get all recipes that are using some Item.
     */
    public Collection<MachineRecipe> getMatchingRecipes(ServerWorld world, Item input) {
        updateRecipeCache(world);
        return Collections.unmodifiableCollection(recipeCache.getOrDefault(input, Collections.emptyList()));
    }

    /**
     * Get all recipes that are not using any input item.
     */
    public Collection<MachineRecipe> getFluidOnlyRecipes(ServerWorld world) {
        updateRecipeCache(world);
        return Collections.unmodifiableList(fluidOnlyRecipes);
    }

    private final Identifier id;
    private boolean allowItemInput = false;
    private boolean allowFluidInput = false;
    private boolean allowItemOutput = false;
    private boolean allowFluidOutput = false;

    public MachineRecipeType withItemInputs() {
        allowItemInput = true;
        return this;
    }

    public MachineRecipeType withFluidInputs() {
        allowFluidInput = true;
        return this;
    }

    public MachineRecipeType withItemOutputs() {
        allowItemOutput = true;
        return this;
    }

    public MachineRecipeType withFluidOutputs() {
        allowFluidOutput = true;
        return this;
    }

    public Identifier getId() {
        return id;
    }

    private void validateRecipe(MachineRecipe recipe) {
        if (!allowItemInput && recipe.itemInputs.size() > 0)
            throw new RuntimeException("Item inputs are not allowed.");
        if (!allowFluidInput && recipe.fluidInputs.size() > 0)
            throw new RuntimeException("Fluid inputs are not allowed.");
        if (!allowItemOutput && recipe.itemOutputs.size() > 0)
            throw new RuntimeException("Item outputs are not allowed.");
        if (!allowFluidOutput && recipe.fluidOutputs.size() > 0)
            throw new RuntimeException("Fluid outputs are not allowed.");
        if (recipe.itemInputs.size() + recipe.fluidInputs.size() == 0)
            throw new RuntimeException("Must have at least one fluid or item input.");
        if (recipe.itemOutputs.size() + recipe.fluidOutputs.size() == 0)
            throw new RuntimeException("Must have at least one fluid or item output.");
    }

    @Override
    public Recipe<?> read(Identifier id, JsonObject json) {
        MachineRecipe recipe = new MachineRecipe(id, this);
        recipe.eu = readPositiveInt(json, "eu");
        recipe.duration = readPositiveInt(json, "duration");
        recipe.itemInputs = readArray(json, "item_inputs", MachineRecipeType::readItemInput);
        recipe.fluidInputs = readArray(json, "fluid_inputs", MachineRecipeType::readFluidInput);
        recipe.itemOutputs = readArray(json, "item_outputs", MachineRecipeType::readItemOutput);
        recipe.fluidOutputs = readArray(json, "fluid_outputs", MachineRecipeType::readFluidOutput);

        validateRecipe(recipe);

        return recipe;
    }

    private static int readPositiveInt(JsonObject json, String element) {
        int x = JsonHelper.getInt(json, element);
        if (x <= 0)
            throw new RuntimeException(element + " should be a positive integer.");
        return x;
    }

    private static int readNonNegativeInt(JsonObject json, String element) {
        int x = JsonHelper.getInt(json, element);
        if (x < 0)
            throw new RuntimeException(element + " should be a positive integer.");
        return x;
    }

    private static float readProbability(JsonObject json, String element) {
        if (JsonHelper.hasPrimitive(json, element)) {
            float x = JsonHelper.getFloat(json, element);
            if (x < 0 || x > 1)
                throw new RuntimeException(element + " should be a float between 0 and 1.");
            return x;
        } else {
            return 1;
        }
    }

    private static Identifier readIdentifier(JsonObject json, String element) {
        return new Identifier(JsonHelper.getString(json, element));
    }

    private static <T> List<T> readArray(JsonObject json, String element, Function<JsonObject, T> reader) {
        if (!JsonHelper.hasArray(json, element)) {
            // If there is no array, try to parse a single element object instead.
            JsonElement backupObject = json.get(element);
            if (backupObject != null && backupObject.isJsonObject()) {
                return Arrays.asList(reader.apply(backupObject.getAsJsonObject()));
            } else {
                return Collections.emptyList();
            }
        } else {
            JsonArray array = JsonHelper.getArray(json, element);
            JsonObject[] objects = new JsonObject[array.size()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = array.get(i).getAsJsonObject();
            }
            return Arrays.stream(objects).map(reader).collect(Collectors.toList());
        }
    }

    private static MachineRecipe.ItemInput readItemInput(JsonObject json) {
        int amount = 1;
        if (json.has("amount")) {
            amount = readNonNegativeInt(json, "amount");
        } else if (json.has("count")) {
            amount = readNonNegativeInt(json, "count");
        }
        float probability = readProbability(json, "probability");
        Ingredient ingredient;

        if (json.has("ingredient")) {
            ingredient = Ingredient.fromJson(json.get("ingredient"));
        } else {
            ingredient = Ingredient.fromJson(json);
        }
        return new MachineRecipe.ItemInput(ingredient, amount, probability);
    }

    private static MachineRecipe.FluidInput readFluidInput(JsonObject json) {
        Identifier id = readIdentifier(json, "fluid");
        Fluid fluid = Registry.FLUID.getOrEmpty(id).<RuntimeException>orElseThrow(() -> {
            throw new RuntimeException("Fluid " + id + " does not exist.");
        });
        int amount = readNonNegativeInt(json, "amount");
        float probability = readProbability(json, "probability");
        return new MachineRecipe.FluidInput(fluid, amount, probability);
    }

    private static MachineRecipe.ItemOutput readItemOutput(JsonObject json) {
        Identifier id = readIdentifier(json, "item");
        Item item = Registry.ITEM.getOrEmpty(id).<RuntimeException>orElseThrow(() -> {
            throw new RuntimeException("Item " + id + " does not exist.");
        });
        int amount = readPositiveInt(json, "amount");
        float probability = readProbability(json, "probability");
        return new MachineRecipe.ItemOutput(item, amount, probability);
    }

    private static MachineRecipe.FluidOutput readFluidOutput(JsonObject json) {
        Identifier id = readIdentifier(json, "fluid");
        Fluid fluid = Registry.FLUID.getOrEmpty(id).<RuntimeException>orElseThrow(() -> {
            throw new RuntimeException("Fluid " + id + " does not exist.");
        });
        int amount = readPositiveInt(json, "amount");
        float probability = readProbability(json, "probability");
        return new MachineRecipe.FluidOutput(fluid, amount, probability);
    }

    public JsonObject write(MachineRecipe recipe) {
        JsonObject json = new JsonObject();
        json.addProperty("eu", recipe.eu);
        json.addProperty("duration", recipe.duration);
        json.add("item_inputs", writeArray(recipe.itemInputs, MachineRecipeType::writeItemInput));
        json.add("fluid_inputs", writeArray(recipe.fluidInputs, MachineRecipeType::writeFluidInput));
        json.add("item_outputs", writeArray(recipe.itemOutputs, MachineRecipeType::writeItemOutput));
        json.add("fluid_outputs", writeArray(recipe.fluidOutputs, MachineRecipeType::writeFluidOutput));
        return json;
    }

    private static JsonObject writeItemInput(MachineRecipe.ItemInput itemInput) {
        JsonObject json = new JsonObject();
        json.add("ingredient", itemInput.ingredient.toJson());
        json.addProperty("amount", itemInput.amount);
        json.addProperty("probability", itemInput.probability);
        return json;
    }

    private static JsonObject writeFluidInput(MachineRecipe.FluidInput fluidInput) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", Registry.FLUID.getId(fluidInput.fluid).toString());
        json.addProperty("amount", fluidInput.amount);
        return json;
    }

    private static JsonObject writeItemOutput(MachineRecipe.ItemOutput itemOutput) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Registry.ITEM.getId(itemOutput.item).toString());
        json.addProperty("amount", itemOutput.amount);
        json.addProperty("probability", itemOutput.probability);
        return json;
    }

    private static JsonObject writeFluidOutput(MachineRecipe.FluidOutput fluidOutput) {
        JsonObject json = new JsonObject();
        json.addProperty("fluid", Registry.FLUID.getId(fluidOutput.fluid).toString());
        json.addProperty("amount", fluidOutput.amount);
        return json;
    }

    private static <T> JsonArray writeArray(List<T> list, Function<T, JsonObject> writer) {
        JsonArray json = new JsonArray();
        list.stream().map(writer).forEach(json::add);
        return json;
    }

    private static final Gson GSON = new GsonBuilder().create();

    @Override
    public Recipe<?> read(Identifier id, PacketByteBuf buf) {
        String input = buf.readString(buf.readInt());
        return read(id, GSON.fromJson(input, JsonObject.class));
    }

    @Override
    public void write(PacketByteBuf buf, Recipe recipe) {
        String json = GSON.toJson(write((MachineRecipe) recipe));
        buf.writeInt(json.length());
        buf.writeString(json);
    }
}
