package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.fluid.FluidNetwork;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkData;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.impl.*;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.pipes.item.ItemNetworkData;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static aztech.modern_industrialization.pipes.api.PipeConnectionType.FLUID;
import static aztech.modern_industrialization.pipes.api.PipeConnectionType.ITEM;

public class MIPipes implements ModInitializer {
    public static final MIPipes INSTANCE = new MIPipes();
    public static final ComponentType<PipeNetworksComponent> PIPE_NETWORKS =
            ComponentRegistry.INSTANCE.registerIfAbsent(new MIIdentifier("pipe_networks"), PipeNetworksComponent.class)
                    .attach(WorldComponentCallback.EVENT, PipeNetworksComponentImpl::new);

    public static final Block BLOCK_PIPE = new PipeBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
    public static BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE_PIPE;
    private Map<PipeNetworkType, Item> pipeItems = new HashMap<>();
    public static final ScreenHandlerType<ItemPipeScreenHandler> SCREN_HANDLER_TYPE_ITEM_PIPE = ScreenHandlerRegistry.registerExtended(new MIIdentifier("item_pipe"), ItemPipeScreenHandler::new);

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new MIIdentifier("pipe"), BLOCK_PIPE);
        BLOCK_ENTITY_TYPE_PIPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("pipe"), BlockEntityType.Builder.create(PipeBlockEntity::new, BLOCK_PIPE).build(null));
        registerFluidPipeType("gold",255 << 24 | 255 << 16 | 225 << 8 | 0, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("aluminum",255 << 24 | 63 << 16 | 202 << 8 | 255, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("steel",255 << 24 | 63 << 16 | 63 << 8 | 63, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("iron",255 << 24 | 240 << 16 | 240 << 8 | 240, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("bronze", 255 << 24 | 255 << 16 | 204 << 8, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("tin",255 << 24 | 203 << 16 | 228 << 8 | 228, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("copper",255 << 24 | 255 << 16 | 102 << 8, FluidUnit.DROPS_PER_BUCKET);

        registerItemPipeType("gold",255 << 24 | 255 << 16 | 225 << 8 | 0);
        registerItemPipeType("aluminum",255 << 24 | 63 << 16 | 202 << 8 | 255);
        registerItemPipeType("steel",255 << 24 | 63 << 16 | 63 << 8 | 63);
        registerItemPipeType("iron",255 << 24 | 240 << 16 | 240 << 8 | 240);
        registerItemPipeType("bronze", 255 << 24 | 255 << 16 | 204 << 8);
        registerItemPipeType("tin",255 << 24 | 203 << 16 | 228 << 8 | 228);
        registerItemPipeType("copper",255 << 24 | 255 << 16 | 102 << 8);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for(World world : server.getWorlds()) {
                PIPE_NETWORKS.get(world).onServerTickStart();
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            for(BlockEntity entity : world.tickingBlockEntities) {
                if(entity instanceof PipeBlockEntity) {
                    PipeBlockEntity pipeEntity = (PipeBlockEntity) entity;
                    pipeEntity.onChunkUnload();
                }
            }
        });

        registerPackets();
    }

    public void registerFluidPipeType(String name, int color, int nodeCapacity) {
        // TODO: maybe save the objects somewhere?
        PipeNetworkType type = PipeNetworkType.register(
                new MIIdentifier("fluid_" + name),
                FluidNetwork::new,
                FluidNetworkNode::new,
                color,
                FLUID
        );
        Item item = new PipeItem(
                new Item.Settings().group(ModernIndustrialization.ITEM_GROUP),
                type,
                new FluidNetworkData(Fluids.EMPTY, nodeCapacity)
        );
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier("pipe_fluid_" + name), item);
        PipeModelProvider.modelNames.add(new MIIdentifier("item/pipe_fluid_" + name));
    }

    public void registerItemPipeType(String name, int color) {
        PipeNetworkType type = PipeNetworkType.register(
                new MIIdentifier("item_" + name),
                ItemNetwork::new,
                ItemNetworkNode::new,
                color,
                ITEM
        );
        Item item = new PipeItem(
                new Item.Settings().group(ModernIndustrialization.ITEM_GROUP),
                type,
                new ItemNetworkData()
        );
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier("pipe_item_" + name), item);
        PipeModelProvider.modelNames.add(new MIIdentifier("item/pipe_item_" + name));
    }

    public Item getPipeItem(PipeNetworkType type) {
        return pipeItems.get(type);
    }

    public void registerPackets() {
        ServerSidePacketRegistry.INSTANCE.register(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST);
    }
}
