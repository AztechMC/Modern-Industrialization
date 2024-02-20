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

import aztech.modern_industrialization.MI;
import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.api.energy.CableTier;
import aztech.modern_industrialization.datagen.model.DelegatingModelBuilder;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
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
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.fluids.FluidType;

public class MIPipes {
    public static final MIPipes INSTANCE = new MIPipes();

    public static final Supplier<PipeBlock> BLOCK_PIPE = MIBlock.BLOCKS.register("pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(2.0f)));
    public static Supplier<BlockEntityType<PipeBlockEntity>> BLOCK_ENTITY_TYPE_PIPE;
    public static volatile boolean transparentCamouflage = false;
    private final Map<PipeNetworkType, Supplier<PipeItem>> pipeItems = new HashMap<>();

    public static final Map<PipeNetworkType, CableTier> ELECTRICITY_PIPE_TIER = new HashMap<>();

    public static final Supplier<MenuType<ItemPipeScreenHandler>> SCREEN_HANDLER_TYPE_ITEM_PIPE = MIRegistries.MENUS.register(
            "item_pipe",
            () -> IMenuTypeExtension.create(ItemPipeScreenHandler::new));
    public static final Supplier<MenuType<FluidPipeScreenHandler>> SCREEN_HANDLER_TYPE_FLUID_PIPE = MIRegistries.MENUS.register(
            "fluid_pipe",
            () -> IMenuTypeExtension.create(FluidPipeScreenHandler::new));

    public void setup() {
        BLOCK_ENTITY_TYPE_PIPE = MIRegistries.BLOCK_ENTITIES.register("pipe",
                () -> BlockEntityType.Builder.of(PipeBlockEntity::new, BLOCK_PIPE.get()).build(null));

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
    }

    public static final BiConsumer<Item, ItemModelProvider> ITEM_MODEL_GENERATOR = (item, modelGenerator) -> {
        // Delegate to block model
        modelGenerator.getBuilder(BuiltInRegistries.ITEM.getKey(item).getPath())
                .customLoader(DelegatingModelBuilder::new)
                .delegate(modelGenerator.getExistingFile(MI.id("block/pipe")))
                .end();
    };

    private void registerFluidPipeType(PipeColor color) {
        String pipeId = color.prefix + "fluid_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), (id, data) -> new FluidNetwork(id, data, FluidType.BUCKET_VOLUME),
                FluidNetworkNode::new, color.color, true);
        var itemDef = MIItem.item(
                color.englishNamePrefix + "Fluid Pipe",
                pipeId,
                prop -> new PipeItem(prop, type, new FluidNetworkData(FluidVariant.blank())),
                ITEM_MODEL_GENERATOR,
                SortOrder.PIPES);
        register(type, itemDef::asItem);
        TagsToGenerate.generateTag(MITags.FLUID_PIPES, itemDef, "Fluid Pipes");
    }

    private void registerItemPipeType(PipeColor color) {
        String pipeId = color.prefix + "item_pipe";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(pipeId), ItemNetwork::new, ItemNetworkNode::new, color.color, true);
        var itemDef = MIItem.item(
                color.englishNamePrefix + "Item Pipe",
                pipeId,
                prop -> new PipeItem(prop, type, new ItemNetworkData()),
                ITEM_MODEL_GENERATOR,
                SortOrder.PIPES);
        register(type, itemDef::asItem);
        TagsToGenerate.generateTag(MITags.ITEM_PIPES, itemDef, "Item Pipes");
    }

    public void registerCableType(String englishName, String name, int color, CableTier tier) {
        String cableId = name + "_cable";
        PipeNetworkType type = PipeNetworkType.register(new MIIdentifier(cableId), (id, data) -> new ElectricityNetwork(id, data, tier),
                ElectricityNetworkNode::new, color, false);
        var itemDef = MIItem.item(
                englishName,
                cableId,
                prop -> new PipeItem(prop, type, new ElectricityNetworkData()),
                ITEM_MODEL_GENERATOR,
                SortOrder.CABLES.and(tier));
        register(type, itemDef::asItem);
        ELECTRICITY_PIPE_TIER.put(type, tier);
    }

    public void register(PipeNetworkType type, Supplier<PipeItem> item) {
        if (pipeItems.containsKey(type)) {
            throw new IllegalStateException("Type " + type + " already registered");
        }

        pipeItems.put(type, item);
    }

    public PipeItem getPipeItem(PipeNetworkType type) {
        return pipeItems.get(type).get();
    }
}
