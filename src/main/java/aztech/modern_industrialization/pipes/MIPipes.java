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
package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.debug.DebugCommands;
import aztech.modern_industrialization.pipes.api.*;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetwork;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkData;
import aztech.modern_industrialization.pipes.electricity.ElectricityNetworkNode;
import aztech.modern_industrialization.pipes.fluid.FluidNetwork;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkData;
import aztech.modern_industrialization.pipes.fluid.FluidNetworkNode;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreenHandler;
import aztech.modern_industrialization.pipes.impl.*;
import aztech.modern_industrialization.pipes.item.ItemNetwork;
import aztech.modern_industrialization.pipes.item.ItemNetworkData;
import aztech.modern_industrialization.pipes.item.ItemNetworkNode;
import aztech.modern_industrialization.pipes.item.ItemPipeScreenHandler;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;

public class MIPipes {
    public static final MIPipes INSTANCE = new MIPipes();

    public static final Block BLOCK_PIPE = new PipeBlock(FabricBlockSettings.of(Material.METAL).destroyTime(4.0f));
    public static BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE_PIPE;
    private final Map<PipeNetworkType, PipeItem> pipeItems = new HashMap<>();

    public static final Map<PipeItem, CableTier> electricityPipeTier = new HashMap<>();

    public static final MenuType<ItemPipeScreenHandler> SCREEN_HANDLER_TYPE_ITEM_PIPE = ScreenHandlerRegistry
            .registerExtended(new MIIdentifier("item_pipe"), ItemPipeScreenHandler::new);
    public static final MenuType<FluidPipeScreenHandler> SCREEN_HANDLER_TYPE_FLUID_PIPE = ScreenHandlerRegistry
            .registerExtended(new MIIdentifier("fluid_pipe"), FluidPipeScreenHandler::new);

    public static final Set<ResourceLocation> PIPE_MODEL_NAMES = new HashSet<>();

    // TODO: move this to MIPipesClient ?
    private static PipeRenderer.Factory makeRenderer(List<String> sprites, boolean innerQuads) {
        return new PipeRenderer.Factory() {
            @Override
            public Collection<net.minecraft.client.resources.model.Material> getSpriteDependencies() {
                return sprites.stream().map(
                        n -> new net.minecraft.client.resources.model.Material(TextureAtlas.LOCATION_BLOCKS, new MIIdentifier("blocks/pipes/" + n)))
                        .collect(Collectors.toList());
            }

            @Override
            public PipeRenderer create(Function<net.minecraft.client.resources.model.Material, TextureAtlasSprite> textureGetter) {
                net.minecraft.client.resources.model.Material[] ids = sprites.stream()
                        .map(n -> new net.minecraft.client.resources.model.Material(TextureAtlas.LOCATION_BLOCKS,
                                new MIIdentifier("blocks/pipes/" + n)))
                        .toArray(net.minecraft.client.resources.model.Material[]::new);
                return new PipeMeshCache(textureGetter, ids, innerQuads);
            }
        };
    }

    private static final PipeRenderer.Factory ITEM_RENDERER = makeRenderer(Arrays.asList("item", "item_item", "item_in", "item_in_out", "item_out"),
            false);
    private static final PipeRenderer.Factory FLUID_RENDERER = makeRenderer(
            Arrays.asList("fluid", "fluid_item", "fluid_in", "fluid_in_out", "fluid_out"), true);
    private static final PipeRenderer.Factory ELECTRICITY_RENDERER = makeRenderer(Arrays.asList("electricity", "electricity_blocks"), false);

    public void setup() {
        Registry.register(Registry.BLOCK, new MIIdentifier("pipe"), BLOCK_PIPE);
        BLOCK_ENTITY_TYPE_PIPE = Registry.register(Registry.BLOCK_ENTITY_TYPE, new MIIdentifier("pipe"),
                FabricBlockEntityTypeBuilder.create(PipeBlockEntity::new, BLOCK_PIPE).build(null));

        for (PipeColor color : PipeColor.values()) {
            registerFluidPipeType(color);
            registerItemPipeType(color);
        }

        registerPackets();
        DebugCommands.init();
    }

    private void registerFluidPipeType(PipeColor color) {
        String pipeId = color.prefix + "fluid_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), (id, data) -> new FluidNetwork(id, data, 81000),
                FluidNetworkNode::new, color.color, true, FLUID_RENDERER);
        PipeItem item = new PipeItem(new Item.Properties().tab(ModernIndustrialization.ITEM_GROUP), type, new FluidNetworkData(FluidVariant.blank()));
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier(pipeId), item);
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/" + pipeId));
        MIItemTagProvider.generateTag(MITags.FLUID_PIPES, item);
    }

    private void registerItemPipeType(PipeColor color) {
        String pipeId = color.prefix + "item_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), ItemNetwork::new, ItemNetworkNode::new, color.color, true,
                ITEM_RENDERER);
        PipeItem item = new PipeItem(new Item.Properties().tab(ModernIndustrialization.ITEM_GROUP), type, new ItemNetworkData());
        pipeItems.put(type, item);
        Registry.register(Registry.ITEM, new MIIdentifier(pipeId), item);
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/" + pipeId));
        MIItemTagProvider.generateTag(MITags.ITEM_PIPES, item);
    }

    public void registerCableType(String name, int color, CableTier tier) {
        String cableId = name + "_cable";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(cableId), (id, data) -> new ElectricityNetwork(id, data, tier),
                ElectricityNetworkNode::new, color, false, ELECTRICITY_RENDERER);
        PipeItem item = new PipeItem(new Item.Properties().tab(ModernIndustrialization.ITEM_GROUP), type, new ElectricityNetworkData());
        pipeItems.put(type, item);
        electricityPipeTier.put(item, tier);
        Registry.register(Registry.ITEM, new MIIdentifier(cableId), item);
        PIPE_MODEL_NAMES.add(new MIIdentifier("item/" + cableId));
    }

    public PipeItem getPipeItem(PipeNetworkType type) {
        return pipeItems.get(type);
    }

    public void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST::handleC2S);
        ServerPlayNetworking.registerGlobalReceiver(PipePackets.SET_CONNECTION_TYPE, PipePackets.ON_SET_CONNECTION_TYPE::handleC2S);
        ServerPlayNetworking.registerGlobalReceiver(PipePackets.INCREMENT_PRIORITY, PipePackets.ON_INCREMENT_PRIORITY);
        ServerPlayNetworking.registerGlobalReceiver(PipePackets.SET_NETWORK_FLUID, PipePackets.ON_SET_NETWORK_FLUID::handleC2S);
    }
}
