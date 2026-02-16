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

package aztech.modern_industrialization.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.slf4j.Logger;

public class MIExtraCodecs {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Codec<Float> FLOAT_01 = Codec.floatRange(0, 1);
    // TODO: remove in favor of ExtraCodecs equivalents
    public static final Codec<Long> NON_NEGATIVE_LONG = longRange(0, Long.MAX_VALUE);
    public static final Codec<Long> POSITIVE_LONG = longRange(1, Long.MAX_VALUE);

    private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(final N minInclusive, final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]", value);
        };
    }

    static Codec<Long> longRange(final long minInclusive, final long maxInclusive) {
        final Function<Long, DataResult<Long>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.LONG.flatXmap(checker, checker);
    }

    public static <T> Codec<List<T>> maybeList(Codec<T> elementCodec) {
        var listCodec = NeoForgeExtraCodecs.listWithOptionalElements(ConditionalOps.createConditionalCodec(elementCodec));
        return Codec.either(listCodec, elementCodec)
                .xmap(either -> either.map(Function.identity(), List::of), Either::left);
    }

    /**
     * A codec that can accept a single element, or a list of potentially conditional elements.
     */
    public static <T> MapCodec<List<T>> maybeList(Codec<T> elementCodec, String field) {
        return maybeList(elementCodec).optionalFieldOf(field, List.of());
    }

    public static <T> MapCodec<T> optionalFieldAlwaysWrite(Codec<T> baseCodec, String field, T defaultValue) {
        return baseCodec.optionalFieldOf(field)
                .xmap(read -> read.orElse(defaultValue), Optional::of);
    }

    // TODO: PR to NeoForge?
    public static DataResult<HolderLookup.Provider> retrieveRegistries(DynamicOps<?> ops) {
        if (!(ops instanceof RegistryOps<?> registryOps))
            return DataResult.error(() -> "Not a registry ops");

        try {
            return DataResult.success(CommonHooks.extractLookupProvider(registryOps));
        } catch (IllegalArgumentException exception) {
            return DataResult.error(() -> "Could not extract lookup provider: " + exception);
        }
    }

    // TODO: PR to NeoForge?
    public static <T extends ValueIOSerializable> MapCodec<T> valueSerializable(Supplier<T> constructor) {
        return new ValueIOSerializableCodec<>(constructor);
    }

    private static class ValueIOSerializableCodec<A extends ValueIOSerializable> extends MapCodec<A> {
        private final Supplier<A> constructor;

        private ValueIOSerializableCodec(Supplier<A> constructor) {
            this.constructor = constructor;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            throw new UnsupportedOperationException("Cannot retrieve keys of ValueIOSerializableCodec");
        }

        @Override
        public <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input) {
            return retrieveRegistries(ops)
                    .flatMap(registries -> {
                        var tag = ops.convertTo(
                                NbtOps.INSTANCE,
                                ops.createMap(input.entries()));
                        var compoundTag = (CompoundTag) tag;

                        try (var reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                            var valueInput = TagValueInput.create(reporter, registries, compoundTag);
                            var output = constructor.get();
                            output.deserialize(valueInput);
                            return DataResult.success(output);
                        }
                    });
        }

        @Override
        public <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            var result = retrieveRegistries(ops)
                    .map(registries -> {
                        try (var reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                            var valueOutput = TagValueOutput.createWithContext(reporter, registries);
                            input.serialize(valueOutput);
                            var builtTag = valueOutput.buildResult();
                            prefix.build(NbtOps.INSTANCE.convertTo(ops, builtTag));
                            return prefix;
                        }
                    });
            prefix.withErrorsFrom(result);
            return prefix;
        }

        @Override
        public String toString() {
            return "ValueIOSerializable[" + constructor + "]";
        }
    }
}
