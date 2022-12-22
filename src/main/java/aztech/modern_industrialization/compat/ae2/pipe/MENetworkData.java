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
package aztech.modern_industrialization.compat.ae2.pipe;

import appeng.api.networking.*;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import net.minecraft.nbt.CompoundTag;

public class MENetworkData extends PipeNetworkData {

    private final IManagedGridNode mainNode;

    public MENetworkData() {
        this.mainNode = GridHelper.createManagedNode(this, new IGridNodeListener<>() {
            @Override
            public void onSecurityBreak(MENetworkData nodeOwner, IGridNode node) {
                throw new UnsupportedOperationException("How did we get here?");
            }

            @Override
            public void onSaveChanges(MENetworkData nodeOwner, IGridNode node) {
            }
        })
                .setFlags(GridFlags.PREFERRED)
                .setIdlePowerUsage(0.0);
    }

    public IManagedGridNode getMainNode() {
        return mainNode;
    }

    @Override
    public PipeNetworkData clone() {
        return new MENetworkData();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        this.getMainNode().loadFromNBT(tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        this.getMainNode().saveToNBT(tag);
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MENetworkData;
    }
}
