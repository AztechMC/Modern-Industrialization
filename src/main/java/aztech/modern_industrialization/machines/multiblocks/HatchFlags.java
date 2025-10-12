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

import java.util.HashSet;
import java.util.Set;

public class HatchFlags {
    public static final HatchFlags NO_HATCH = new Builder().build();

    private final Set<HatchType> allowed;

    public HatchFlags(Set<HatchType> allowed) {
        this.allowed = Set.copyOf(allowed);
    }

    public boolean allows(HatchType type) {
        return allowed.contains(type);
    }

    public Set<HatchType> values() {
        return allowed;
    }

    public static class Builder {
        private final Set<HatchType> allowed = new HashSet<>();

        public Builder with(HatchType type) {
            allowed.add(type);
            return this;
        }

        public Builder with(HatchType... types) {
            for (HatchType type : types) {
                with(type);
            }

            return this;
        }

        public HatchFlags build() {
            return new HatchFlags(allowed);
        }
    }
}
