package aztech.modern_industrialization;

import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.machines.init.SingleBlockSpecialMachines;
import aztech.modern_industrialization.network.MIPackets;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MI.ID)
public class MI {
    public static final String ID = "modern_industrialization";
    public static final Logger LOGGER = LoggerFactory.getLogger("Modern Industrialization");

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public MI(IEventBus modBus) {
        MIPackets.init();

        MIFluids.init(modBus);
        MIBlock.init(modBus);
        MIItem.init(modBus);
        MIRegistries.init(modBus);

        SingleBlockSpecialMachines.init();

        modBus.addListener(GatherDataEvent.class, event -> {
            MIDatagenServer.configure(event.getGenerator(), event.includeServer(), false);
        });
    }
}
