package aztech.modern_industrialization;

import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.armor.ClientKeyHandler;
import aztech.modern_industrialization.items.armor.HudRenderer;
import aztech.modern_industrialization.items.armor.JetpackParticleAdder;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineModel;
import aztech.modern_industrialization.machines.impl.MachinePackets;
import aztech.modern_industrialization.machines.impl.MachineScreen;
//import aztech.modern_industrialization.machines.impl.SteamBoilerScreen;
import aztech.modern_industrialization.model.block.ModelProvider;
import aztech.modern_industrialization.pipes.MIPipesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.UnbakedModel;
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
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        MITanks.setupClient();
        setupMachines();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> {
            return new ModelProvider();
        });
        (new MIPipesClient()).onInitializeClient();
        ClientKeyHandler.setup();
        ClientTickEvents.START_CLIENT_TICK.register(JetpackParticleAdder::addJetpackParticles);
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    private void setupScreens() {
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, MachineScreen::new);
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, ForgeHammerScreen::new);
    }

    private void setupPackets() {
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT, ConfigurableInventoryPacketHandlers.UPDATE_ITEM_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT, ConfigurableInventoryPacketHandlers.UPDATE_FLUID_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(MachinePackets.S2C.UPDATE_AUTO_EXTRACT, MachinePackets.S2C.ON_UPDATE_AUTO_EXTRACT);
        ClientSidePacketRegistry.INSTANCE.register(MachinePackets.S2C.SYNC_PROPERTY, MachinePackets.S2C.ON_SYNC_PROPERTY);
    }

    private void setupMachines() {
        for(MachineFactory factory : MachineFactory.getFactories()) {
            MachineModel model = factory.buildModel();

            ModelProvider.modelMap.put(new MIIdentifier("block/"+factory.getID()), model);
            ModelProvider.modelMap.put(new MIIdentifier("item/"+factory.getID()), model);
        }
    }
}
