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
package aztech.modern_industrialization.pipes.fluid;

import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.util.NbtHelper;
import net.minecraft.nbt.CompoundTag;

public class FluidNetworkData extends PipeNetworkData {
    FluidVariant fluid;

    public FluidNetworkData(FluidVariant fluid) {
        this.fluid = fluid;
    }

    @Override
    public FluidNetworkData clone() {
        return new FluidNetworkData(fluid);
    }

    @Override
    public void fromTag(CompoundTag tag) {
        fluid = NbtHelper.getFluidCompatible(tag, "fluid");
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        NbtHelper.putFluid(tag, "fluid", fluid);
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FluidNetworkData) {
            FluidNetworkData otherData = (FluidNetworkData) obj;
            return otherData.fluid == fluid;
        } else {
            return false;
        }
    }
}
