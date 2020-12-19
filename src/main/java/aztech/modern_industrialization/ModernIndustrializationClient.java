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
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineRenderer;
import aztech.modern_industrialization.model.block.ModelProvider;
import aztech.modern_industrialization.pipes.MIPipesClient;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.DependencyException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.metadata.ParseMetadataException;
import net.minecraft.util.Identifier;

public class ModernIndustrializationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupScreens();
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        MITanks.setupClient();
        setupMachines();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new ModelProvider());
        (new MIPipesClient()).onInitializeClient();
        ClientKeyHandler.setup();
        ClientTickEvents.START_CLIENT_TICK.register(JetpackParticleAdder::addJetpackParticles);
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);
        registerBuiltinResourcePack();

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    private void setupScreens() {
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_TYPE_MACHINE, MachineScreen::new);
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, ForgeHammerScreen::new);
    }

    private void setupPackets() {
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT,
                ConfigurableInventoryPacketHandlers.UPDATE_ITEM_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT,
                ConfigurableInventoryPacketHandlers.UPDATE_FLUID_SLOT);
        ClientSidePacketRegistry.INSTANCE.register(MachinePackets.S2C.UPDATE_AUTO_EXTRACT, MachinePackets.S2C.ON_UPDATE_AUTO_EXTRACT);
        ClientSidePacketRegistry.INSTANCE.register(MachinePackets.S2C.SYNC_PROPERTY, MachinePackets.S2C.ON_SYNC_PROPERTY);
    }

    @SuppressWarnings("unchecked")
    private void setupMachines() {
        for (MachineFactory factory : MachineFactory.getFactories()) {
            MachineModel model = factory.buildModel();

            ModelProvider.modelMap.put(new MIIdentifier("block/" + factory.getID()), model);
            ModelProvider.modelMap.put(new MIIdentifier("item/" + factory.getID()), model);

            if (factory.isMultiblock()) {
                BlockEntityRendererRegistry.INSTANCE.register(factory.blockEntityType, MultiblockMachineRenderer::new);
            }
        }
    }

    private void registerBuiltinResourcePack() {
        if (!ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(ModernIndustrialization.MOD_ID, "alternate"), "alternate", FabricLoader.getInstance().getModContainer(ModernIndustrialization.MOD_ID).orElseThrow(DependencyException::new), false)) {
            ModernIndustrialization.LOGGER.warn("Modern Industrialization's Alternate Builtin Resource Pack couldn't be registered! This is probably bad!");
        }
    }
}
