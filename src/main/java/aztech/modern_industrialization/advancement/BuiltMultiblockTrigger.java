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

package aztech.modern_industrialization.advancement;

import aztech.modern_industrialization.MIAdvancementTriggers;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;

public class BuiltMultiblockTrigger extends SimpleCriterionTrigger<BuiltMultiblockTrigger.TriggerInstance> {
    public static Criterion<TriggerInstance> builtMultiblock(ResourceLocation multiblockId, @Nullable String shapeId) {
        return MIAdvancementTriggers.BUILT_MULTIBLOCK.get().createCriterion(new TriggerInstance(
                Optional.empty(),
                Optional.of(new MultiblockPredicate(multiblockId, Optional.ofNullable(shapeId)))));
    }

    public static Criterion<TriggerInstance> builtMultiblock(ResourceLocation multiblockId) {
        return builtMultiblock(multiblockId, null);
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, MultiblockMachineBlockEntity machine) {
        var multiblockId = machine.guiParams.blockId;
        var shapeId = machine.getActiveShapeId();
        this.trigger(player, (instance) -> instance.matches(player, multiblockId, shapeId));
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MultiblockPredicate> multiblock) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance
                .group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                        MultiblockPredicate.CODEC.optionalFieldOf("multiblock").forGetter(TriggerInstance::multiblock))
                .apply(instance, TriggerInstance::new));

        public boolean matches(ServerPlayer player, ResourceLocation multiblockId, @Nullable String shapeId) {
            return multiblock.map((predicate) -> predicate.matches(multiblockId, shapeId)).orElse(true);
        }
    }

    public record MultiblockPredicate(ResourceLocation id, Optional<String> shape) {
        public static final Codec<MultiblockPredicate> CODEC = RecordCodecBuilder.create((instance) -> instance
                .group(
                        ResourceLocation.CODEC.fieldOf("id").forGetter(MultiblockPredicate::id),
                        Codec.STRING.optionalFieldOf("shape").forGetter(MultiblockPredicate::shape))
                .apply(instance, MultiblockPredicate::new));

        public boolean matches(ResourceLocation multiblockId, @Nullable String shapeId) {
            return id.equals(multiblockId) &&
                    shape.map((shape) -> shape.equals(shapeId)).orElse(true);
        }
    }
}
