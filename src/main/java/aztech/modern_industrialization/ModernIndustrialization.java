package aztech.modern_industrialization;


import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerBlock;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerPacket;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerScreenHandler;
import aztech.modern_industrialization.blocks.tank.MITanks;
import aztech.modern_industrialization.fluid.CraftingFluid;
import aztech.modern_industrialization.machines.MIMachines;
import aztech.modern_industrialization.machines.impl.MachineBlock;
import aztech.modern_industrialization.machines.impl.MachineFactory;
import aztech.modern_industrialization.machines.impl.MachinePackets;
import aztech.modern_industrialization.machines.impl.MachineScreenHandler;
import aztech.modern_industrialization.material.*;
import aztech.modern_industrialization.pipes.MIPipes;
import aztech.modern_industrialization.tools.WrenchItem;
import aztech.modern_industrialization.util.ChunkUnloadBlockEntity;
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

//import aztech.modern_industrialization.machines.impl.SteamBoilerScreenHandler;

public class ModernIndustrialization implements ModInitializer {


    public static final int FLAG_BLOCK_LOOT = 1;
    public static final int FLAG_BLOCK_MODEL = 1 << 1;
    public static final int FLAG_BLOCK_ITEM_MODEL = 1 << 2;


    public static final String MOD_ID = "modern_industrialization";
    public static final Logger LOGGER = LogManager.getLogger("Modern Industrialization");
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create("modern_industrialization:general");

    public static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(
            new Identifier(MOD_ID, "general"),
            () -> new ItemStack(Items.REDSTONE)
    );

    // Tags
    private static Identifier WRENCH_TAG = new Identifier("fabric", "wrenches");
    public static Tag<Item> TAG_WRENCH = TagRegistry.item(WRENCH_TAG);

    // Item
    public static final Item ITEM_WRENCH = new WrenchItem(new Item.Settings());

    // Block
    public static final Block FORGE_HAMMER = new ForgeHammerBlock();
    public static final Item ITEM_FORGE_HAMMER = new BlockItem(FORGE_HAMMER, new Item.Settings().group(ITEM_GROUP));

    // ScreenHandlerType
    public static final ScreenHandlerType<MachineScreenHandler> SCREEN_HANDLER_TYPE_MACHINE =
            ScreenHandlerRegistry.registerExtended(new Identifier(MOD_ID, "machine_recipe"), MachineScreenHandler::new);
    public static final ScreenHandlerType<ForgeHammerScreenHandler> SCREEN_HANDLER_FORGE_HAMMER =
            ScreenHandlerRegistry.registerSimple(new Identifier(MOD_ID, "forge_hammer"), ForgeHammerScreenHandler::new);

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        MITags.setup();
        setupItems();
        setupBlocks();
        setupBlockEntities();
        MIFluids.setupFluids();
        setupMaterial();
        MITanks.setup();
        MIMachines.setupRecipes(); // will also load the static fields.
        ForgeHammerScreenHandler.setupRecipes();
        setupMachines();
        setupPackets();
        setupFuels();

        MIPipes.INSTANCE.onInitialize();

