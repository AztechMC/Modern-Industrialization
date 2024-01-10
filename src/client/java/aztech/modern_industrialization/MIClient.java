package aztech.modern_industrialization;

import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.blocks.storage.barrel.client.BarrelTooltipComponent;
import aztech.modern_industrialization.datagen.MIDatagenClient;
import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.datagen.model.DelegatingModelBuilder;
import aztech.modern_industrialization.datagen.model.MachineModelsToGenerate;
import aztech.modern_industrialization.items.ConfigCardItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.SteamDrillTooltipComponent;
import aztech.modern_industrialization.items.client.ClientConfigCardTooltip;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.machines.gui.ClientComponentRenderer;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.models.MachineCasingHolderModel;
import aztech.modern_industrialization.machines.models.MachineUnbakedModel;
import aztech.modern_industrialization.materials.MaterialRegistry;
import aztech.modern_industrialization.materials.part.MIParts;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.impl.DelegatingUnbakedModel;
import aztech.modern_industrialization.pipes.impl.PipeUnbakedModel;
import aztech.modern_industrialization.pipes.item.ItemPipeScreen;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MI.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MIClient {
    @SubscribeEvent
    private static void init(FMLConstructModEvent ignored) {
        MIPipesClient.setupClient();

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

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parentScreen) -> AutoConfig.getConfigScreen(MIConfig.class, parentScreen).get()));
    }

    @SubscribeEvent
    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register((MenuType<MachineMenuClient>) MIRegistries.MACHINE_MENU.get(), MachineScreen::new);

            MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_ITEM_PIPE.get(), ItemPipeScreen::new);
//        MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE, FluidPipeScreen::new);
        });
    }

    @SubscribeEvent
    private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(DelegatingModelBuilder.LOADER_ID, DelegatingUnbakedModel.LOADER);
        event.register(MachineCasingHolderModel.LOADER_ID, MachineCasingHolderModel.LOADER);
        event.register(MachineUnbakedModel.LOADER_ID, MachineUnbakedModel.LOADER);
        event.register(PipeUnbakedModel.LOADER_ID, PipeUnbakedModel.LOADER);
    }

    @SubscribeEvent
    private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(MachineCasingHolderModel.MODEL_ID);
    }

    private static final List<Runnable> blockEntityRendererRegistrations = new ArrayList<>();

    public static <T extends BlockEntity, U extends T> void registerBlockEntityRenderer(Supplier<BlockEntityType<U>> bet, BlockEntityRendererProvider<T> renderer) {
        blockEntityRendererRegistrations.add(() -> BlockEntityRenderers.register(bet.get(), renderer));
    }

    @SubscribeEvent
    private static void registerBlockEntityRenderers(FMLClientSetupEvent event) {
        for (var blockDef : MIBlock.BLOCKS.getEntries()) {
            if (blockDef.get() instanceof MachineBlock machine) {
                var blockEntity = machine.getBlockEntityInstance();
                BlockEntityType type = blockEntity.getType();

                // TODO NEO multiblock BERs
//                if (blockEntity instanceof LargeTankMultiblockBlockEntity) {
//                    BlockEntityRenderers.register(type, MultiblockTankBER::new);
//                } else if (blockEntity instanceof MultiblockMachineBlockEntity) {
//                    BlockEntityRenderers.register(type, MultiblockMachineBER::new);
//                } else {
                    BlockEntityRenderers.register(type, c -> new MachineBlockEntityRenderer(c));
//                }
            }
        }

        blockEntityRendererRegistrations.forEach(Runnable::run);
    }

    @SubscribeEvent
    private static void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BarrelTooltipData.class, BarrelTooltipComponent::new);
        event.register(ConfigCardItem.TooltipData.class, ClientConfigCardTooltip::new);
        event.register(SteamDrillItem.SteamDrillTooltipData.class, SteamDrillTooltipComponent::new);
    }
}
