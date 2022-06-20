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
package aztech.modern_industrialization.compat.waila;

import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkNode;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.util.NbtHelper;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.nbt.CompoundTag;

public class PipeDataProvider implements IServerDataProvider<PipeBlockEntity> {
    @Override
    public void appendServerData(CompoundTag data, IServerAccessor<PipeBlockEntity> accessor, IPluginConfig config) {
        for (PipeNetworkNode node : accessor.getTarget().getNodes()) {
            CompoundTag pipeData = new CompoundTag();

            if (node instanceof FluidNetworkNode) {
                FluidNetworkNode fluidNode = (FluidNetworkNode) node;
                pipeData.putLong("amount", fluidNode.getAmount());
                pipeData.putInt("capacity", fluidNode.getCapacity());
                NbtHelper.putFluid(pipeData, "fluid", fluidNode.getFluid());
            }

            if (node instanceof ElectricityNetworkNode) {
                ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) node;
                pipeData.putLong("eu", electricityNode.getEu());
                pipeData.putLong("maxEu", electricityNode.getMaxEu());
                pipeData.putString("tier", electricityNode.getTier().toString());
            }

            data.put(node.getType().getIdentifier().toString(), pipeData);
        }
    }
}
