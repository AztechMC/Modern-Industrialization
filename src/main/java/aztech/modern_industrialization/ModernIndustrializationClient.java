package aztech.modern_industrialization;

import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineModel;
import aztech.modern_industrialization.machines.impl.MachinePackets;
import aztech.modern_industrialization.machines.impl.MachineScreen;
//import aztech.modern_industrialization.machines.impl.SteamBoilerScreen;
import aztech.modern_industrialization.model.block.ModelProvider;
import aztech.modern_industrialization.pipes.MIPipesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import java.util.function.BiConsumer;

public class ModernIndustrializationClient implements ClientModInitializer {
    public static final String MOD_ID = ModernIndustrialization.MOD_ID;

    @Override
    public void onInitializeClient() {
        setupScreens();
        setupFluidRenders();
        setupPackets();
        setupMachines();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> {
            return new ModelProvider();
        });
        (new MIPipesClient()).onInitializeClient();

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    private void setupScreens() {
        //ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_TYPE_STEAM_BOILER, SteamBoilerScreen::new);
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, MachineScreen::new);
    }

    private void setupFluidRenders() {
        final Identifier[] waterSpriteIds = new Identifier[] { new Identifier("minecraft:block/water_still"), new Identifier("minecraft:block/water_flow") };
        final Sprite[] waterSprites = new Sprite[2];

        final Identifier listenerId = new MIIdentifier("waterlike_reload_listener");
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return listenerId;
            }

            @Override
            public void apply(ResourceManager manager) {
                for(int i = 0; i < 2; ++i) {
                    waterSprites[i] = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEX).apply(waterSpriteIds[i]);
                }
            }
        });

        BiConsumer<Fluid, Integer> registerWaterlikeFluid = (fluid, color) -> {
            FluidRenderHandlerRegistry.INSTANCE.register(fluid, new FluidRenderHandler() {
                @Override
                public Sprite[] getFluidSprites(BlockRenderView blockRenderView, BlockPos blockPos, FluidState fluidState) {
                    return waterSprites;
                }

                @Override
                public int getFluidColor(BlockRenderView view, BlockPos pos, FluidState state) {
                    return color;
                }
            });
        };

        registerWaterlikeFluid.accept(ModernIndustrialization.FLUID_STEAM, -1);
    }

    private void setupPackets() {
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, ConfigurableInventoryPacketHandlers.UPDATE_ITEM_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT, ConfigurableInventoryPacketHandlers.UPDATE_FLUID_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(MachinePackets.S2C.UPDATE_AUTO_EXTRACT, MachinePackets.S2C.ON_UPDATE_AUTO_EXTRACT);
    }

    private void setupMachines() {
        for(MachineFactory factory : MachineFactory.getFactories()) {
            MachineModel model = factory.buildModel();

            ModelProvider.modelMap.put(new MIIdentifier("block/"+factory.getID()), model);
            ModelProvider.modelMap.put(new MIIdentifier("item/"+factory.getID()), model);
        }
    }
}
