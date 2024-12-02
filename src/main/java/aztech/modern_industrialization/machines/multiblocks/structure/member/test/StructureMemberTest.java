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
package aztech.modern_industrialization.machines.multiblocks.structure.member.test;

import aztech.modern_industrialization.machines.multiblocks.structure.StructureNBTMode;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public sealed abstract class StructureMemberTest permits StateStructureMemberTest, TagStructureMemberTest {
    public static final Codec<StructureMemberTest> CODEC = Codec.STRING.flatComapMap(
            StructureMemberTestType::getType, type -> DataResult.success(type.name()))
            .dispatch(test -> (StructureMemberTestType) test.type(), StructureMemberTestType::codec);

    public abstract StructureMemberTestType<?> type();

    public abstract boolean matchesState(BlockState state, @Nullable BlockEntity blockEntity, StructureNBTMode mode);
}
