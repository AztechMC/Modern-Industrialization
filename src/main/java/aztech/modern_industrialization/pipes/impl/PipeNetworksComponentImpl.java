package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.pipes.api.PipeNetworkManager;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeNetworksComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class PipeNetworksComponentImpl implements PipeNetworksComponent {
    private Map<PipeNetworkType, PipeNetworkManager> managers = new HashMap<>();

    public PipeNetworksComponentImpl(World world) {
        for(PipeNetworkType type : PipeNetworkType.getTypes().values()) {
            managers.put(type, new PipeNetworkManager(type));
        }
    }

    @Override
    public PipeNetworkManager getManager(PipeNetworkType type) {
        return managers.get(type);
    }

    @Override
    public void onServerTickStart() {
        for(PipeNetworkManager manager : managers.values()) {
            manager.markNetworksAsUnticked();
        }
    }

    @Override
    public void fromTag(CompoundTag tag) {
        for(Map.Entry<Identifier, PipeNetworkType> entry : PipeNetworkType.getTypes().entrySet()) {
            PipeNetworkManager manager = new PipeNetworkManager(entry.getValue());
            String tagKey = entry.getKey().toString();
            if(tag.contains(tagKey)) {
                manager.fromTag(tag.getCompound(tagKey));
            }
            managers.put(entry.getValue(), manager);
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        for(Map.Entry<PipeNetworkType, PipeNetworkManager> entry : managers.entrySet()) {
            tag.put(entry.getKey().getIdentifier().toString(), entry.getValue().toTag(new CompoundTag()));
        }
        return tag;
    }
}
