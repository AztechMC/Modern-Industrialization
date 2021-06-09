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
import mcp.mobius.waila.api.IServerDataProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class PipeDataProvider implements IServerDataProvider<BlockEntity> {
    @Override
    public void appendServerData(NbtCompound data, ServerPlayerEntity player, World world, BlockEntity be) {
        PipeBlockEntity pipe = (PipeBlockEntity) be;

        for (PipeNetworkNode node : pipe.getNodes()) {
            NbtCompound pipeData = new NbtCompound();

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
