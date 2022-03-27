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
import aztech.modern_industrialization.api.ScrewdriverableBlockEntity;
import aztech.modern_industrialization.api.WrenchableBlockEntity;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.armor.ArmorPackets;
import aztech.modern_industrialization.items.armor.MIArmorEffects;
import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.init.*;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.misc.autotest.MIAutoTesting;
import aztech.modern_industrialization.misc.guidebook.GuidebookEvents;
import aztech.modern_industrialization.nuclear.NuclearItem;
import aztech.modern_industrialization.pipes.MIPipes;
import java.util.Map;
import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.loot.JEntry;
import net.devtech.arrp.json.loot.JLootTable;
import net.devtech.arrp.json.loot.JPool;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModernIndustrialization implements ModInitializer {

    public static final String MOD_ID = "modern_industrialization";
    public static final Logger LOGGER = LogManager.getLogger("Modern Industrialization");
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("modern_industrialization:general");

    // Materials
    public static final Material METAL_MATERIAL = new FabricMaterialBuilder(MaterialColor.METAL).build();
    public static final Material STONE_MATERIAL = new FabricMaterialBuilder(MaterialColor.STONE).build();

    public static final CreativeModeTab ITEM_GROUP = FabricItemGroupBuilder.build(new ResourceLocation(MOD_ID, "general"),
            () -> new ItemStack(Registry.ITEM.get(new MIIdentifier("forge_hammer"))));

    // ScreenHandlerType
    public static final MenuType<MachineScreenHandlers.Common> SCREEN_HANDLER_MACHINE = ScreenHandlerRegistry
            .registerExtended(new MIIdentifier("machine"), MachineScreenHandlers::createClient);
    public static final MenuType<ForgeHammerScreenHandler> SCREEN_HANDLER_FORGE_HAMMER = ScreenHandlerRegistry
            .registerSimple(new ResourceLocation(MOD_ID, "forge_hammer"), ForgeHammerScreenHandler::new);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        MIMaterials.init();
        MITags.init();
        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();
        NuclearItem.init();
        setupItems();
        setupBlocks();
        MIBlockEntityTypes.init();
        MIFluids.setupFluids();
        // fields.
        setupPackets();
        setupFuels();
        MIArmorEffects.init();
        setupWrench();

        MIPipes.INSTANCE.setup();

        RRPCallback.EVENT.register(a -> {
            a.add(RESOURCE_PACK);
        });

        ChunkEventListeners.init();
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, oldWorld, newWorld) -> MIKeyMap.clear(player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            MIKeyMap.clear(handler.player);
        });
        GuidebookEvents.init();

        if (System.getProperty("modern_industrialization.autoTest") != null) {
            MIAutoTesting.init();
        }

        LOGGER.info("Modern Industrialization setup done!");
    }

    private void setupItems() {
        for (Map.Entry<String, Item> entry : MIItem.items.entrySet()) {
            registerItem(entry.getValue(), entry.getKey());
            if (MIItem.registrationEvents.containsKey(entry.getKey())) {
                MIItem.registrationEvents.get(entry.getKey()).accept(entry.getValue());
            }
        }
    }

    public static void registerItem(Item item, String id) {
        ResourceLocation ID = new MIIdentifier(id);
        Registry.register(Registry.ITEM, ID, item);

        RESOURCE_PACK.addModel(
                JModel.model().parent(MIItem.handhelds.contains(id) ? "minecraft:item/handheld" : "minecraft:item/generated")
                        .textures(new JTextures().layer0(ID.getNamespace() + ":items/" + ID.getPath())),
                new ResourceLocation(ID.getNamespace() + ":item/" + ID.getPath()));
    }

    private void setupBlocks() {
        for (Map.Entry<String, MIBlock> entry : MIBlock.blocks.entrySet()) {
            registerBlock(entry.getValue());
            entry.getValue().onRegister(entry.getValue(), entry.getValue().blockItem);
        }
    }

    public static void registerBlock(Block block, Item item, String id, int flag) {
        ResourceLocation identifier = new MIIdentifier(id);
        Registry.register(Registry.BLOCK, identifier, block);
        if (Registry.ITEM.getOptional(identifier).isEmpty()) {
            Registry.register(Registry.ITEM, identifier, item);
        }
        if ((flag & MIBlock.FLAG_BLOCK_LOOT) != 0) {
            if (block instanceof MIBlock) {
                RESOURCE_PACK.addLootTable(new MIIdentifier("blocks/" + id), ((MIBlock) block).getLootTables());
            } else {
                RESOURCE_PACK.addLootTable(new MIIdentifier("blocks/" + id),
                        JLootTable.loot("minecraft:block")
                                .pool(new JPool().rolls(1).entry(new JEntry().type("minecraft:item").name(ModernIndustrialization.MOD_ID + ":" + id))
                                        .condition(new JCondition("minecraft:survives_explosion"))));
            }
        }

        // TODO: client side?
        RESOURCE_PACK.addBlockState(JState.state().add(new JVariant().put("", new JBlockModel(MOD_ID + ":block/" + id))), identifier);

        if ((flag & MIBlock.FLAG_BLOCK_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent("block/cube_all").textures(new JTextures().var("all", MOD_ID + ":blocks/" + id)),
                    new MIIdentifier("block/" + id));

        if ((flag & MIBlock.FLAG_BLOCK_ITEM_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent(MOD_ID + ":block/" + id), new MIIdentifier("item/" + id));

    }

    public static void registerBlock(Block block, Item item, String id) {
        registerBlock(block, item, id, MIBlock.FLAG_BLOCK_LOOT | MIBlock.FLAG_BLOCK_ITEM_MODEL | MIBlock.FLAG_BLOCK_MODEL);
    }

    public static void registerBlock(MIBlock block) {
        ResourceLocation identifier = new MIIdentifier(block.id);
        Registry.register(Registry.BLOCK, identifier, block);

        if (Registry.ITEM.getOptional(identifier).isEmpty()) {
            Registry.register(Registry.ITEM, identifier, block.blockItem);
        }

        if ((block.FLAGS & MIBlock.FLAG_BLOCK_LOOT) != 0) {
            if (block instanceof MIBlock) {
                RESOURCE_PACK.addLootTable(new MIIdentifier("blocks/" + block.id), block.getLootTables());
            }
        }
        // TODO: client side?
        RESOURCE_PACK.addBlockState(block.getBlockState(), identifier);

        if ((block.FLAGS & MIBlock.FLAG_BLOCK_MODEL) != 0)
            RESOURCE_PACK.addModel(block.getBlockModel(), new MIIdentifier("block/" + block.id));

        if ((block.FLAGS & MIBlock.FLAG_BLOCK_ITEM_MODEL) != 0)
            RESOURCE_PACK.addModel(block.getItemModel(), new MIIdentifier("item/" + block.id));

    }

    private void setupPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.SET_LOCKING_MODE,
                ConfigurableInventoryPacketHandlers.C2S.SET_LOCKING_MODE);
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.DO_SLOT_DRAGGING,
                ConfigurableInventoryPacketHandlers.C2S.DO_SLOT_DRAGGING);
        ServerPlayNetworking.registerGlobalReceiver(ConfigurableInventoryPackets.ADJUST_SLOT_CAPACITY,
                ConfigurableInventoryPacketHandlers.C2S.ADJUST_SLOT_CAPACITY);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.SET_AUTO_EXTRACT, MachinePackets.C2S.ON_SET_AUTO_EXTRACT);
        ServerPlayNetworking.registerGlobalReceiver(MachinePackets.C2S.REI_LOCK_SLOTS, MachinePackets.C2S.ON_REI_LOCK_SLOTS);
        ServerSidePacketRegistry.INSTANCE.register(ArmorPackets.UPDATE_KEYS, ArmorPackets.ON_UPDATE_KEYS);
        ServerSidePacketRegistry.INSTANCE.register(ArmorPackets.ACTIVATE_CHEST, ArmorPackets.ON_ACTIVATE_CHEST);
    }

    private static void addFuel(String id, int burnTicks) {
        Item item = Registry.ITEM.get(new MIIdentifier(id));
        if (item == Items.AIR) {
            throw new IllegalArgumentException("Couldn't find item " + id);
        }
        FuelRegistry.INSTANCE.add(item, burnTicks);
    }

    private void setupFuels() {
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
        FluidFuelRegistry.register(MIFluids.CRUDE_OIL, 8);
        FluidFuelRegistry.register(MIFluids.SYNTHETIC_OIL, 8);
        FluidFuelRegistry.register(MIFluids.NAPHTHA, 40);
        FluidFuelRegistry.register(MIFluids.CREOSOTE, 80);
        FluidFuelRegistry.register(MIFluids.LIGHT_FUEL, 80);
        FluidFuelRegistry.register(MIFluids.HEAVY_FUEL, 120);
        FluidFuelRegistry.register(MIFluids.DIESEL, 200);
        FluidFuelRegistry.register(MIFluids.BOOSTED_DIESEL, 400);

    }

    private void setupWrench() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator() || !world.mayInteract(player, hitResult.getBlockPos())) {
                return InteractionResult.PASS;
            }

            boolean isWrench = player.getItemInHand(hand).is(MITags.WRENCHES);
            boolean isScrewdriver = player.getItemInHand(hand).is(MITags.SCREWDRIVERS);
            if (isWrench || isScrewdriver) {
                BlockEntity entity = world.getBlockEntity(hitResult.getBlockPos());
                if (isWrench && entity instanceof WrenchableBlockEntity wrenchable) {
                    if (wrenchable.useWrench(player, hand, hitResult)) {
                        return InteractionResult.sidedSuccess(world.isClientSide());
                    }
                }
                if (isScrewdriver && entity instanceof ScrewdriverableBlockEntity screwdriverable) {
                    if (screwdriverable.useScrewdriver(player, hand, hitResult)) {
                        return InteractionResult.sidedSuccess(world.isClientSide());
                    }
                }
            }

            return InteractionResult.PASS;
        });
    }
}
