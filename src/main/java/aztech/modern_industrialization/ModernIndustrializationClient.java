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

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.tank.CreativeTankSetup;
import aztech.modern_industrialization.debug.MissingTranslationsCommand;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.armor.ClientKeyHandler;
import aztech.modern_industrialization.items.armor.HudRenderer;
import aztech.modern_industrialization.items.armor.JetpackParticleAdder;
import aztech.modern_industrialization.machines.ClientMachinePackets;
import aztech.modern_industrialization.machines.MachineOverlay;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.blockentities.multiblocks.ElectricBlastFurnaceBlockEntity;
import aztech.modern_industrialization.machines.components.FuelBurningComponent;
import aztech.modern_industrialization.machines.components.LubricantHelper;
import aztech.modern_industrialization.machines.components.UpgradeComponent;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.models.MachineModels;
import aztech.modern_industrialization.machines.multiblocks.MultiblockErrorHighlight;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.proxy.ClientProxy;
import aztech.modern_industrialization.util.TextHelper;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.fabricmc.loader.DependencyException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ModernIndustrializationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientProxy.set();

        setupScreens();
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        CreativeTankSetup.setupClient();
        MachineModels.init();
        MultiblockMachines.clientInit();
        MultiblockErrorHighlight.init();
        WorldRenderEvents.BLOCK_OUTLINE.register(MachineOverlay::onBlockOutline);
        (new MIPipesClient()).setupClient();
        ClientKeyHandler.setup();
        WorldRenderEvents.START.register(renderer -> JetpackParticleAdder.addJetpackParticles(MinecraftClient.getInstance()));
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);
        registerBuiltinResourcePack();
        setupTooltips();
        setupClientCommands();

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    @SuppressWarnings({ "unchecked", "RedundantCast", "rawtypes" })
    private void setupScreens() {
        ScreenRegistry.register(
                (ScreenHandlerType<? extends MachineScreenHandlers.Client>) (ScreenHandlerType) ModernIndustrialization.SCREEN_HANDLER_MACHINE,
                MachineScreenHandlers.ClientScreen::new);
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_FORGE_HAMMER, ForgeHammerScreen::new);
    }

    private void setupPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_ITEM_SLOT,
                ConfigurableInventoryPacketHandlers.S2C.UPDATE_ITEM_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.UPDATE_FLUID_SLOT,
                ConfigurableInventoryPacketHandlers.S2C.UPDATE_FLUID_SLOT);
        ClientPlayNetworking.registerGlobalReceiver(MachinePackets.S2C.COMPONENT_SYNC, ClientMachinePackets.ON_COMPONENT_SYNC);
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
            SpeedUpgrade upgrade = SpeedUpgrade.LOOKUP.find(stack, null);
            if (upgrade != null) {
                lines.add(new TranslatableText("text.modern_industrialization.tooltip_speed_upgrade", upgrade.value())
                        .setStyle(TextHelper.UPGRADE_TEXT));
            }

            Item item = stack.getItem();

            if (item != null) {
                if (item instanceof PipeItem) {
                    PipeItem pipe = (PipeItem) item;
                    if (MIPipes.electricityPipeTier.containsKey(pipe)) {
                        CableTier tier = MIPipes.electricityPipeTier.get(pipe);
                        lines.add(new TranslatableText("text.modern_industrialization.eu_cable", new TranslatableText(tier.translationKey),
                                tier.getMaxTransfer()).setStyle(TextHelper.EU_TEXT));
                    }
                }
                if (item == Items.GUNPOWDER) {
                    lines.add(new TranslatableText("text.modern_industrialization.gunpowder_upgrade").setStyle(TextHelper.GRAY_TEXT));
                }
                if (item == MIFluids.LUBRICANT.bucketItem) {
                    lines.add(new TranslatableText("text.modern_industrialization.lubricant_tooltip", LubricantHelper.mbPerTick)
                            .setStyle(TextHelper.GRAY_TEXT));
                }
                if (UpgradeComponent.upgrades.containsKey(item)) {
                    lines.add(new TranslatableText("text.modern_industrialization.machine_upgrade", UpgradeComponent.upgrades.get(item))
                            .setStyle(TextHelper.UPGRADE_TEXT));
                }
                if (item instanceof BlockItem) {
                    Block block = ((BlockItem) item).getBlock();
                    if (ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.containsKey(block)) {
                        lines.add(new TranslatableText("text.modern_industrialization.ebf_max_eu",
                                ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.get(block)).setStyle(TextHelper.UPGRADE_TEXT));
                    }
                }

                // Apparently tooltips are accessed from the main menu, or something, hence the
                // != null check
                if (MinecraftClient.getInstance().world != null && !MIConfig.getConfig().disableFuelTooltips) {
                    try {
                        Integer fuelTime = FuelRegistryImpl.INSTANCE.get(item);
                        if (fuelTime != null && fuelTime > 0) {
                            long totalEu = fuelTime * FuelBurningComponent.EU_PER_BURN_TICK;
                            lines.add(new TranslatableText("text.modern_industrialization.base_eu_total_double", TextHelper.getEuString(totalEu),
                                    TextHelper.getEuUnit(totalEu)).setStyle(TextHelper.GRAY_TEXT));
                        }
                    } catch (Exception e) {
                        ModernIndustrialization.LOGGER.warn("Could not show MI fuel tooltip.", e);
                    }
                }

                if (context.isAdvanced() && !MIConfig.getConfig().disableItemTagTooltips) {
                    List<Identifier> ids = (List<Identifier>) ItemTags.getTagGroup().getTagsFor(item);
                    Collections.sort(ids);
                    for (Identifier id : ids) {
                        lines.add(new LiteralText("#" + id).setStyle(TextHelper.GRAY_TEXT));
                    }
                }
            }
        }));
    }

    private void setupClientCommands() {
        ClientCommandManager.DISPATCHER.register(literal("miclient")//
                .then(literal("dump_missing_translations").executes(MissingTranslationsCommand::run))//
        );
    }
}
