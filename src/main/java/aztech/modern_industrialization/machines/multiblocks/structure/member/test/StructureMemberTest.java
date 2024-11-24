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

import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;

public abstract class StructureMemberTest {
    public abstract String typeId();

    public abstract boolean matchesState(BlockState state);

    public abstract boolean isLoaded();

    protected final void assertLoaded() {
        if (!isLoaded()) {
            throw new IllegalStateException("Member test is not loaded");
        }
    }

    public void load(CompoundTag tag) {
        Objects.requireNonNull(tag);
    }

    public void save(CompoundTag tag) {
        Objects.requireNonNull(tag);
        assertLoaded();
    }

    public static StructureMemberTest from(CompoundTag tag) {
        Objects.requireNonNull(tag);
        if (!tag.contains("type", Tag.TAG_STRING)) {
            throw new IllegalArgumentException("Invalid structure member test format: " + tag);
        }
        String type = tag.getString("type");
        StructureMemberTest test = switch (type) {
        case "tag" -> new TagStructureMemberTest();
        case "state" -> new StateStructureMemberTest();
        default -> throw new IllegalStateException("Unexpected type: " + type);
        };
        test.load(tag);
        return test;
    }
}
