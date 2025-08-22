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
package aztech.modern_industrialization.machines.multiblocks.shape.member;

import aztech.modern_industrialization.machines.multiblocks.shape.test.MultiblockMemberTest;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleMultiblockMember extends MultiblockMember {
    protected final MultiblockMemberState preview;
    protected final List<MultiblockMemberTest> tests;

    public SimpleMultiblockMember(MultiblockMemberState preview, List<MultiblockMemberTest> tests) {
        Objects.requireNonNull(preview);
        Objects.requireNonNull(tests);
        this.preview = preview;
        this.tests = Collections.unmodifiableList(tests);
    }

    public MultiblockMemberState preview() {
        return preview;
    }

    public List<MultiblockMemberTest> tests() {
        return tests;
    }

    @Override
    public void forceLoad() {
        preview.state();
        for (var test : tests) {
            test.forceLoad();
        }
    }

    @Override
    public boolean matchesState(BlockState state) {
        for (var test : tests) {
            if (test.matchesState(state)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MultiblockMemberState getPreviewState() {
        return preview;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof SimpleMultiblockMember other && this.getClass() == other.getClass()) {
            return preview.equals(other.preview) &&
            // Ensures the lists are equal ignoring order
                    tests.containsAll(other.tests) && other.tests.containsAll(tests);
        }
        return false;
    }
}
