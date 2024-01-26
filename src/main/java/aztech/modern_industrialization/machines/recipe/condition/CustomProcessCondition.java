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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class CustomProcessCondition implements MachineProcessCondition {
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

    private static Codec<CustomProcessCondition> makeCodec(boolean syncToClient) {
        return RecordCodecBuilder.create(
                g -> g
                        .group(
                                Codec.STRING.fieldOf("custom_id").forGetter(c -> c.id),
                                ComponentSerialization.CODEC.listOf().optionalFieldOf("description")
                                        .forGetter(c -> syncToClient ? Optional.of(c.description) : Optional.empty()))
                        .apply(g, (id, desc) -> desc.map(d -> new CustomProcessCondition(id, d)).orElseGet(() -> new CustomProcessCondition(id))));
    }

    static final Codec<CustomProcessCondition> CODEC = makeCodec(false);
    private static final Codec<CustomProcessCondition> CODEC_FOR_SYNC = makeCodec(true);

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
    public Codec<? extends MachineProcessCondition> codec(boolean syncToClient) {
        return syncToClient ? CODEC_FOR_SYNC : CODEC;
    }
}
