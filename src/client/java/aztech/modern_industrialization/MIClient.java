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
import aztech.modern_industrialization.blocks.storage.barrel.BarrelRenderer;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelTooltipData;
import aztech.modern_industrialization.blocks.storage.barrel.DeferredBarrelTextRenderer;
import aztech.modern_industrialization.blocks.storage.barrel.client.BarrelTooltipComponent;
import aztech.modern_industrialization.blocks.storage.tank.TankRenderer;
import aztech.modern_industrialization.datagen.MIDatagenClient;
import aztech.modern_industrialization.datagen.model.DelegatingModelBuilder;
import aztech.modern_industrialization.items.ConfigCardItem;
import aztech.modern_industrialization.items.RedstoneControlModuleItem;
import aztech.modern_industrialization.items.SteamDrillHighlight;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.SteamDrillTooltipComponent;
import aztech.modern_industrialization.items.armor.ClientKeyHandler;
import aztech.modern_industrialization.items.armor.HudRenderer;
import aztech.modern_industrialization.items.armor.JetpackParticleAdder;
import aztech.modern_industrialization.items.client.ClientConfigCardTooltip;
import aztech.modern_industrialization.machines.MachineBlock;
import aztech.modern_industrialization.machines.MachineBlockEntityRenderer;
import aztech.modern_industrialization.machines.MachineOverlayClient;
import aztech.modern_industrialization.machines.blockentities.multiblocks.LargeTankMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.FuelBurningComponent;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineScreen;
import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineCasings;
import aztech.modern_industrialization.machines.models.MachineUnbakedModel;
import aztech.modern_industrialization.machines.models.UseBlockModelUnbakedModel;
import aztech.modern_industrialization.machines.multiblocks.MultiblockErrorHighlight;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBER;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockTankBER;
import aztech.modern_industrialization.misc.version.VersionEvents;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.pipes.MIPipesClient;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreen;
import aztech.modern_industrialization.pipes.impl.DelegatingUnbakedModel;
import aztech.modern_industrialization.pipes.impl.PipeUnbakedModel;
import aztech.modern_industrialization.pipes.item.ItemPipeScreen;
import aztech.modern_industrialization.util.TextHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MI.ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MIClient {
    @SubscribeEvent
    private static void init(FMLConstructModEvent ignored) {
        var modBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        Objects.requireNonNull(modBus);

        NeoForge.EVENT_BUS.addListener(SteamDrillHighlight::onBlockHighlight);
        NeoForge.EVENT_BUS.addListener(MachineOverlayClient::onBlockOutline);
        DeferredBarrelTextRenderer.init();
        MultiblockErrorHighlight.init();
        MIPipesClient.setupClient(modBus);
        VersionEvents.init(ModLoadingContext.get().getActiveContainer());

        NeoForge.EVENT_BUS.addListener(TickEvent.RenderTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.START) {
                JetpackParticleAdder.addJetpackParticles(Minecraft.getInstance());
            }
        });
        NeoForge.EVENT_BUS.addListener(TickEvent.ClientTickEvent.class, event -> {
            if (event.phase == TickEvent.Phase.END) {
                ClientKeyHandler.onEndTick(Minecraft.getInstance());
            }
        });
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> {
            MITooltips.attachTooltip(event.getItemStack(), event.getToolTip());

            // Apparently tooltips are accessed from the main menu, or something, hence the
            // != null check
            if (Minecraft.getInstance().level != null && !MIConfig.getConfig().disableFuelTooltips) {
                try {
                    int fuelTime = CommonHooks.getBurnTime(event.getItemStack(), null);
                    if (fuelTime > 0) {
                        long totalEu = fuelTime * FuelBurningComponent.EU_PER_BURN_TICK;
                        event.getToolTip().add(new MITooltips.Line(MIText.BaseEuTotalStored).arg(totalEu, MITooltips.EU_PARSER).build());
                    }
                } catch (Exception e) {
                    MI.LOGGER.warn("Could not show MI fuel tooltip.", e);
                }
            }

            if (event.getFlags().isAdvanced() && !MIConfig.getConfig().disableItemTagTooltips) {
                var ids = event.getItemStack().getTags().map(TagKey::location).sorted().toList();
                for (ResourceLocation id : ids) {
                    event.getToolTip().add(Component.literal("#" + id).setStyle(TextHelper.GRAY_TEXT));
                }
            }
        });

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
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parentScreen) -> AutoConfig.getConfigScreen(MIConfig.class, parentScreen).get()));

        // Warn if neither JEI nor REI is present!
        if (!ModList.get().isLoaded("emi") && !ModList.get().isLoaded("jei")
                && !ModList.get().isLoaded("roughlyenoughitems")) {
            NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
                if (MIConfig.getConfig().enableNoEmiMessage) {
                    event.getPlayer().displayClientMessage(MIText.NoEmi.text().withStyle(ChatFormatting.GOLD), false);
                }
            });
        }

        // TODO NEO: runtime datagen
