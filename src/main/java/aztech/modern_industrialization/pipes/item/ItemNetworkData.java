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

package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

// There is no data for item networks, two pipes of the same type can always connect.
public class ItemNetworkData extends PipeNetworkData {
    @Override
    public PipeNetworkData clone() {
        return new ItemNetworkData();
    }

    @Override
    public void fromTag(CompoundTag tag, HolderLookup.Provider registries) {}

    @Override
    public CompoundTag toTag(CompoundTag tag, HolderLookup.Provider registries) {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemNetworkData;
    }
}
