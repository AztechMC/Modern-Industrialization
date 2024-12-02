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
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class MIExtraCodecs {
    public static final Codec<Float> FLOAT_01 = Codec.floatRange(0, 1);
    public static final Codec<Long> NON_NEGATIVE_LONG = longRange(0, Long.MAX_VALUE);
    public static final Codec<Long> POSITIVE_LONG = longRange(1, Long.MAX_VALUE);
    public static final Codec<Lazy<BlockState>> LAZY_BLOCK_STATE = lazy(BlockState.CODEC);

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

    /**
     * A codec that wraps another codec in a Lazy object.
     */
    public static <B> Codec<Lazy<B>> lazy(Codec<B> baseCodec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<Pair<Lazy<B>, T>> decode(DynamicOps<T> ops, T input) {
                return DataResult.success(Pair.of(Lazy.of(() -> baseCodec.decode(ops, input).getOrThrow().getFirst()), input));
            }

            @Override
            public <T> DataResult<T> encode(Lazy<B> input, DynamicOps<T> ops, T prefix) {
                return baseCodec.encode(input.get(), ops, prefix);
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1, final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2, final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3, final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4, final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5, final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6, final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7, final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8, final Function<C, T8> getter8,
            final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> function) {
        return new StreamCodec<>() {
            public C decode(B buf) {
                T1 t1 = codec1.decode(buf);
                T2 t2 = codec2.decode(buf);
                T3 t3 = codec3.decode(buf);
                T4 t4 = codec4.decode(buf);
                T5 t5 = codec5.decode(buf);
                T6 t6 = codec6.decode(buf);
                T7 t7 = codec7.decode(buf);
                T8 t8 = codec8.decode(buf);
                return function.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            public void encode(B buf, C value) {
                codec1.encode(buf, getter1.apply(value));
                codec2.encode(buf, getter2.apply(value));
                codec3.encode(buf, getter3.apply(value));
                codec4.encode(buf, getter4.apply(value));
                codec5.encode(buf, getter5.apply(value));
                codec6.encode(buf, getter6.apply(value));
                codec7.encode(buf, getter7.apply(value));
                codec8.encode(buf, getter8.apply(value));
            }
        };
    }
}
