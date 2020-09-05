package aztech.modern_industrialization.pipes.electricity;

import aztech.modern_industrialization.api.EnergyExtractable;
import aztech.modern_industrialization.api.EnergyInsertable;
import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;
import aztech.modern_industrialization.pipes.api.PipeNetworkNode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static alexiil.mc.lib.attributes.Simulation.ACTION;
import static alexiil.mc.lib.attributes.Simulation.SIMULATE;

public class ElectricityNetwork extends PipeNetwork {
    private final long maxEu;
    private static final int AMPS = 8;

    public ElectricityNetwork(int id, PipeNetworkData data, long maxEu) {
        super(id, data == null ? new ElectricityNetworkData() : data);
        this.maxEu = maxEu;
    }

    @Override
    public void tick(World world) {
        // Only tick once
        if(ticked) return;
        ticked = true;

        List<EnergyInsertable> insertables = new ArrayList<>();
        List<EnergyExtractable> extractables = new ArrayList<>();
        for(Map.Entry<BlockPos, PipeNetworkNode> entry : nodes.entrySet()) {
            if(entry.getValue() != null) {
                ElectricityNetworkNode node = (ElectricityNetworkNode) entry.getValue();
                node.appendAttributes(world, entry.getKey(), insertables, extractables);
            }
        }

        int remAmps = AMPS;
        outer_loop: for(EnergyExtractable extractable : extractables) {
            for(int i = 0; i < insertables.size();) {
                EnergyInsertable insertable = insertables.get(i);
                long ext = extractable.attemptPacketExtraction(SIMULATE);
                if(ext > maxEu) continue outer_loop;
                if(insertable.attemptPacketInsertion(ext, SIMULATE)) {
                    extractable.attemptPacketExtraction(ACTION);
                    insertable.attemptPacketInsertion(ext, ACTION);
                    --remAmps;
                    if(remAmps == 0) break outer_loop;;
                } else {
                    ++i;
                }
            }
        }
    }
}