        RRPCallback.EVENT.register(a -> a.add(RESOURCE_PACK));

        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            for (BlockEntity entity : chunk.getBlockEntities().values()) {
                if (entity instanceof ChunkUnloadBlockEntity) {
                    ((ChunkUnloadBlockEntity) entity).onChunkUnload();
                }
            }
        });

        LOGGER.info("Modern Industrialization setup done!");
    }

    private void setupMaterial() {
        for (MIMaterial material : MIMaterial.getAllMaterials()) {
            registerMaterial(material);
        }
        for (Map.Entry<Identifier, ConfiguredFeature<?, ?>> entry : MIMaterialSetup.ORE_GENERATORS.entrySet()) {
            Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, entry.getKey(), entry.getValue());
        }
    }

    private void setupItems() {
        for (MIItem item : MIItem.items.values()) {
            registerItem(item, item.getId());
        }

        registerItem(ITEM_WRENCH, "wrench");
    }

    private void setupBlocks() {
        registerBlock(FORGE_HAMMER, ITEM_FORGE_HAMMER, "forge_hammer", FLAG_BLOCK_LOOT | FLAG_BLOCK_ITEM_MODEL);
        for (MIBlock block : MIBlock.blocks.values()) {
            registerBlock(block, block.getItem(), block.getId());
        }
    }

    private void setupBlockEntities() {
        //BLOCK_ENTITY_STEAM_BOILER = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "steam_boiler"), BlockEntityType.Builder.create(SteamBoilerBlockEntity::new, BLOCK_STEAM_BOILER).build(null));
    }

    private void setupMachines() {
        for (MachineFactory factory : MachineFactory.getFactories()) {
            factory.block = new MachineBlock(factory.blockEntityConstructor);
            factory.item = new BlockItem(factory.block, new Item.Settings().group(ITEM_GROUP));
            registerBlock(factory.block, factory.item, factory.getID());
            factory.blockEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, factory.getID()), BlockEntityType.Builder.create(factory.blockEntityConstructor, factory.block).build(null));
        }
    }

    public static void registerBlock(Block block, Item item, String id, int flag) {
        Identifier identifier = new MIIdentifier(id);
        Registry.register(Registry.BLOCK, identifier, block);
        Registry.register(Registry.ITEM, identifier, item);
        if ((flag & FLAG_BLOCK_LOOT) != 0) {
            registerBlockLoot(id);
        }
        // TODO: client side?
        RESOURCE_PACK.addBlockState(JState.state().add(new JVariant().put("", new JBlockModel(MOD_ID + ":block/" + id))), identifier);

        if ((flag & FLAG_BLOCK_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent("block/cube_all").textures(
                    new JTextures().var("all", MOD_ID + ":blocks/" + id)),
                    new MIIdentifier("block/" + id)
            );

        if ((flag & FLAG_BLOCK_ITEM_MODEL) != 0)
            RESOURCE_PACK.addModel(JModel.model().parent(MOD_ID + ":block/" + id),
                    new MIIdentifier("item/" + id)
            );


    }


    public static void registerBlock(Block block, Item item, String id) {
        registerBlock(block, item, id, FLAG_BLOCK_LOOT | FLAG_BLOCK_ITEM_MODEL | FLAG_BLOCK_MODEL);
    }


    private void registerMaterial(MIMaterial material) {
        String id = material.getId();

        for (String block_type : material.getBlockType()) {
            Block block = material.getBlock(block_type);
            Item item = new BlockItem(block, new Item.Settings().group(ITEM_GROUP));
            Identifier identifier = new MIIdentifier(id + "_" + block_type);
            material.saveBlock(block_type, block);
            Registry.register(Registry.BLOCK, identifier, block);
            Registry.register(Registry.ITEM, identifier, item);
            RESOURCE_PACK.addBlockState(JState.state().add(
                    new JVariant().put("", new JBlockModel(MOD_ID + ":block/materials/" + id + "/" + block_type))), identifier);
            RESOURCE_PACK.addModel(JModel.model().parent("block/cube_all").textures(
                    new JTextures().var("all", MOD_ID + ":blocks/materials/" + id + "/" + block_type)),
                    new MIIdentifier("block/materials/" + id + "/" + block_type)
            );
            RESOURCE_PACK.addModel(JModel.model().parent(MOD_ID + ":block/materials/" + id + "/" + block_type),
                    new MIIdentifier("item/" + id + "_" + block_type)
            );
            registerBlockLoot(id + "_" + block_type);
        }

        for (String item_type : material.getItemType()) {
            Item item = new Item(new Item.Settings().group(ITEM_GROUP));
            material.saveItem(item_type, item);
            String custom_id = id;
            if (!id.equals(item_type)) {
                custom_id = id + "_" + item_type;
            }
            Registry.register(Registry.ITEM, new MIIdentifier(custom_id), item);
            RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated")
                    .textures(new JTextures().layer0(MOD_ID + ":items/materials/" + id + "/" + item_type)), new MIIdentifier("item/" + custom_id));
        }


    }



    public static void registerItem(Item item, String id) {
        Registry.register(Registry.ITEM, new MIIdentifier(id), item);
        RESOURCE_PACK.addModel(JModel.model().parent("minecraft:item/generated").textures(new JTextures().layer0(MOD_ID + ":items/" + id)), new MIIdentifier("item/" + id));
    }

    private static void registerBlockLoot(String id) {
        RESOURCE_PACK.addLootTable(
                new MIIdentifier("blocks/" + id),
                JLootTable.loot("minecraft:block").pool(
                        new JPool()
                                .rolls(1)
                                .entry(new JEntry().type("minecraft:item").name(MOD_ID + ":" + id))
                                .condition(new JCondition("minecraft:survives_explosion")))
        );
    }

    private void setupPackets() {
        ServerSidePacketRegistry.INSTANCE.register(MachinePackets.C2S.SET_AUTO_EXTRACT, MachinePackets.C2S.ON_SET_AUTO_EXTRACT);
        ServerSidePacketRegistry.INSTANCE.register(ForgeHammerPacket.SET_HAMMER, ForgeHammerPacket.ON_SET_HAMMER);
    }

    private void setupFuels() {
        FuelRegistry.INSTANCE.add(MIItem.ITEM_COKE, 6400);
        //FuelRegistry.INSTANCE.add(MIMaterials.coal.getItem("dust"), 1600);
        //FuelRegistry.INSTANCE.add(MIMaterials.coal.getItem("small_dust"), 160);
        //FuelRegistry.INSTANCE.add(MIMaterials.lignite_coal.getItem("lignite_coal"), 1600);
    }
}
