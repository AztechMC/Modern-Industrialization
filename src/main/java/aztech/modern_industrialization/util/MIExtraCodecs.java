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
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

public class MIExtraCodecs {
    public static final Codec<Float> FLOAT_01 = Codec.floatRange(0, 1);
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
}
