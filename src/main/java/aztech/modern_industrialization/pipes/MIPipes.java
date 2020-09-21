package aztech.modern_industrialization.pipes;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import aztech.modern_industrialization.api.CableTier;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetwork;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkData;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkNode;
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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static aztech.modern_industrialization.api.CableTier.*;
import static aztech.modern_industrialization.pipes.api.PipeConnectionType.*;

public class MIPipes implements ModInitializer {
    public static final MIPipes INSTANCE = new MIPipes();
    public static final ComponentType<PipeNetworksComponent> PIPE_NETWORKS =
            ComponentRegistry.INSTANCE.registerIfAbsent(new MIIdentifier("pipe_networks"), PipeNetworksComponent.class)
                    .attach(WorldComponentCallback.EVENT, PipeNetworksComponentImpl::new);

    public static final Block BLOCK_PIPE = new PipeBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
    public static BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE_PIPE;
    private Map<PipeNetworkType, Item> pipeItems = new HashMap<>();
    public static final ScreenHandlerType<ItemPipeScreenHandler> SCREN_HANDLER_TYPE_ITEM_PIPE = ScreenHandlerRegistry.registerExtended(new MIIdentifier("item_pipe"), ItemPipeScreenHandler::new);
    public static final Set<Identifier> PIPE_MODEL_NAMES = new HashSet<>();

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new MIIdentifier("pipe"), BLOCK_PIPE);
        BLOCK_ENTITY_TYPE_PIPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("pipe"), BlockEntityType.Builder.create(PipeBlockEntity::new, BLOCK_PIPE).build(null));
        registerFluidPipeType("gold",255 << 24 | 255 << 16 | 225 << 8 | 0, 1000);
        registerFluidPipeType("aluminum",255 << 24 | 63 << 16 | 202 << 8 | 255, 1000);
        registerFluidPipeType("steel",255 << 24 | 63 << 16 | 63 << 8 | 63, 1000);
        registerFluidPipeType("iron",255 << 24 | 240 << 16 | 240 << 8 | 240, 1000);
        registerFluidPipeType("bronze", 255 << 24 | 255 << 16 | 204 << 8, 1000);
        registerFluidPipeType("tin",255 << 24 | 203 << 16 | 228 << 8 | 228, 1000);
        registerFluidPipeType("copper",255 << 24 | 255 << 16 | 102 << 8, 1000);
        registerFluidPipeType("lead",255 << 24 | 0x4a2649, 1000);
        registerFluidPipeType("nickel",255 << 24 | 0xa9a9d4, 1000);
        registerFluidPipeType("silver",255 << 24 | 0x99ffff, 1000);
        registerFluidPipeType("electrum",255 << 24 | 0xefff5e, 1000);

        registerItemPipeType("gold",255 << 24 | 255 << 16 | 225 << 8 | 0);
        registerItemPipeType("aluminum",255 << 24 | 63 << 16 | 202 << 8 | 255);
        registerItemPipeType("steel",255 << 24 | 63 << 16 | 63 << 8 | 63);
        registerItemPipeType("iron",255 << 24 | 240 << 16 | 240 << 8 | 240);
        registerItemPipeType("bronze", 255 << 24 | 255 << 16 | 204 << 8);
        registerItemPipeType("tin",255 << 24 | 203 << 16 | 228 << 8 | 228);
        registerItemPipeType("copper",255 << 24 | 255 << 16 | 102 << 8);
        registerItemPipeType("lead",255 << 24 | 0x4a2649);
        registerItemPipeType("nickel",255 << 24 | 0xa9a9d4);
        registerItemPipeType("silver",255 << 24 | 0x99ffff);
        registerItemPipeType("electrum",255 << 24 | 0xefff5e);

        registerElectricityPipeType("tin", 255 << 24 | 203 << 16 | 228 << 8 | 228, LV);
        registerElectricityPipeType("copper", 255 << 24 | 255 << 16 | 102 << 8, LV);
        registerElectricityPipeType("cupronickel", 0xffe39680, MV);
        registerElectricityPipeType("electrum",255 << 24 | 0xefff5e, MV);

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for(World world : server.getWorlds()) {
                PIPE_NETWORKS.get(world).onServerTickStart();
            }
        });

        registerPackets();
    }

    public void registerFluidPipeType(String name, int color, int nodeCapacity) {
        // TODO: maybe save the objects somewhere?
        PipeNetworkType type = PipeNetworkType.register(
                new MIIdentifier("fluid_" + name),
                (id, data) -> new FluidNetwork(id, data, nodeCapacity),
                FluidNetworkNode::new,
                color,
                FLUID
        );
        Item item = new PipeItem(
                new Item.Settings().group(ModernIndustrialization.ITEM_GROUP),
                type,
                new FluidNetworkData(FluidKeys.EMPTY)
        );
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier("pipe_fluid_" + name), item);
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/pipe_fluid_" + name));
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
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/pipe_item_" + name));
    }

    public void registerElectricityPipeType(String name, int color, CableTier tier) {
        PipeNetworkType type = PipeNetworkType.register(
                new MIIdentifier("electricity_" + name),
                (id, data) -> new ElectricityNetwork(id, data, tier),
                ElectricityNetworkNode::new,
                color,
                ELECTRICITY
        );
        Item item = new PipeItem(
                new Item.Settings().group(ModernIndustrialization.ITEM_GROUP),
                type,
                new ElectricityNetworkData()
        );
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier("pipe_electricity_" + name), item);
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/pipe_electricity_" + name));
    }

    public Item getPipeItem(PipeNetworkType type) {
        return pipeItems.get(type);
    }

    public void registerPackets() {
        ServerSidePacketRegistry.INSTANCE.register(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST);
    }
}
