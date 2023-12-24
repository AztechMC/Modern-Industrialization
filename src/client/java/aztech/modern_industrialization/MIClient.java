package aztech.modern_industrialization;

import aztech.modern_industrialization.datagen.MIDatagenClient;
import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.datagen.model.MachineModelsToGenerate;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.models.MachineCasingHolderModel;
import aztech.modern_industrialization.machines.models.MachineUnbakedModel;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Objects;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MI.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MIClient {
    @SubscribeEvent
    public static void init(FMLConstructModEvent ignored) {
        var modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        Objects.requireNonNull(modBus);

        modBus.addListener(GatherDataEvent.class, event -> {
            MIDatagenClient.configure(
                    event.getGenerator(),
                    event.getExistingFileHelper(),
                    event.getLookupProvider(),
                    event.includeServer(),
                    false);
        });
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register((MenuType<MachineMenuClient>) MIRegistries.MACHINE_MENU.get(), MachineScreen::new);
        });
    }

    @SubscribeEvent
    public static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MachineCasingHolderModel.LOADER_ID, MachineCasingHolderModel.LOADER);
        event.register(MachineUnbakedModel.LOADER_ID, MachineUnbakedModel.LOADER);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(MachineCasingHolderModel.MODEL_ID);
    }
}