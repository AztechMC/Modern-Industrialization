package aztech.modern_industrialization;

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.pipe.item.SpeedUpgrade;
import aztech.modern_industrialization.blocks.WrenchableBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.compat.ae2.MIAEAddon;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.datagen.MIDatagenServer;
import aztech.modern_industrialization.debug.DebugCommands;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.ItemDefinition;
import aztech.modern_industrialization.fluid.MIFluid;
import aztech.modern_industrialization.items.armor.MIArmorEffects;
import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.items.tools.QuantumSword;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.machines.init.MultiblockHatches;
import aztech.modern_industrialization.machines.init.MultiblockMachines;
import aztech.modern_industrialization.machines.init.SingleBlockCraftingMachines;
import aztech.modern_industrialization.machines.init.SingleBlockSpecialMachines;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.materials.part.TextureGenParams;
import aztech.modern_industrialization.misc.autotest.MIAutoTesting;
import aztech.modern_industrialization.misc.guidebook.GuidebookEvents;
import aztech.modern_industrialization.network.MIPackets;
import aztech.modern_industrialization.nuclear.FluidNuclearComponent;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(MI.ID)
public class MI {
    public static final String ID = "modern_industrialization";
    public static final Logger LOGGER = LoggerFactory.getLogger("Modern Industrialization");

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public MI(IEventBus modBus) {
        MIFluids.init(modBus);
        MIBlock.init(modBus);
        MIItem.init(modBus);
        MIRegistries.init(modBus);
        MIMaterials.init();

        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();
        KubeJSProxy.instance.fireRegisterMachinesEvent();

        MIPipes.INSTANCE.setup();

        CommonProxy.initEvents();
        ChunkEventListeners.init();
        DebugCommands.init();
        GuidebookEvents.init();
        MIArmorEffects.init();

        if (System.getProperty("modern_industrialization.autoTest") != null) {
            MIAutoTesting.init();
        }

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerChangedDimensionEvent.class, event -> MIKeyMap.clear(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> MIKeyMap.clear(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(VillagerTradesEvent.class, MIVillager::init);

        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, event -> {
            if (event.getUseBlock() == Event.Result.DENY) {
                return;
            }

            var hand = event.getHand();
            var hitResult = event.getHitVec();
            var player = event.getEntity();
            var world = event.getLevel();

            if (player.isSpectator() || !event.getLevel().mayInteract(player, hitResult.getBlockPos())) {
                return;
            }

            if (player.getItemInHand(hand).is(MITags.WRENCHES)) {
                if (world.getBlockEntity(hitResult.getBlockPos()) instanceof WrenchableBlockEntity wrenchable) {
                    if (wrenchable.useWrench(player, hand, hitResult)) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.sidedSuccess(world.isClientSide()));
                    }
                }
            }
        });
        // Setup after, so wrench has priority
        BarrelBlock.setupBarrelEvents();

        modBus.addListener(FMLCommonSetupEvent.class, event -> {
            MIBlock.BLOCK_DEFINITIONS.values().forEach(BlockDefinition::onRegister);
            MIItem.ITEM_DEFINITIONS.values().forEach(ItemDefinition::onRegister);

            FluidFuelRegistry.init();
            FluidNuclearComponent.init();
            MIFuels.init();


            SpeedUpgrade.UPGRADES.put(MIItem.MOTOR.asItem(), 2);
            SpeedUpgrade.UPGRADES.put(MIItem.LARGE_MOTOR.asItem(), 8);
            SpeedUpgrade.UPGRADES.put(MIItem.ADVANCED_MOTOR.asItem(), 32);
            SpeedUpgrade.UPGRADES.put(MIItem.LARGE_ADVANCED_MOTOR.asItem(), 64);
        });

        modBus.addListener(GatherDataEvent.class, event -> {
            MIDatagenServer.configure(
                    event.getGenerator(),
                    event.getExistingFileHelper(),
                    event.getLookupProvider(),
                    event.includeServer(),
                    false);
        });

        modBus.addListener(RegisterCapabilitiesEvent.class, MICapabilities::init);
        modBus.addListener(RegisterPayloadHandlerEvent.class, MIPackets::init);

        if (MIConfig.loadAe2Compat()) {
            MIAEAddon.init(modBus);
        }

        LOGGER.info("Modern Industrialization setup done!");
    }
}
