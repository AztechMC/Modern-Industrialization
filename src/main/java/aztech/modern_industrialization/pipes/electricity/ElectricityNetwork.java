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
package aztech.modern_industrialization.pipes.electricity;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.energy.EnergyExtractable;
import aztech.modern_industrialization.api.energy.EnergyInsertable;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElectricityNetwork extends PipeNetwork {
    final CableTier tier;

    public ElectricityNetwork(int id, PipeNetworkData data, CableTier tier) {
        super(id, data == null ? new ElectricityNetworkData() : data);
        this.tier = tier;
    }

    @Override
    public void tick(World world) {
        // Only tick once
        if (ticked)
            return;
        ticked = true;

        List<EnergyInsertable> insertables = new ArrayList<>();
        List<EnergyExtractable> extractables = new ArrayList<>();
        long networkAmount = 0;
        long remainingInsert = 0;
        int loadedNodes = 0;
        for (Map.Entry<BlockPos, PipeNetworkNode> entry : nodes.entrySet()) {
            if (entry.getValue() != null) {
                ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getValue();
                node.appendAttributes(world, entry.getKey(), insertables, extractables);
                networkAmount += node.eu;
                remainingInsert += tier.getMaxInsert() - node.eu;
                loadedNodes++;
            }
        }
        remainingInsert = Math.min(remainingInsert, tier.getMaxInsert());

        for (EnergyExtractable extractable : extractables) {
            long ext = extractable.extractEnergy(remainingInsert);
            remainingInsert -= ext;
            networkAmount += ext;
        }

        for (EnergyInsertable insertable : insertables) {
            if (insertable.canInsert(tier)) {
                networkAmount = insertable.insertEnergy(networkAmount);
            }
        }

        for (PipeNetworkNode node : nodes.values()) {
            if (node != null) {
                ElectricityNetworkNode electricityNode = (ElectricityNetworkNode) node;
                electricityNode.eu = networkAmount / loadedNodes;
                networkAmount -= electricityNode.eu;
                --loadedNodes;
            }
        }
    }
}
