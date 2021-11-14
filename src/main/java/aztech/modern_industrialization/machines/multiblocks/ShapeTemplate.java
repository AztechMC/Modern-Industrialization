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
package aztech.modern_industrialization.machines.multiblocks;

import aztech.modern_industrialization.machines.models.MachineCasing;
import aztech.modern_industrialization.machines.models.MachineCasings;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * An immutable description of a multiblock shape.
 */
public class ShapeTemplate {
    public final Map<BlockPos, SimpleMember> simpleMembers = new HashMap<>();
    public final Map<BlockPos, HatchFlags> hatchFlags = new HashMap<>();
    public final MachineCasing hatchCasing;

    public ShapeTemplate(MachineCasing hatchCasing) {
        this.hatchCasing = hatchCasing;
    }

    public static class Builder {
        private final ShapeTemplate template;

        public Builder(MachineCasing hatchCasing) {
            template = new ShapeTemplate(hatchCasing);
        }

        public Builder add3by3Levels(int minY, int maxY, SimpleMember member, @Nullable HatchFlags flags) {
            for (int y = minY; y <= maxY; ++y) {
                add3by3(y, member, y != minY, (y == minY || y == maxY) ? flags : null);
            }

            return this;
        }

        public Builder add3by3LevelsRoofed(int minY, int maxY, SimpleMember member, @Nullable HatchFlags flags) {
            for (int y = minY; y <= maxY; ++y) {
                add3by3(y, member, y != minY && y != maxY, (y == minY || y == maxY) ? flags : null);
            }

            return this;
        }

        public Builder add(int x, int y, int z, SimpleMember member, @Nullable HatchFlags flags) {
            BlockPos pos = new BlockPos(x, y, z);
            template.simpleMembers.put(pos, member);
            if (flags != null) {
                template.hatchFlags.put(pos, flags);
            }
            return this;
        }

        public Builder add(int x, int y, int z, SimpleMember member) {
            return add(x, y, z, member, null);
        }

        public Builder add3by3(int y, SimpleMember member, boolean hollow, @Nullable HatchFlags flags) {
            for (int x = -1; x <= 1; x++) {
                for (int z = 0; z <= 2; z++) {
                    if (hollow && x == 0 && z == 1) {
                        continue;
                    }
                    add(x, y, z, member, flags);
                }
            }
            return this;
        }

        public Builder remove(int x, int y, int z) {
            BlockPos pos = new BlockPos(x, y, z);
            template.simpleMembers.remove(pos);
            template.hatchFlags.remove(pos);
            return this;
        }

        public ShapeTemplate build() {
            remove(0, 0, 0);
            return template;
        }
    }

    public static ShapeTemplate computeDummyUnion(ShapeTemplate[] templates) {
        Builder builder = new ShapeTemplate.Builder(MachineCasings.BRICKS);

        for (ShapeTemplate template : templates) {
            for (BlockPos pos : template.simpleMembers.keySet()) {
                builder.add(pos.getX(), pos.getY(), pos.getZ(), SimpleMember.forBlock(Blocks.BARREL), null);
            }
        }

        return builder.build();
    }
}
