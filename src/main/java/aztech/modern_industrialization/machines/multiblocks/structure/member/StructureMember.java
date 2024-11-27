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
package aztech.modern_industrialization.machines.multiblocks.structure.member;

import aztech.modern_industrialization.blocks.FastBlockEntity;
import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.multiblocks.HatchFlags;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import aztech.modern_industrialization.machines.multiblocks.structure.member.test.StructureMemberTest;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;

public sealed abstract class StructureMember implements SimpleMember permits SimpleStructureMember, VariableStructureMember {
    public static final Codec<StructureMember> CODEC = Codec.STRING.flatComapMap(
            StructureMemberType::getType, type -> DataResult.success(type.name()))
            .dispatch(member -> (StructureMemberType) member.type(), StructureMemberType::codec);

    public abstract StructureMemberType<?> type();

    public abstract Optional<Pair<BlockState, FastBlockEntity>> asStructureBlock(BlockPos pos, boolean required);

    public static SimpleStructureMember simple(Supplier<BlockState> preview, List<StructureMemberTest> tests) {
        return new SimpleStructureMember(Lazy.of(preview), tests);
    }

    public static LiteralStructureMember literal(Supplier<BlockState> blockState) {
        return new LiteralStructureMember(Lazy.of(blockState));
    }

    public static HatchStructureMember hatch(Supplier<BlockState> preview, List<StructureMemberTest> tests, MachineCasing casing,
            HatchFlags hatchFlags) {
        return new HatchStructureMember(Lazy.of(preview), tests, casing, hatchFlags);
    }

    public static VariableStructureMember variable(String name) {
        return new VariableStructureMember(name);
    }
}
