package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.fluid.FluidUnit;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.fluid.FluidNetwork;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkData;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.impl.*;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.WorldComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class MIPipes implements ModInitializer {
    public static final ComponentType<PipeNetworksComponent> PIPE_NETWORKS =
            ComponentRegistry.INSTANCE.registerIfAbsent(new MIIdentifier("pipe_networks"), PipeNetworksComponent.class)
                    .attach(WorldComponentCallback.EVENT, PipeNetworksComponentImpl::new);

    public static final Block BLOCK_PIPE = new PipeBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f));
    public static BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE_PIPE;

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new MIIdentifier("pipe"), BLOCK_PIPE);
        BLOCK_ENTITY_TYPE_PIPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("pipe"), BlockEntityType.Builder.create(PipeBlockEntity::new, BLOCK_PIPE).build(null));
        registerFluidPipeType("bronze",255 << 24 | 227 << 16 | 103 << 8, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("steel",255 << 24 | 63 << 16 | 63 << 8 | 63, FluidUnit.DROPS_PER_BUCKET);
        registerFluidPipeType("aluminium",255 << 24 | 63 << 16 | 202 << 8 | 255, FluidUnit.DROPS_PER_BUCKET);

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
    }

    public void registerFluidPipeType(String name, int color, int nodeCapacity) {
        // TODO: maybe save the objects somewhere?
        PipeNetworkType type = PipeNetworkType.register(
                new MIIdentifier("fluid_" + name),
                FluidNetwork::new,
                FluidNetworkNode::new,
                color
        );
        Item item = new PipeItem(
                new Item.Settings().group(ModernIndustrialization.ITEM_GROUP),
                type,
                new FluidNetworkData(Fluids.EMPTY, nodeCapacity)
        );
        Registry.register(Registry.ITEM, new MIIdentifier("pipe_fluid_" + name), item);
        PipeModelProvider.modelNames.add(new MIIdentifier("item/pipe_fluid_" + name));
    }
}
