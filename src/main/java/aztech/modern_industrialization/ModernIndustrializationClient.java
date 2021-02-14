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

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.armor.ClientKeyHandler;
import aztech.modern_industrialization.items.armor.HudRenderer;
import aztech.modern_industrialization.items.armor.JetpackParticleAdder;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachineModel;
import aztech.modern_industrialization.machines.impl.multiblock.MultiblockMachineRenderer;
import aztech.modern_industrialization.machinesv2.MachineOverlay;
import aztech.modern_industrialization.machinesv2.MachinePackets;
import aztech.modern_industrialization.machinesv2.MachineScreenHandlers;
import aztech.modern_industrialization.machinesv2.models.MachineModels;
import aztech.modern_industrialization.model.block.ModelProvider;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.util.TextHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.lookup.v1.item.ItemKey;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.DependencyException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ModernIndustrializationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupScreens();
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        MITanks.setupClient();
        MachineModels.init();
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(new MachineOverlay.Renderer());
        (new MIPipesClient()).onInitializeClient();
        ClientKeyHandler.setup();
        ClientTickEvents.START_CLIENT_TICK.register(JetpackParticleAdder::addJetpackParticles);
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);
        registerBuiltinResourcePack();
        setupTooltips();

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    @SuppressWarnings({"unchecked", "RedundantCast", "rawtypes"})
    private void setupScreens() {
        ScreenRegistry.register((ScreenHandlerType<? extends MachineScreenHandlers.Client>) (ScreenHandlerType) ModernIndustrialization.SCREEN_HANDLER_MACHINE, MachineScreenHandlers.ClientScreen::new);
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, ForgeHammerScreen::new);
    }

    private void setupPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT,
                ConfigurableInventoryPacketHandlers.S2C.UPDATE_ITEM_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT,
                ConfigurableInventoryPacketHandlers.S2C.UPDATE_FLUID_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(MachinePackets.S2C.COMPONENT_SYNC, MachinePackets.S2C.ON_COMPONENT_SYNC);
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
        if (!ResourceManagerHelper.registerBuiltinResourcePack(new Identifier(ModernIndustrialization.MOD_ID, "alternate"), "alternate",
                FabricLoader.getInstance().getModContainer(ModernIndustrialization.MOD_ID).orElseThrow(DependencyException::new), false)) {
            ModernIndustrialization.LOGGER
                    .warn("Modern Industrialization's Alternate Builtin Resource Pack couldn't be registered! This is probably bad!");
        }
    }

    private void setupTooltips() {
        ItemTooltipCallback.EVENT.register(((stack, context, lines) -> {
            SpeedUpgrade upgrade = SpeedUpgrade.LOOKUP.get(ItemKey.of(stack), null);
            if (upgrade != null) {
                lines.add(new TranslatableText("text.modern_industrialization.tooltip_speed_upgrade", upgrade.value())
                        .setStyle(TextHelper.UPGRADE_TEXT));
            }

            if (stack.getItem() instanceof PipeItem) {
                PipeItem pipe = (PipeItem) stack.getItem();
                if (MIPipes.electricityPipeTier.containsKey(pipe)) {
                    CableTier tier = MIPipes.electricityPipeTier.get(pipe);
                    lines.add(new TranslatableText("text.modern_industrialization.eu_cable", new TranslatableText(tier.translationKey),
                            tier.getMaxInsert()).setStyle(TextHelper.EU_TEXT));
                }
            }
        }));
    }
}
