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
package aztech.modern_industrialization.items.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record StructureWrenchSelection(List<BlockPos> positions) {
    public static final StructureWrenchSelection EMPTY = new StructureWrenchSelection(List.of());

    public static final Codec<StructureWrenchSelection> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.list(BlockPos.CODEC).fieldOf("positions").forGetter(StructureWrenchSelection::positions))
            .apply(instance, StructureWrenchSelection::new));

    public static final StreamCodec<ByteBuf, StructureWrenchSelection> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
            StructureWrenchSelection::positions,
            StructureWrenchSelection::new);

    public StructureWrenchSelection {
        positions = Collections.unmodifiableList(positions);
    }

    public static StructureWrenchSelection of(List<BlockPos> positions) {
        return positions.isEmpty() ? EMPTY : new StructureWrenchSelection(positions);
    }

    public boolean contains(BlockPos pos) {
        return positions.contains(pos);
    }

    public StructureWrenchSelection add(BlockPos pos) {
        if (contains(pos)) {
            return this;
        }
        var newPositions = new ArrayList<>(positions);
        newPositions.add(pos);
        return new StructureWrenchSelection(Collections.unmodifiableList(newPositions));
    }

    public StructureWrenchSelection remove(BlockPos pos) {
        if (!contains(pos)) {
            return this;
        }
        var newPositions = new ArrayList<>(positions);
        newPositions.remove(pos);
        return newPositions.isEmpty() ? EMPTY : new StructureWrenchSelection(Collections.unmodifiableList(newPositions));
    }
}
