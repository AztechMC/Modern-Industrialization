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

import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessConditions;
import com.google.gson.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class MachineRecipeType implements RecipeType<MachineRecipe>, RecipeSerializer<MachineRecipe> {

    public MachineRecipeType(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Never modify or store the result!
     */
    protected Collection<MachineRecipe> getManagerRecipes(Level world) {
        return world.getRecipeManager().getAllRecipesFor(this);
    }

    public Collection<MachineRecipe> getRecipes(Level world) {
        return getManagerRecipes(world);
    }

    @Nullable
    public MachineRecipe getRecipe(Level world, ResourceLocation id) {
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
    private final Map<Item, List<MachineRecipe>> recipeCache = new HashMap<>();
    private final List<MachineRecipe> fluidOnlyRecipes = new ArrayList<>();
    private long lastUpdate = 0;
    private static final long UPDATE_INTERVAL = 20 * 1000; // 20 seconds

    /**
     * Update recipe cache if necessary
     */
    private void updateRecipeCache(ServerLevel world) {
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
    public Collection<MachineRecipe> getMatchingRecipes(ServerLevel world, Item input) {
        updateRecipeCache(world);
        return Collections.unmodifiableCollection(recipeCache.getOrDefault(input, Collections.emptyList()));
    }

    /**
     * Get all recipes that are not using any input item.
     */
    public Collection<MachineRecipe> getFluidOnlyRecipes(ServerLevel world) {
        updateRecipeCache(world);
        return Collections.unmodifiableList(fluidOnlyRecipes);
    }

    private final ResourceLocation id;
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

    public ResourceLocation getId() {
        return id;
    }

    public String getPath() {
        return id.getPath();
    }

    private void validateRecipe(MachineRecipe recipe) {
        if (!allowItemInput && recipe.itemInputs.size() > 0)
            throw new IllegalArgumentException("Item inputs are not allowed.");
        if (!allowFluidInput && recipe.fluidInputs.size() > 0)
            throw new IllegalArgumentException("Fluid inputs are not allowed.");
        if (!allowItemOutput && recipe.itemOutputs.size() > 0)
            throw new IllegalArgumentException("Item outputs are not allowed.");
        if (!allowFluidOutput && recipe.fluidOutputs.size() > 0)
            throw new IllegalArgumentException("Fluid outputs are not allowed.");
        if (recipe.itemInputs.size() + recipe.fluidInputs.size() == 0)
            throw new IllegalArgumentException("Must have at least one fluid or item input.");
        if (recipe.itemOutputs.size() + recipe.fluidOutputs.size() == 0)
            throw new IllegalArgumentException("Must have at least one fluid or item output.");

        if (this == MIMachineRecipeTypes.FORGE_HAMMER) {
            if (recipe.itemInputs.size() != 1)
                throw new IllegalArgumentException("Must have exactly one item input.");
            if (recipe.itemOutputs.size() != 1)
                throw new IllegalArgumentException("Must have exactly one item output.");
            if (recipe.itemInputs.get(0).probability != 1.0f || recipe.itemOutputs.get(0).probability != 1.0f)
                throw new IllegalArgumentException("Chanced recipes are not supported for the forge hammer.");
        }
    }

    @Override
    public MachineRecipe fromJson(ResourceLocation id, JsonObject json) {
        MachineRecipe recipe = new MachineRecipe(id, this);

        if (this.id.equals(MIMachineRecipeTypes.FORGE_HAMMER.id)) {
            recipe.eu = json.has("hammer_damage") ? readNonNegativeInt(json, "hammer_damage") : 0;
            recipe.duration = 0;
        } else {
            recipe.eu = readPositiveInt(json, "eu");
            recipe.duration = readPositiveInt(json, "duration");
        }

        recipe.itemInputs = readArray(json, "item_inputs", MachineRecipeType::readItemInput);
        recipe.fluidInputs = readArray(json, "fluid_inputs", MachineRecipeType::readFluidInput);
        recipe.itemOutputs = readArray(json, "item_outputs", MachineRecipeType::readItemOutput);
        recipe.fluidOutputs = readArray(json, "fluid_outputs", MachineRecipeType::readFluidOutput);

        recipe.conditions = readConditions(json);

        validateRecipe(recipe);

        return recipe;
    }

    private static int readPositiveInt(JsonObject json, String element) {
        int x = GsonHelper.getAsInt(json, element);
        if (x <= 0)
            throw new IllegalArgumentException(element + " should be a positive integer.");
        return x;
    }

    private static int readNonNegativeInt(JsonObject json, String element) {
        int x = GsonHelper.getAsInt(json, element);
        if (x < 0)
            throw new IllegalArgumentException(element + " should be a positive integer.");
        return x;
    }

    private static int readFluidAmount(JsonObject json, String element) {
        double amountMb = GsonHelper.getAsDouble(json, element);
        int amount = (int) Math.round(amountMb * 81);
        if (amount < 0) {
            throw new IllegalArgumentException(element + " should be a positive fluid amount.");
        }
        return amount;
    }

    private static float readProbability(JsonObject json, String element) {
        if (GsonHelper.isValidPrimitive(json, element)) {
            float x = GsonHelper.getAsFloat(json, element);
            if (x < 0 || x > 1)
                throw new IllegalArgumentException(element + " should be a float between 0 and 1.");
            return x;
        } else {
            return 1;
        }
    }

    private static ResourceLocation readIdentifier(JsonObject json, String element) {
        return new ResourceLocation(GsonHelper.getAsString(json, element));
    }

    private static <T> List<T> readArray(JsonObject json, String element, Function<JsonObject, T> reader) {
        if (!GsonHelper.isArrayNode(json, element)) {
            // If there is no array, try to parse a single element object instead.
            JsonElement backupObject = json.get(element);
            if (backupObject != null && backupObject.isJsonObject()) {
                return Arrays.asList(reader.apply(backupObject.getAsJsonObject()));
            } else {
                return Collections.emptyList();
            }
        } else {
            JsonArray array = GsonHelper.getAsJsonArray(json, element);
            JsonObject[] objects = new JsonObject[array.size()];
            for (int i = 0; i < objects.length; i++) {
                objects[i] = array.get(i).getAsJsonObject();
            }
            return Arrays.stream(objects).map(reader).collect(Collectors.toList());
        }
    }

    public static MachineRecipe.ItemInput readItemInput(JsonObject json) {
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
        ResourceLocation id = readIdentifier(json, "fluid");
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElseThrow(() -> {
            throw new IllegalArgumentException("Fluid " + id + " does not exist.");
        });
        int amount = readFluidAmount(json, "amount");
        float probability = readProbability(json, "probability");
        return new MachineRecipe.FluidInput(fluid, amount, probability);
    }

    private static MachineRecipe.ItemOutput readItemOutput(JsonObject json) {
        ResourceLocation id = readIdentifier(json, "item");
        Item item = BuiltInRegistries.ITEM.getOptional(id).orElseThrow(() -> {
            throw new IllegalArgumentException("Item " + id + " does not exist.");
        });
        int amount = 1;
        if (json.has("amount")) {
            amount = readPositiveInt(json, "amount");
        }
        float probability = readProbability(json, "probability");
        return new MachineRecipe.ItemOutput(item, amount, probability);
    }

    private static MachineRecipe.FluidOutput readFluidOutput(JsonObject json) {
        ResourceLocation id = readIdentifier(json, "fluid");
        Fluid fluid = BuiltInRegistries.FLUID.getOptional(id).orElseThrow(() -> {
            throw new IllegalArgumentException("Fluid " + id + " does not exist.");
        });
        int amount = readFluidAmount(json, "amount");
        float probability = readProbability(json, "probability");
        return new MachineRecipe.FluidOutput(fluid, amount, probability);
    }

    private static List<MachineProcessCondition> readConditions(JsonObject object) {
        if (!object.has("process_conditions")) {
            return List.of();
        }
        JsonArray array = GsonHelper.getAsJsonArray(object, "process_conditions");
        List<MachineProcessCondition> conditions = new ArrayList<>(array.size());
        for (var condJson : array) {
            var obj = condJson.getAsJsonObject();
            var id = new ResourceLocation(GsonHelper.getAsString(obj, "id"));
            var serializer = MachineProcessConditions.get(id);
            if (serializer == null) {
                throw new IllegalArgumentException("Unknown machine process condition " + id);
            }
            conditions.add(serializer.fromJson(obj));
        }
        return conditions;
    }

    private static <T> List<T> readList(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> reader) {
        List<T> l = new ArrayList<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; ++i) {
            l.add(reader.apply(buf));
        }
        return l;
    }

    private static <T> void writeList(FriendlyByteBuf buf, List<T> list, BiConsumer<FriendlyByteBuf, T> writer) {
        buf.writeVarInt(list.size());
        for (T t : list) {
            writer.accept(buf, t);
        }
    }

    @Override
    public MachineRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
        MachineRecipe recipe = new MachineRecipe(id, this);
        recipe.eu = buf.readVarInt();
        recipe.duration = buf.readVarInt();
        recipe.itemInputs = readList(buf, b -> new MachineRecipe.ItemInput(Ingredient.fromNetwork(b), b.readVarInt(), b.readFloat()));
        recipe.fluidInputs = readList(buf,
                b -> new MachineRecipe.FluidInput(BuiltInRegistries.FLUID.byId(b.readVarInt()), b.readVarLong(), b.readFloat()));
        recipe.itemOutputs = readList(buf, b -> new MachineRecipe.ItemOutput(Item.byId(b.readVarInt()), b.readVarInt(), b.readFloat()));
        recipe.fluidOutputs = readList(buf,
                b -> new MachineRecipe.FluidOutput(BuiltInRegistries.FLUID.byId(b.readVarInt()), b.readVarLong(), b.readFloat()));
        recipe.conditions = readList(buf, b -> {
            var serializer = MachineProcessConditions.get(b.readResourceLocation());
            var json = b.readUtf();
            return serializer.fromJson(JsonParser.parseString(json).getAsJsonObject());
        });

        return recipe;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, MachineRecipe recipe) {
        buf.writeVarInt(recipe.eu);
        buf.writeVarInt(recipe.duration);
        writeList(buf, recipe.itemInputs, (b, i) -> {
            i.ingredient.toNetwork(buf);
            buf.writeVarInt(i.amount);
            buf.writeFloat(i.probability);
        });
        writeList(buf, recipe.fluidInputs, (b, i) -> {
            buf.writeVarInt(BuiltInRegistries.FLUID.getId(i.fluid));
            buf.writeVarLong(i.amount);
            buf.writeFloat(i.probability);
        });
        writeList(buf, recipe.itemOutputs, (b, i) -> {
            buf.writeVarInt(Item.getId(i.item));
            buf.writeVarInt(i.amount);
            buf.writeFloat(i.probability);
        });
        writeList(buf, recipe.fluidOutputs, (b, i) -> {
            buf.writeVarInt(BuiltInRegistries.FLUID.getId(i.fluid));
            buf.writeVarLong(i.amount);
            buf.writeFloat(i.probability);
        });
        writeList(buf, recipe.conditions, (b, cond) -> {
            var serializer = cond.getSerializer();
            buf.writeResourceLocation(MachineProcessConditions.getId(serializer));
            buf.writeUtf(serializer.toJson(cast(cond), true).toString());
        });
    }

    private static <T> T cast(Object object) {
        return (T) object;
    }
}
