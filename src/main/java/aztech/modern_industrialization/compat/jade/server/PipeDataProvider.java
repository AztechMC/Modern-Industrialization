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
package aztech.modern_industrialization.compat.jade.server;

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkNode;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public class PipeDataProvider implements IServerDataProvider<BlockAccessor> {
    @Override
    public ResourceLocation getUid() {
        return MI.id("pipe");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        var be = (PipeBlockEntity) accessor.getBlockEntity();

        for (PipeNetworkNode node : be.getNodes()) {
            CompoundTag pipeData = new CompoundTag();

            if (node instanceof FluidNetworkNode fluidNode) {
                var info = fluidNode.collectNetworkInfo();
                pipeData.put("fluid", info.fluid().toNbt());
                pipeData.putLong("amount", info.stored());
                pipeData.putLong("capacity", info.capacity());
                pipeData.putLong("transfer", info.transfer());
                pipeData.putLong("maxTransfer", info.maxTransfer());
            }

            if (node instanceof ElectricityNetworkNode electricityNode) {
                var info = electricityNode.collectNetworkInfo();
                pipeData.putLong("eu", info.stored());
                pipeData.putLong("maxEu", info.capacity());
                pipeData.putLong("transfer", info.transfer());
                pipeData.putLong("maxTransfer", info.maxTransfer());
            }

            if (node instanceof ItemNetworkNode itemNode) {
                var info = itemNode.collectNetworkInfo();
                pipeData.putLong("items", info.movedItems());
                pipeData.putInt("pulse", info.pulse());
            }

            data.put(node.getType().getIdentifier().toString(), pipeData);
        }
    }
}
