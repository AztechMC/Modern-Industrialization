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
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class MIExtraCodecs {
    public static final Codec<Float> FLOAT_01 = Codec.floatRange(0, 1);
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

    /**
     * A codec that can accept a single element, or a list of potentially conditional elements.
     */
    public static <T> MapCodec<List<T>> maybeList(Codec<T> elementCodec, String field) {
        var listCodec = NeoForgeExtraCodecs.listWithOptionalElements(ConditionalOps.createConditionalCodec(elementCodec));
        var maybeListCodec = ExtraCodecs.either(elementCodec, listCodec)
                .xmap(either -> either.map(List::of, Function.identity()), Either::right);
        return ExtraCodecs.strictOptionalField(maybeListCodec, field, List.of());
    }

    public static <F, S> MapCodec<Either<F, S>> xor(MapCodec<F> first, MapCodec<S> second) {
        return new XorMapCodec<>(first, second);
    }

    public static <T> MapCodec<T> optionalFieldAlwaysWrite(Codec<T> baseCodec, String field, T defaultValue) {
        return ExtraCodecs.strictOptionalField(baseCodec, field)
                .xmap(read -> read.orElse(defaultValue), Optional::of);
    }

    private static final class XorMapCodec<F, S> extends MapCodec<Either<F, S>> {
        private final MapCodec<F> first;
        private final MapCodec<S> second;

        private XorMapCodec(MapCodec<F> first, MapCodec<S> second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(first.keys(ops), second.keys(ops)).distinct();
        }

        @Override
        public <T> DataResult<Either<F, S>> decode(DynamicOps<T> ops, MapLike<T> input) {
            DataResult<Either<F, S>> firstResult = first.decode(ops, input).map(Either::left);
            DataResult<Either<F, S>> secondResult = second.decode(ops, input).map(Either::right);
            var firstValue = firstResult.result();
            var secondValue = secondResult.result();
            if (firstValue.isPresent() && secondValue.isPresent()) {
                return DataResult.error(
                        () -> "Both alternatives read successfully, can not pick the correct one; first: " + firstValue.get() + " second: "
                                + secondValue.get(),
                        firstValue.get());
            } else if (firstValue.isPresent()) {
                return firstResult;
            } else if (secondValue.isPresent()) {
                return secondResult;
            } else {
                return firstResult.apply2((x, y) -> y, secondResult);
            }
        }

        @Override
        public <T> RecordBuilder<T> encode(Either<F, S> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.map(x -> first.encode(x, ops, prefix), x -> second.encode(x, ops, prefix));
        }

        @Override
        public String toString() {
            return "XorMapCodec[" + first + ", " + second + "]";
        }
    }
}