//        if (MIConfig.getConfig().datagenOnStartup) {
//            RuntimeDataGen.run(gen -> {
//                MIDatagenClient.configure(gen, true);
//                MIDatagenServer.configure(gen, true);
//            });
//        }

        MI.LOGGER.info("Modern Industrialization client setup done!");
    }

    @SubscribeEvent
    private static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MIRegistries.FORGE_HAMMER_MENU.get(), ForgeHammerScreen::new);
        event.register((MenuType<MachineMenuClient>) MIRegistries.MACHINE_MENU.get(), MachineScreen::new);

        event.register(MIPipes.SCREEN_HANDLER_TYPE_ITEM_PIPE.get(), ItemPipeScreen::new);
        event.register(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE.get(), FluidPipeScreen::new);
    }

    @SubscribeEvent
    private static void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(DelegatingModelBuilder.LOADER_ID, DelegatingUnbakedModel.LOADER);
        event.register(MachineUnbakedModel.LOADER_ID, MachineUnbakedModel.LOADER);
        event.register(PipeUnbakedModel.LOADER_ID, PipeUnbakedModel.LOADER);
        event.register(UseBlockModelUnbakedModel.LOADER_ID, UseBlockModelUnbakedModel.LOADER);
    }

    @SubscribeEvent
    private static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (var casing : MachineCasings.registeredCasings.values()) {
            event.register(MachineBakedModel.getCasingModelId(casing));
        }
    }

    private static final List<Runnable> blockEntityRendererRegistrations = new ArrayList<>();

    public static <T extends BlockEntity, U extends T> void registerBlockEntityRenderer(Supplier<BlockEntityType<U>> bet,
            BlockEntityRendererProvider<T> renderer) {
        blockEntityRendererRegistrations.add(() -> BlockEntityRenderers.register(bet.get(), renderer));
    }

    @SubscribeEvent
    private static void registerBlockEntityRenderers(FMLClientSetupEvent event) {
        for (var blockDef : MIBlock.BLOCKS.getEntries()) {
            if (blockDef.get() instanceof MachineBlock machine) {
                var blockEntity = machine.getBlockEntityInstance();
                BlockEntityType type = blockEntity.getType();

                if (blockEntity instanceof LargeTankMultiblockBlockEntity) {
                    BlockEntityRenderers.register(type, MultiblockTankBER::new);
                } else if (blockEntity instanceof MultiblockMachineBlockEntity) {
                    BlockEntityRenderers.register(type, MultiblockMachineBER::new);
                } else {
                    BlockEntityRenderers.register(type, c -> new MachineBlockEntityRenderer(c));
                }
            }
        }

        BlockEntityRenderers.register(MIRegistries.CREATIVE_BARREL_BE.get(), context -> new BarrelRenderer(0x000000));
        BlockEntityRenderers.register(MIRegistries.CREATIVE_TANK_BE.get(), context -> new TankRenderer(0x000000));

        blockEntityRendererRegistrations.forEach(Runnable::run);
    }

    @SubscribeEvent
    private static void registerClientTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BarrelTooltipData.class, BarrelTooltipComponent::new);
        event.register(ConfigCardItem.TooltipData.class, ClientConfigCardTooltip::new);
        event.register(SteamDrillItem.SteamDrillTooltipData.class, SteamDrillTooltipComponent::new);
    }

    @SubscribeEvent
    private static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.ITEM_NAME.id(), MI.id("activation_status"),
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                    HudRenderer.onRenderHud(guiGraphics, partialTick);
                });
    }

    @SubscribeEvent
    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ClientKeyHandler.keyActivate);
    }

    @SubscribeEvent
    private static void registerItemProperties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(MIItem.REDSTONE_CONTROL_MODULE.asItem(), MI.id("redstone_control_module"),
                    (stack, level, entity, seed) -> {
                        return RedstoneControlModuleItem.isRequiresLowSignal(stack) ? 0 : 1;
                    });
        });
    }
}
