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
import aztech.modern_industrialization.blocks.OreBlock;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreen;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.blocks.storage.barrel.client.BarrelTooltipComponent;
import aztech.modern_industrialization.blocks.storage.tank.CreativeTankClientSetup;
import aztech.modern_industrialization.client.model.MachineModelLoader;
import aztech.modern_industrialization.debug.MissingTranslationsCommand;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.SteamDrillItem;
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
import aztech.modern_industrialization.misc.tooltips.FaqTooltips;
import aztech.modern_industrialization.misc.version.VersionEvents;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.impl.PipeItem;
import aztech.modern_industrialization.util.TextHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ModernIndustrializationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        setupScreens();
        MIFluidsRender.setupFluidRenders();
        setupPackets();
        CreativeTankClientSetup.setupClient();
        MachineModels.init();
        MachineModelLoader.init();
        MultiblockMachines.clientInit();
        MultiblockErrorHighlight.init();
        WorldRenderEvents.BLOCK_OUTLINE.register(MachineOverlay::onBlockOutline);
        (new MIPipesClient()).setupClient();
        ClientKeyHandler.setup();
        WorldRenderEvents.START.register(renderer -> JetpackParticleAdder.addJetpackParticles(Minecraft.getInstance()));
        ClientTickEvents.END_CLIENT_TICK.register(ClientKeyHandler::onEndTick);
        HudRenderCallback.EVENT.register(HudRenderer::onRenderHud);
        setupTooltips();
        setupClientCommands();
        VersionEvents.init();
        FaqTooltips.init();

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    @SuppressWarnings({ "unchecked", "RedundantCast", "rawtypes" })
    private void setupScreens() {
        ScreenRegistry.register(
                (MenuType<? extends MachineScreenHandlers.Client>) (MenuType) ModernIndustrialization.SCREEN_HANDLER_MACHINE,
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

    private void setupTooltips() {
        ItemTooltipCallback.EVENT.register(((stack, context, lines) -> {
            SpeedUpgrade upgrade = SpeedUpgrade.LOOKUP.find(stack, null);
            if (upgrade != null) {
                lines.add(new TranslatableComponent("text.modern_industrialization.tooltip_speed_upgrade", upgrade.value())
                        .setStyle(TextHelper.UPGRADE_TEXT));
            }

            Item item = stack.getItem();

            if (item != null) {
                if (item instanceof PipeItem) {
                    PipeItem pipe = (PipeItem) item;
                    if (MIPipes.electricityPipeTier.containsKey(pipe)) {
                        CableTier tier = MIPipes.electricityPipeTier.get(pipe);
                        lines.add(
                                new TranslatableComponent("text.modern_industrialization.eu_cable", new TranslatableComponent(tier.translationKey),
                                        TextHelper.getEuTextTick(tier.getMaxTransfer(), true)));
                    }
                }
                if (item == Items.GUNPOWDER) {
                    lines.add(new TranslatableComponent("text.modern_industrialization.gunpowder_upgrade").setStyle(TextHelper.GRAY_TEXT));
                }
                if (item == MIFluids.LUBRICANT.bucketItem) {
                    lines.add(new TranslatableComponent("text.modern_industrialization.lubricant_tooltip", LubricantHelper.mbPerTick)
                            .setStyle(TextHelper.GRAY_TEXT));
                }
                if (UpgradeComponent.upgrades.containsKey(item)) {
                    lines.add(new TranslatableComponent("text.modern_industrialization.machine_upgrade", UpgradeComponent.upgrades.get(item))
                            .setStyle(TextHelper.UPGRADE_TEXT));
                }
                if (item instanceof BlockItem) {
                    Block block = ((BlockItem) item).getBlock();
                    if (ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.containsKey(block)) {
                        lines.add(new TranslatableComponent("text.modern_industrialization.ebf_max_eu",
                                ElectricBlastFurnaceBlockEntity.coilsMaxBaseEU.get(block)).setStyle(TextHelper.UPGRADE_TEXT));
                    } else if (block instanceof OreBlock oreBlock) {
                        if (oreBlock.params.generate) {

                            MIConfig config = MIConfig.getConfig();

                            if (config.generateOres && !config.blacklistedOres.contains(oreBlock.materialName)) {
                                lines.add(TextHelper
                                        .formatWithNumber("text.modern_industrialization.ore_generation_tooltip_y", -64, oreBlock.params.maxYLevel)
                                        .setStyle(TextHelper.GRAY_TEXT_NOT_ITALIC));
                                lines.add(TextHelper.formatWithNumber("text.modern_industrialization.ore_generation_tooltip_vein_frequency",
                                        oreBlock.params.veinsPerChunk).setStyle(TextHelper.GRAY_TEXT_NOT_ITALIC));
                                lines.add(TextHelper
                                        .formatWithNumber("text.modern_industrialization.ore_generation_tooltip_vein_size", oreBlock.params.veinSize)
                                        .setStyle(TextHelper.GRAY_TEXT_NOT_ITALIC));
                            }

                        }
                    }
                }

                // Apparently tooltips are accessed from the main menu, or something, hence the
                // != null check
                if (Minecraft.getInstance().level != null && !MIConfig.getConfig().disableFuelTooltips) {
                    try {
                        Integer fuelTime = FuelRegistryImpl.INSTANCE.get(item);
                        if (fuelTime != null && fuelTime > 0) {

                            long totalEu = fuelTime * FuelBurningComponent.EU_PER_BURN_TICK;
                            lines.add(TextHelper.getEuStorageTooltip(totalEu));
                        }
                    } catch (Exception e) {
                        ModernIndustrialization.LOGGER.warn("Could not show MI fuel tooltip.", e);
                    }
                }

                if (context.isAdvanced() && !MIConfig.getConfig().disableItemTagTooltips) {
                    var ids = item.builtInRegistryHolder().tags().map(TagKey::location).sorted().toList();
                    for (ResourceLocation id : ids) {
                        lines.add(new TextComponent("#" + id).setStyle(TextHelper.GRAY_TEXT));
                    }
                }
            }
        }));

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof BarrelTooltipData barrelData) {
                return new BarrelTooltipComponent(barrelData);
            } else if (data instanceof SteamDrillItem.SteamDrillTooltipData steamDrillData) {
                return new SteamDrillItem.SteamDrillTooltipComponent(steamDrillData);
            }
            return null;
        });
    }

    private void setupClientCommands() {
        ClientCommandManager.DISPATCHER.register(literal("miclient")//
                .then(literal("dump_missing_translations").executes(MissingTranslationsCommand::run))//
        );
    }
}
