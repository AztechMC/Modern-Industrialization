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
import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.blocks.storage.barrel.client.BarrelTooltipComponent;
import aztech.modern_industrialization.blocks.storage.barrel.client.CreativeBarrelClientSetup;
import aztech.modern_industrialization.blocks.storage.tank.creativetank.CreativeTankClientSetup;
import aztech.modern_industrialization.datagen.MIDatagenClient;
import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.inventory.ConfigurableInventoryS2CPacketHandlers;
import aztech.modern_industrialization.items.ConfigCardItem;
import aztech.modern_industrialization.items.RedstoneControlModuleItem;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.SteamDrillTooltipComponent;
import aztech.modern_industrialization.items.armor.ClientKeyHandler;
import aztech.modern_industrialization.items.armor.HudRenderer;
import aztech.modern_industrialization.items.armor.JetpackParticleAdder;
import aztech.modern_industrialization.items.client.ClientConfigCardTooltip;
import aztech.modern_industrialization.machines.ClientMachinePackets;
import aztech.modern_industrialization.machines.MachineOverlayClient;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.components.FuelBurningComponent;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.models.MachineRendering;
import aztech.modern_industrialization.machines.multiblocks.MultiblockErrorHighlight;
import aztech.modern_industrialization.misc.runtime_datagen.RuntimeDataGen;
import aztech.modern_industrialization.misc.version.VersionEvents;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.util.TextHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

public class ModernIndustrializationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MIStartup.onClientStartup();

        setupScreens();
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        CreativeTankClientSetup.setupClient();
        CreativeBarrelClientSetup.setupClient();
        MachineRendering.init();
        MultiblockMachines.clientInit();
        MultiblockErrorHighlight.init();
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(MachineOverlayClient::onBlockOutline);
        (new MIPipesClient()).setupClient();
        ClientKeyHandler.setup();
        WorldRenderEvents.START.register(renderer -> JetpackParticleAdder.addJetpackParticles(Minecraft.getInstance()));
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);
        setupTooltips();
        VersionEvents.init();
        setupItemPredicates();

        // Warn if neither JEI nor REI is present!
        if (!FabricLoader.getInstance().isModLoaded("emi") && !FabricLoader.getInstance().isModLoaded("jei")
                && !FabricLoader.getInstance().isModLoaded("roughlyenoughitems")) {
            ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
                if (MIConfig.getConfig().enableNoEmiMessage) {
                    client.player.displayClientMessage(MIText.NoEmi.text().withStyle(ChatFormatting.GOLD), false);
                }
            });
        }

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");

        if (MIConfig.getConfig().datagenOnStartup) {
            RuntimeDataGen.run(gen -> {
                MIDatagenClient.configure(gen, true);
                MIDatagenServer.configure(gen, true);
            });
        }
    }

    @SuppressWarnings({ "unchecked", "RedundantCast", "rawtypes" })
    private void setupScreens() {
        MenuScreens.register(
                (MenuType<? extends MachineMenuClient>) (MenuType) ModernIndustrialization.SCREEN_HANDLER_MACHINE,
                MachineScreen::new);
        MenuScreens.register(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, ForgeHammerScreen::new);
    }

    private void setupPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT,
                ConfigurableInventoryS2CPacketHandlers.UPDATE_ITEM_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT,
                ConfigurableInventoryS2CPacketHandlers.UPDATE_FLUID_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(MachinePackets.S2C.COMPONENT_SYNC, ClientMachinePackets.ON_COMPONENT_SYNC);
    }

    private void setupTooltips() {
        ItemTooltipCallback.EVENT.register(((stack, context, lines) -> {

            MITooltips.attachTooltip(stack, lines);

            Item item = stack.getItem();
            if (item != null) {
                // Apparently tooltips are accessed from the main menu, or something, hence the
                // != null check
                if (Minecraft.getInstance().level != null && !MIConfig.getConfig().disableFuelTooltips) {
                    try {
                        Integer fuelTime = FuelRegistryImpl.INSTANCE.get(item);
                        if (fuelTime != null && fuelTime > 0) {
                            long totalEu = fuelTime * FuelBurningComponent.EU_PER_BURN_TICK;
                            lines.add(new MITooltips.Line(MIText.BaseEuTotalStored).arg(totalEu, MITooltips.EU_PARSER).build());
                        }
                    } catch (Exception e) {
                        ModernIndustrialization.LOGGER.warn("Could not show MI fuel tooltip.", e);
                    }
                }

                if (context.isAdvanced() && !MIConfig.getConfig().disableItemTagTooltips) {
                    var ids = item.builtInRegistryHolder().tags().map(TagKey::location).sorted().toList();
                    for (ResourceLocation id : ids) {
                        lines.add(Component.literal("#" + id).setStyle(TextHelper.GRAY_TEXT));
                    }
                }
            }
        }));

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof BarrelTooltipData barrelData) {
                return new BarrelTooltipComponent(barrelData);
            } else if (data instanceof SteamDrillItem.SteamDrillTooltipData steamDrillData) {
                return new SteamDrillTooltipComponent(steamDrillData);
            } else if (data instanceof ConfigCardItem.TooltipData configCardData) {
                return new ClientConfigCardTooltip(configCardData);
            }
            return null;
        });
    }

    private void setupItemPredicates() {
        ItemProperties.register(MIItem.REDSTONE_CONTROL_MODULE.asItem(), new MIIdentifier("redstone_control_module"),
                (stack, level, entity, seed) -> {
                    return RedstoneControlModuleItem.isRequiresLowSignal(stack) ? 0 : 1;
                });
    }
}
