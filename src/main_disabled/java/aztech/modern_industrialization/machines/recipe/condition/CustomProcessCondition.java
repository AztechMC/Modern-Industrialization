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
package aztech.modern_industrialization.machines.recipe.condition;

import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import net.minecraft.network.chat.Component;

public class CustomProcessCondition implements MachineProcessCondition {
    public static final Serde SERIALIZER = new Serde();

    static final Map<String, Definition> definitions = new HashMap<>();

    public static void onReload() {
        definitions.clear();

        KubeJSProxy.instance.fireCustomConditionEvent();
    }

    public static void register(String id, BiPredicate<MachineProcessCondition.Context, MachineRecipe> predicate, Component... description) {
        Objects.requireNonNull(predicate);

        if (definitions.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate custom process condition definition: " + id);
        }

        if (description.length == 0) {
            throw new IllegalArgumentException("Custom process condition must have a description");
        }

        definitions.put(id, new Definition(predicate, List.of(description)));
    }

    private record Definition(
            BiPredicate<MachineProcessCondition.Context, MachineRecipe> predicate,
            List<Component> description) {
    }

    public CustomProcessCondition(String id) {
        var definition = definitions.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown custom process condition definition: " + id);
        }
        this.id = id;
        this.description = definition.description;
    }

    public CustomProcessCondition(String id, List<Component> description) {
        this.id = id;
        this.description = description;
    }

    private final String id;
    private final List<Component> description;

    @Override
    public boolean canProcessRecipe(Context context, MachineRecipe recipe) {
        return definitions.get(id).predicate.test(context, recipe);
    }

    @Override
    public void appendDescription(List<Component> list) {
        list.addAll(description);
    }

    @Override
    public Serializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static class Serde implements Serializer<CustomProcessCondition> {
        @Override
        public CustomProcessCondition fromJson(JsonObject json) {
            var id = json.get("custom_id").getAsString();

            if (json.has("description")) {
                List<Component> description = new ArrayList<>();
                for (var element : json.getAsJsonArray("description")) {
                    description.add(Component.Serializer.fromJson(element));
                }

                return new CustomProcessCondition(id, description);
            } else {
                return new CustomProcessCondition(id);
            }
        }

        @Override
        public JsonObject toJson(CustomProcessCondition condition, boolean syncToClient) {
            var obj = new JsonObject();
            obj.addProperty("custom_id", condition.id);

            if (syncToClient) {
                var description = new JsonArray();
                for (var line : condition.description) {
                    description.add(Component.Serializer.toJsonTree(line));
                }
                obj.add("description", description);
            }

            return obj;
        }
    }
}
