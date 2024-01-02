package aztech.modern_industrialization;

import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MultiblockHatches;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines;
import aztech.modern_industrialization.machines.init.SingleBlockSpecialMachines;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.network.MIPackets;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
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
        MIMaterials.init();

        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();

        MIPipes.INSTANCE.setup();

        CommonProxy.initEvents();
        ChunkEventListeners.init();

        modBus.addListener(GatherDataEvent.class, event -> {
            MIDatagenServer.configure(
                    event.getGenerator(),
                    event.getExistingFileHelper(),
                    event.getLookupProvider(),
                    event.includeServer(),
                    false);
        });

        modBus.addListener(RegisterCapabilitiesEvent.class, MICapabilities::init);
    }
}
