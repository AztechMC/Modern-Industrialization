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

package aztech.modern_industrialization.advancement.multiblock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public record BuiltMultiblockContext(ResourceLocation id, Optional<String> shape) {
    public static final Codec<BuiltMultiblockContext> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(BuiltMultiblockContext::id),
                    Codec.STRING.optionalFieldOf("shape").forGetter(BuiltMultiblockContext::shape))
            .apply(instance, BuiltMultiblockContext::new));

    public static final StreamCodec<ByteBuf, BuiltMultiblockContext> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            BuiltMultiblockContext::id,
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8),
            BuiltMultiblockContext::shape,
            BuiltMultiblockContext::new);

    public BuiltMultiblockContext(ResourceLocation id, @Nullable String shape) {
        this(id, shape == null || shape.isEmpty() ? Optional.empty() : Optional.of(shape));
    }
}
