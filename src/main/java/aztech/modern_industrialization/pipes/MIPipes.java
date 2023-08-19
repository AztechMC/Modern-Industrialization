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

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.debug.DebugCommands;
import aztech.modern_industrialization.items.SortOrder;
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
import aztech.modern_industrialization.proxy.CommonProxy;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MIPipes {
    public static final MIPipes INSTANCE = new MIPipes();

    public static final Block BLOCK_PIPE = new PipeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(2.0f));
    public static BlockEntityType<PipeBlockEntity> BLOCK_ENTITY_TYPE_PIPE;
    private final Map<PipeNetworkType, PipeItem> pipeItems = new HashMap<>();

    public static final Map<PipeItem, CableTier> ELECTRICITY_PIPE_TIER = new HashMap<>();

    public static final MenuType<ItemPipeScreenHandler> SCREEN_HANDLER_TYPE_ITEM_PIPE = Registry.register(BuiltInRegistries.MENU,
            new MIIdentifier("item_pipe"),
            new ExtendedScreenHandlerType<>(ItemPipeScreenHandler::new));
    public static final MenuType<FluidPipeScreenHandler> SCREEN_HANDLER_TYPE_FLUID_PIPE = Registry.register(BuiltInRegistries.MENU,
            new MIIdentifier("fluid_pipe"),
            new ExtendedScreenHandlerType<>(FluidPipeScreenHandler::new));

    public static final Set<ResourceLocation> ITEM_PIPE_MODELS = new HashSet<>();

    public void setup() {
        Registry.register(BuiltInRegistries.BLOCK, new MIIdentifier("pipe"), BLOCK_PIPE);
        BLOCK_ENTITY_TYPE_PIPE = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new MIIdentifier("pipe"),
                FabricBlockEntityTypeBuilder.create(PipeBlockEntity::new, BLOCK_PIPE).build(null));

        for (PipeColor color : PipeColor.values()) {
            registerFluidPipeType(color);
        }
        for (PipeColor color : PipeColor.values()) {
            registerItemPipeType(color);
        }

        if (MIConfig.loadAe2Compat()) {
            try {
                Class.forName("aztech.modern_industrialization.compat.ae2.MIAEAddon")
                        .getMethod("onInitializePipes")
                        .invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        registerPackets();
        DebugCommands.init();
    }

    private void registerFluidPipeType(PipeColor color) {
        String pipeId = color.prefix + "fluid_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), (id, data) -> new FluidNetwork(id, data, 81000),
                FluidNetworkNode::new, color.color, true);
        var itemDef = MIItem.itemNoModel(color.englishNamePrefix + "Fluid Pipe", pipeId,
                prop -> new PipeItem(prop, type, new FluidNetworkData(FluidVariant.blank())), SortOrder.PIPES);
        var item = itemDef.asItem();
        pipeItems.put(type, item);
        ITEM_PIPE_MODELS.add(new MIIdentifier("item/" + pipeId));
        TagsToGenerate.generateTag(MITags.FLUID_PIPES, item, "Fluid Pipes");
    }

    private void registerItemPipeType(PipeColor color) {
        String pipeId = color.prefix + "item_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), ItemNetwork::new, ItemNetworkNode::new, color.color, true);
        var itemDef = MIItem.itemNoModel(color.englishNamePrefix + "Item Pipe", pipeId, prop -> new PipeItem(prop, type, new ItemNetworkData()),
                SortOrder.PIPES);
        var item = itemDef.asItem();
        pipeItems.put(type, item);
        ITEM_PIPE_MODELS.add(new MIIdentifier("item/" + pipeId));
        TagsToGenerate.generateTag(MITags.ITEM_PIPES, item, "Item Pipes");
    }

    public void registerCableType(String englishName, String name, int color, CableTier tier) {
        String cableId = name + "_cable";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(cableId), (id, data) -> new ElectricityNetwork(id, data, tier),
                ElectricityNetworkNode::new, color, false);
        var itemDef = MIItem.itemNoModel(englishName, cableId, prop -> new PipeItem(prop, type, new ElectricityNetworkData()),
                SortOrder.CABLES.and(tier));
        var item = itemDef.asItem();
        pipeItems.put(type, item);
        ELECTRICITY_PIPE_TIER.put(item, tier);
        ITEM_PIPE_MODELS.add(new MIIdentifier("item/" + cableId));
    }

    public void register(PipeNetworkType type, PipeItem item) {
        if (pipeItems.containsKey(type)) {
            throw new IllegalStateException("Type " + type + " already registered");
        }

        pipeItems.put(type, item);
    }

    public PipeItem getPipeItem(PipeNetworkType type) {
        return pipeItems.get(type);
    }

    public void registerPackets() {
        CommonProxy.INSTANCE.registerUnsidedPacket(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST);
        CommonProxy.INSTANCE.registerUnsidedPacket(PipePackets.SET_CONNECTION_TYPE, PipePackets.ON_SET_CONNECTION_TYPE);
        ServerPlayNetworking.registerGlobalReceiver(PipePackets.INCREMENT_PRIORITY, PipePackets.ON_INCREMENT_PRIORITY);
        CommonProxy.INSTANCE.registerUnsidedPacket(PipePackets.SET_NETWORK_FLUID, PipePackets.ON_SET_NETWORK_FLUID);
    }
}
