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
import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.api.pipes.item.SpeedUpgrade;
import aztech.modern_industrialization.blocks.TrashCanBlock;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerBlock;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerPacket;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.blocks.tank.CreativeTankSetup;
import aztech.modern_industrialization.compat.RecipeCompat;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPacketHandlers;
import aztech.modern_industrialization.inventory.ConfigurableInventoryPackets;
import aztech.modern_industrialization.items.FluidFuelItemHelper;
import aztech.modern_industrialization.items.SteamDrillItem;
import aztech.modern_industrialization.items.armor.ArmorPackets;
import aztech.modern_industrialization.items.armor.JetpackItem;
import aztech.modern_industrialization.items.armor.MIKeyMap;
import aztech.modern_industrialization.items.diesel_tools.DieselToolItem;
import aztech.modern_industrialization.items.tools.CrowbarItem;
import aztech.modern_industrialization.items.tools.WrenchItem;
import aztech.modern_industrialization.machines.MachinePackets;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.init.*;
import aztech.modern_industrialization.machines.multiblocks.world.ChunkEventListeners;
import aztech.modern_industrialization.materials.MIMaterials;
import aztech.modern_industrialization.misc.guidebook.GuidebookEvents;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.recipe.MIRecipes;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
import java.util.Map;
import me.shedaniel.cloth.api.common.events.v1.PlayerChangeWorldCallback;
import me.shedaniel.cloth.api.common.events.v1.PlayerLeaveCallback;
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
import net.devtech.arrp.json.tags.JTag;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricMaterialBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModernIndustrialization implements ModInitializer {

    public static final int FLAG_BLOCK_LOOT = 1;
    public static final int FLAG_BLOCK_MODEL = 1 << 1;
    public static final int FLAG_BLOCK_ITEM_MODEL = 1 << 2;

    public static final String MOD_ID = "modern_industrialization";
    public static final Logger LOGGER = LogManager.getLogger("Modern Industrialization");
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("modern_industrialization:general");

    // Materials
    public static final Material METAL_MATERIAL = new FabricMaterialBuilder(MapColor.IRON_GRAY).build();
    public static final Material STONE_MATERIAL = new FabricMaterialBuilder(MapColor.STONE_GRAY).build();

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(MOD_ID, "general"),
            () -> new ItemStack(Registry.ITEM.get(new MIIdentifier("forge_hammer"))));

    // Tags
    public static final Tag<Item> OVERLAY_SOURCES = TagRegistry.item(new MIIdentifier("overlay_sources"));
    public static final Tag<Item> WRENCHES = TagRegistry.item(new Identifier("fabric:wrenches"));
    public static final Tag<Block> WRENCHABLES = TagRegistry.block(new Identifier("fabric:wrenchables"));

    // Item
    public static final Item ITEM_SCREWDRIVER = new Item(new Item.Settings().maxCount(1).group(ITEM_GROUP));
    public static final Item ITEM_WRENCH = new WrenchItem(new Item.Settings().maxCount(1).group(ITEM_GROUP));
    public static final JetpackItem ITEM_DIESEL_JETPACK = new JetpackItem(new Item.Settings().group(ITEM_GROUP));
    public static final DieselToolItem ITEM_DIESEL_CHAINSAW = new DieselToolItem(new Item.Settings().group(ITEM_GROUP));
    public static final DieselToolItem ITEM_DIESEL_DRILL = new DieselToolItem(new Item.Settings().group(ITEM_GROUP));
    public static final SteamDrillItem ITEM_STEAM_DRILL = new SteamDrillItem(new Item.Settings().group(ITEM_GROUP));
    public static final Item ITEM_CROWBAR = new CrowbarItem(new Item.Settings().group(ITEM_GROUP));

    // Block
    public static final Block FORGE_HAMMER = new ForgeHammerBlock();
    public static final Item ITEM_FORGE_HAMMER = new BlockItem(FORGE_HAMMER, new Item.Settings().group(ITEM_GROUP));
    public static final TrashCanBlock TRASH_CAN = new TrashCanBlock();
    public static final BlockItem ITEM_TRASH_CAN = new BlockItem(TRASH_CAN, new Item.Settings().group(ITEM_GROUP));

    // ScreenHandlerType
    public static final ScreenHandlerType<MachineScreenHandlers.Common> SCREEN_HANDLER_MACHINE = ScreenHandlerRegistry
            .registerExtended(new MIIdentifier("machine"), MachineScreenHandlers::createClient);
    public static final ScreenHandlerType<ForgeHammerScreenHandler> SCREEN_HANDLER_FORGE_HAMMER = ScreenHandlerRegistry
            .registerSimple(new Identifier(MOD_ID, "forge_hammer"), ForgeHammerScreenHandler::new);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        MIMaterials.init();
        MIMachineRecipeTypes.init();
        SingleBlockCraftingMachines.init();
        SingleBlockSpecialMachines.init();
        MultiblockHatches.init();
        MultiblockMachines.init();
        setupItems();
        setupBlocks();
        MIFluids.setupFluids();
        CreativeTankSetup.setup();
        // SingleBlockCraftingMachines.setupRecipes(); // will also load the static
        // fields.
        ForgeHammerScreenHandler.setupRecipes();
        // setupMachines();
        setupPackets();
        setupFuels();
        RecipeCompat.loadCompatRecipes();

        MIPipes.INSTANCE.setup();

        RRPCallback.EVENT.register(a -> {
            a.add(RESOURCE_PACK);
            a.add(MIRecipes.buildRecipesPack());
        });

        ChunkEventListeners.init();
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (be instanceof ChunkUnloadBlockEntity) {
                ((ChunkUnloadBlockEntity) be).onChunkUnload();
            }
        });
        PlayerChangeWorldCallback.EVENT.register((player, oldWorld, newWorld) -> MIKeyMap.clear(player));
        PlayerLeaveCallback.EVENT.register(MIKeyMap::clear);
        GuidebookEvents.init();

        LOGGER.info("Modern Industrialization setup done!");
    }

    private void setupItems() {
        for (Map.Entry<String, Item> entry : MIItem.items.entrySet()) {
            registerItem(entry.getValue(), entry.getKey());
        }

        registerItem(ITEM_SCREWDRIVER, "screwdriver");
        registerItem(ITEM_WRENCH, "wrench");
        registerItem(ITEM_DIESEL_JETPACK, "diesel_jetpack");
        registerItem(ITEM_DIESEL_CHAINSAW, "diesel_chainsaw", true);
        registerItem(ITEM_DIESEL_DRILL, "diesel_mining_drill", true);
        registerItem(ITEM_STEAM_DRILL, "steam_mining_drill", true);
        registerItem(ITEM_CROWBAR, "crowbar", true);

        FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(DieselToolItem.CAPACITY, stack, ctx),
                ITEM_DIESEL_CHAINSAW, ITEM_DIESEL_DRILL);
        FluidStorage.ITEM.registerForItems((stack, ctx) -> new FluidFuelItemHelper.ItemStorage(JetpackItem.CAPACITY, stack, ctx),
                ITEM_DIESEL_JETPACK);

        SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 2, MIItem.ITEM_MOTOR);
        SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 8, MIItem.ITEM_LARGE_MOTOR);
        SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 16, MIItem.ADVANCED_MOTOR);
        SpeedUpgrade.LOOKUP.registerForItems((key, vd) -> () -> 64, MIItem.LARGE_ADVANCED_MOTOR);

        RESOURCE_PACK.addTag(new MIIdentifier("items/overlay_sources"), JTag.tag().tag(new Identifier("fabric:wrenches")));
    }

    private void setupBlocks() {
        registerBlock(FORGE_HAMMER, ITEM_FORGE_HAMMER, "forge_hammer", FLAG_BLOCK_LOOT | FLAG_BLOCK_ITEM_MODEL);
        registerBlock(TRASH_CAN, ITEM_TRASH_CAN, "trash_can", 7);
        for (Map.Entry<String, MIBlock> entry : MIBlock.blocks.entrySet()) {
            int flags = FLAG_BLOCK_ITEM_MODEL | FLAG_BLOCK_LOOT;
            if (entry.getValue().arrpModel) {
                flags |= FLAG_BLOCK_MODEL;
            }
            registerBlock(entry.getValue(), entry.getValue().blockItem, entry.getKey(), flags);
        }

        ItemStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashCanBlock.trashStorage(), TRASH_CAN);
        FluidStorage.SIDED.registerForBlocks((world, pos, state, be, direction) -> TrashCanBlock.trashStorage(), TRASH_CAN);
        FluidStorage.ITEM.registerForItems((key, ctx) -> TrashCanBlock.trashStorage(), ITEM_TRASH_CAN);
        EnergyApi.MOVEABLE.registerForBlocks((world, pos, state, be, direction) -> EnergyApi.CREATIVE_EXTRACTABLE,
                CreativeTankSetup.CREATIVE_TANK_BLOCK);
    }

    public static void registerBlock(Block block, Item item, String id, int flag) {
        Identifier identifier = new MIIdentifier(id);
        Registry.register(Registry.BLOCK, identifier, block);
        if (Registry.ITEM.getOrEmpty(identifier).isEmpty()) {
            Registry.register(Registry.ITEM, identifier, item);
        }
        if ((flag & FLAG_BLOCK_LOOT) != 0) {
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

        if ((flag & FLAG_BLOCK_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent("block/cube_all").textures(new JTextures().var("all", MOD_ID + ":blocks/" + id)),
                    new MIIdentifier("block/" + id));

        if ((flag & FLAG_BLOCK_ITEM_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent(MOD_ID + ":block/" + id), new MIIdentifier("item/" + id));

    }

    public static void registerBlock(Block block, Item item, String id) {
        registerBlock(block, item, id, FLAG_BLOCK_LOOT | FLAG_BLOCK_ITEM_MODEL | FLAG_BLOCK_MODEL);
    }

    public static void registerItem(Item item, String id, boolean handheld) {
        registerItem(item, new MIIdentifier(id), handheld);
    }

    public static void registerItem(Item item, Identifier id, boolean handheld) {
        Registry.register(Registry.ITEM, id, item);
        RESOURCE_PACK.addModel(
                JModel.model().parent(handheld ? "minecraft:item/handheld" : "minecraft:item/generated")
                        .textures(new JTextures().layer0(id.getNamespace() + ":items/" + id.getPath())),
                new Identifier(id.getNamespace() + ":item/" + id.getPath()));
    }

    public static void registerItem(Item item, String id) {
        registerItem(item, id, false);
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
        ServerSidePacketRegistry.INSTANCE.register(ForgeHammerPacket.SET_HAMMER, ForgeHammerPacket.ON_SET_HAMMER);
        ServerSidePacketRegistry.INSTANCE.register(ArmorPackets.UPDATE_KEYS, ArmorPackets.ON_UPDATE_KEYS);
        ServerSidePacketRegistry.INSTANCE.register(ArmorPackets.ACTIVATE_JETPACK, ArmorPackets.ON_ACTIVATE_JETPACK);
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
        FuelRegistry.INSTANCE.add(TagRegistry.item(new Identifier("c:coal_dusts")), 1600);
        addFuel("coal_tiny_dust", 160);
        addFuel("lignite_coal", 1600);
        addFuel("lignite_coal_block", 16000);
        addFuel("lignite_coal_crushed_dust", 1600);
        addFuel("lignite_coal_dust", 1600);
        addFuel("lignite_coal_tiny_dust", 160);
        addFuel("carbon_dust", 6400);
        addFuel("carbon_tiny_dust", 640);

        FluidFuelRegistry.register(MIFluids.CRUDE_OIL, 8);
        FluidFuelRegistry.register(MIFluids.DIESEL, 200);
        FluidFuelRegistry.register(MIFluids.HEAVY_FUEL, 120);
        FluidFuelRegistry.register(MIFluids.LIGHT_FUEL, 80);
        FluidFuelRegistry.register(MIFluids.CREOSOTE, 80);
        FluidFuelRegistry.register(MIFluids.NAPHTHA, 40);
        FluidFuelRegistry.register(MIFluids.SYNTHETIC_OIL, 8);
        FluidFuelRegistry.register(MIFluids.BOOSTED_DIESEL, 400);
    }
}
