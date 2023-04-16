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

import aztech.modern_industrialization.api.FluidFuelRegistry;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.compat.ae2.AECompatCondition;
import aztech.modern_industrialization.compat.kubejs.KubeJSProxy;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.definition.FluidDefinition;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.armor.ArmorPackets;
import aztech.modern_industrialization.items.armor.MIArmorEffects;
import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.machines.init.*;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.misc.autotest.MIAutoTesting;
import aztech.modern_industrialization.misc.guidebook.GuidebookEvents;
import aztech.modern_industrialization.nuclear.NuclearItem;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.stats.PlayerStatisticsData;
import java.util.Comparator;
import java.util.Map;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModernIndustrialization {

    public static final String MOD_ID = "modern_industrialization";
    public static final Logger LOGGER = LogManager.getLogger("Modern Industrialization");

    // Materials
    public static final Material METAL_MATERIAL = new FabricMaterialBuilder(MaterialColor.METAL).build();
    public static final Material STONE_MATERIAL = new FabricMaterialBuilder(MaterialColor.STONE).build();

    public static final CreativeModeTab ITEM_GROUP = FabricItemGroupBuilder.build(new ResourceLocation(MOD_ID, "general"),
            () -> new ItemStack(Registry.ITEM.get(new MIIdentifier("forge_hammer"))));

    // ScreenHandlerType
    public static final MenuType<MachineMenuCommon> SCREEN_HANDLER_MACHINE = ScreenHandlerRegistry
            .registerExtended(new MIIdentifier("machine"), CommonProxy.INSTANCE::createClientMachineMenu);
    public static final MenuType<ForgeHammerScreenHandler> SCREEN_HANDLER_FORGE_HAMMER = ScreenHandlerRegistry
            .registerSimple(new MIIdentifier("forge_hammer"), ForgeHammerScreenHandler::new);

    public static void initialize() {
        MIMaterials.init();
        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();
        KubeJSProxy.instance.fireRegisterMachinesEvent();
        NuclearItem.init();
        MIPipes.INSTANCE.setup();
        setupFluids();
        setupItems();
        setupBlocks();
        MIBlockEntityTypes.init();
        MIVillager.init();

        // fields.
        setupPackets();
        setupFuels();
        MIArmorEffects.init();
        setupWrench();

        ChunkEventListeners.init();
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, oldWorld, newWorld) -> MIKeyMap.clear(player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            MIKeyMap.clear(handler.player);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, packetSender, server) -> {
            var player = handler.getPlayer();
            PlayerStatisticsData.get(server).get(player).onPlayerJoin(player);
        });
        GuidebookEvents.init();

        CommonProxy.initEvents();
        AECompatCondition.init();

        if (System.getProperty("modern_industrialization.autoTest") != null) {
            MIAutoTesting.init();
        }

        LOGGER.info("Modern Industrialization setup done!");
    }

    private static void setupItems() {
        MIItem.ITEMS.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue().sortOrder)).forEach(entry -> {
            Registry.register(Registry.ITEM, entry.getKey(), entry.getValue().asItem());
            entry.getValue().onRegister();
        });
    }

    private static void setupBlocks() {
        for (Map.Entry<ResourceLocation, BlockDefinition<?>> entry : MIBlock.BLOCKS.entrySet()) {
            Registry.register(Registry.BLOCK, entry.getKey(), entry.getValue().asBlock());
            entry.getValue().onRegister();
        }
    }

    private static void setupFluids() {
        for (Map.Entry<ResourceLocation, FluidDefinition> entry : MIFluids.FLUIDS.entrySet()) {
            Registry.register(Registry.BLOCK, entry.getKey(), entry.getValue().fluidBlock);
            Registry.register(Registry.FLUID, entry.getKey(), entry.getValue().asFluid());
        }

        if (MIConfig.getConfig().colorWaterLava) {
            FluidVariantAttributes.enableColoredVanillaFluidNames();
        }
    }

    private static void setupPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.LOCK_ALL,
                ConfigurableInventoryPacketHandlers.C2S.LOCK_ALL);
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.SET_LOCKING_MODE,
                ConfigurableInventoryPacketHandlers.C2S.SET_LOCKING_MODE);
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.DO_SLOT_DRAGGING,
                ConfigurableInventoryPacketHandlers.C2S.DO_SLOT_DRAGGING);
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.ADJUST_SLOT_CAPACITY,
                ConfigurableInventoryPacketHandlers.C2S.ADJUST_SLOT_CAPACITY);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.CHANGE_SHAPE, MachinePackets.C2S.ON_CHANGE_SHAPE);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.SET_AUTO_EXTRACT, MachinePackets.C2S.ON_SET_AUTO_EXTRACT);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.FORGE_HAMMER_MOVE_RECIPE, MachinePackets.C2S.ON_FORGE_HAMMER_MOVE_RECIPE);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.REI_LOCK_SLOTS, MachinePackets.C2S.ON_REI_LOCK_SLOTS);
        CommonProxy.INSTANCE.registerUnsidedPacket(ArmorPackets.UPDATE_KEYS, ArmorPackets.ON_UPDATE_KEYS);
        CommonProxy.INSTANCE.registerUnsidedPacket(ArmorPackets.ACTIVATE_CHEST, ArmorPackets.ON_ACTIVATE_CHEST);
    }

    private static void addFuel(String id, int burnTicks) {
        Item item = Registry.ITEM.get(new MIIdentifier(id));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Couldn't find item " + id);
        }
        FuelRegistry.INSTANCE.add(item, burnTicks);
    }

    private static void setupFuels() {
        addFuel("coke", 6400);
        addFuel("coke_dust", 6400);
        addFuel("coke_block", Short.MAX_VALUE); // F*** YOU VANILLA ! (Should be 6400*9 but it overflows ...)
        addFuel("coal_crushed_dust", 1600);
        FuelRegistry.INSTANCE.add(MITags.item("coal_dusts"), 1600);
        addFuel("coal_tiny_dust", 160);
        addFuel("lignite_coal", 1600);
        addFuel("lignite_coal_block", 16000);
        addFuel("lignite_coal_crushed_dust", 1600);
        addFuel("lignite_coal_dust", 1600);
        addFuel("lignite_coal_tiny_dust", 160);
        addFuel("carbon_dust", 6400);
        addFuel("carbon_tiny_dust", 640);

        FluidFuelRegistry.register(MIFluids.HYDROGEN, 1);
        FluidFuelRegistry.register(MIFluids.DEUTERIUM, 1);
        FluidFuelRegistry.register(MIFluids.TRITIUM, 1);
        FluidFuelRegistry.register(MIFluids.CRUDE_OIL, 16);
        FluidFuelRegistry.register(MIFluids.SYNTHETIC_OIL, 16);
        FluidFuelRegistry.register(MIFluids.RAW_BIODIESEL, 50);
        FluidFuelRegistry.register(MIFluids.NAPHTHA, 80);
        FluidFuelRegistry.register(MIFluids.CREOSOTE, 160);
        FluidFuelRegistry.register(MIFluids.LIGHT_FUEL, 160);
        FluidFuelRegistry.register(MIFluids.HEAVY_FUEL, 240);
        FluidFuelRegistry.register(MIFluids.BIODIESEL, 250);
        FluidFuelRegistry.register(MIFluids.DIESEL, 400);
        FluidFuelRegistry.register(MIFluids.BOOSTED_DIESEL, 800);
    }

    private static void setupWrench() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator() || !world.mayInteract(player, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }

            if (player.getItemInHand(hand).is(MITags.WRENCHES)) {
                if (world.getBlockEntity(hitResult.getBlockPos()) instanceof WrenchableBlockEntity wrenchable) {
                    if (wrenchable.useWrench(player, hand, hitResult)) {
                        return InteractionResult.sidedSuccess(world.isClientSide());
                    }
                }
            }

            return InteractionResult.PASS;
        });

        // Setup after, so wrench has priority
        BarrelBlock.setupBarrelEvents();
    }
}
