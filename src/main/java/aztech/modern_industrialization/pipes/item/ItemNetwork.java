package aztech.modern_industrialization.pipes.item;

import aztech.modern_industrialization.pipes.api.PipeNetwork;
import aztech.modern_industrialization.pipes.api.PipeNetworkData;

public class ItemNetwork extends PipeNetwork {
    public ItemNetwork(int id, PipeNetworkData data) {
        super(id, data == null ? new ItemNetworkData() : data);
    }
}
