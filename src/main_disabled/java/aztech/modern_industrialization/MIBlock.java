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

import static net.minecraft.world.level.material.MapColor.STONE;

import aztech.modern_industrialization.blocks.TrashCanBlock;
import aztech.modern_industrialization.blocks.creativestorageunit.CreativeStorageUnitBlock;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerBlock;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelItem;
import aztech.modern_industrialization.blocks.storage.barrel.CreativeBarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.TankBlock;
import aztech.modern_industrialization.blocks.storage.tank.TankItem;
import aztech.modern_industrialization.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.part.TankPart;
import com.google.gson.JsonParser;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

@SuppressWarnings("unused")
public class MIBlock {

    public static SortedMap<ResourceLocation, BlockDefinition<?>> BLOCKS = new TreeMap<>();

    // @formatter:off
    // Forge hammer
    public static final BlockDefinition<ForgeHammerBlock> FORGE_HAMMER = block("Forge Hammer", "forge_hammer",
            BlockDefinitionParams.defaultStone().withBlockConstructor(ForgeHammerBlock::new).sortOrder(SortOrder.FORGE_HAMMER).noModel().destroyTime(6.0f).explosionResistance(1200)
                    .sound(SoundType.ANVIL),
            ForgeHammerBlock.class);

    // Bronze stuff
    public static final BlockDefinition<TrashCanBlock> TRASH_CAN = block("Automatic Trash Can", "trash_can",
            BlockDefinitionParams.defaultStone().withBlockConstructor(TrashCanBlock::new).destroyTime(6.0f).explosionResistance(1200),
            TrashCanBlock.class)
            .withBlockRegistrationEvent(TrashCanBlock::onRegister);

    // Other
    public static final BlockDefinition<Block> BASIC_MACHINE_HULL = block("Basic Machine Hull", MIBlockKeys.BASIC_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> ADVANCED_MACHINE_HULL = block("Advanced Machine Hull", MIBlockKeys.ADVANCED_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> TURBO_MACHINE_HULL = block("Turbo Machine Hull", MIBlockKeys.TURBO_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> HIGHLY_ADVANCED_MACHINE_HULL = block("Highly Advanced Machine Hull", MIBlockKeys.HIGHLY_ADVANCED_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> QUANTUM_MACHINE_HULL = block("Quantum Machine Hull", MIBlockKeys.QUANTUM_MACHINE_HULL.location().getPath(), BlockDefinitionParams.defaultStone().resistance(6000f));

    public static final BlockDefinition<Block> FUSION_CHAMBER = block("Fusion Chamber", "fusion_chamber");
    public static final BlockDefinition<Block> INDUSTRIAL_TNT = blockExplosive("Industrial TNT", "industrial_tnt");
    public static final BlockDefinition<Block> NUKE = blockExplosive("Nuke", "nuke");

    public static final BlockDefinition<TankBlock> CREATIVE_TANK = block(
            "Creative Tank",
            "creative_tank",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor(() -> new TankBlock(CreativeTankBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(TankItem::new)
                    .withModel(TankPart.MODEL_GENERATOR)
                    .withBlockEntityRendererItemModel()
                    .noLootTable(),
            TankBlock.class
    ).withBlockRegistrationEvent(
            (block, item) -> ((TankItem) item).registerItemApi());


    public static final BlockDefinition<BarrelBlock> CREATIVE_BARREL = block(
            "Creative Barrel",
            "creative_barrel",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor((p) -> new BarrelBlock(CreativeBarrelBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(BarrelItem::new)
                    .withModel(TexturedModel.COLUMN)
                    .withBlockEntityRendererItemModel()
                    .noLootTable(),
            BarrelBlock.class
    );


    public static final BlockDefinition<CreativeStorageUnitBlock> CREATIVE_STORAGE_UNIT = block("Creative Storage Unit",
            "creative_storage_unit", BlockDefinitionParams.defaultStone().withBlockConstructor(CreativeStorageUnitBlock::new));

    // Materials
    public static final BlockDefinition<Block> BLOCK_FIRE_CLAY_BRICKS = block("Fire Clay Bricks", "fire_clay_bricks",
            BlockDefinitionParams.of(BlockBehaviour.Properties.of().mapColor(STONE)).sortOrder(SortOrder.MATERIALS.and("fire_clay")).destroyTime(2.0f).explosionResistance(6.0f).requiresCorrectToolForDrops());

    // @formatter:on

    private static <T extends Block> BlockDefinition<T> block(
            String englishName,
            String id,
            T block,
            BiFunction<? super T, FabricItemSettings, BlockItem> blockItemCtor,
            BiConsumer<Block, BlockModelGenerators> modelGenerator,
            BiConsumer<Item, ItemModelGenerators> itemModelGenerator,
            BiConsumer<Block, BlockLootSubProvider> lootTableGenerator,
            List<TagKey<Block>> tags,
            SortOrder sortOrder) {
        BlockDefinition<T> definition = new BlockDefinition<>(englishName, id, block, blockItemCtor, modelGenerator, itemModelGenerator,
                lootTableGenerator, tags,
                sortOrder);
        if (BLOCKS.put(definition.getId(), definition) != null) {
            throw new IllegalArgumentException("Block id already taken : " + definition.getId());
        }

        return definition;

    }

    public static <T extends Block> BlockDefinition<T> block(String englishName, String id,
            BlockDefinitionParams<T> params) {
        return block(englishName, id,
                params.ctor.apply(params),
                params.blockItemCtor,
                params.modelGenerator,
                params.itemModelGenerator,
                params.lootTableGenerator,
                params.tags,
                params.sortOrder);
    }

    public static BlockDefinition<Block> block(String englishName, String id,
            BlockBehaviour.Properties params) {
        return block(englishName, id, params, Block.class);
    }

    public static <T extends Block> BlockDefinition<T> block(String englishName, String id,
            BlockBehaviour.Properties params,
            Class<T> blockClass) {
        return block(englishName, id, (BlockDefinitionParams<T>) params);
    }

    public static BlockDefinition<Block> block(String englishName, String id) {
        return MIBlock.block(englishName, id, BlockDefinitionParams.defaultStone());
    }

    public static BlockDefinition<Block> blockExplosive(String englishName, String id) {
        return MIBlock.block(
                englishName,
                id,
                BlockDefinitionParams.of(
                        BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).ignitedByLava().instabreak().sound(SoundType.GRASS))
                        .clearTags().noModel());

        // TODO : Datagen model
    }

    public static class BlockDefinitionParams<T extends Block> extends FabricBlockSettings {

        public BiConsumer<Block, BlockModelGenerators> modelGenerator;
        public BiConsumer<Item, ItemModelGenerators> itemModelGenerator = (item, gen) -> {
        };
        public BiConsumer<Block, BlockLootSubProvider> lootTableGenerator;
        public final ArrayList<TagKey<Block>> tags = new ArrayList<>();
        public SortOrder sortOrder = SortOrder.BLOCKS_OTHERS;

        public Function<BlockBehaviour.Properties, T> ctor;
        public BiFunction<? super T, FabricItemSettings, BlockItem> blockItemCtor;

        protected BlockDefinitionParams(BlockBehaviour.Properties properties,
                Function<BlockBehaviour.Properties, T> ctor,
                BiFunction<? super T, FabricItemSettings, BlockItem> blockItemCtor,
                BiConsumer<Block, BlockModelGenerators> modelGenerator,
                BiConsumer<Block, BlockLootSubProvider> lootTableGenerator,
                List<TagKey<Block>> tags) {
            super(properties);
            this.ctor = ctor;
            this.blockItemCtor = blockItemCtor;
            this.modelGenerator = modelGenerator;
            this.lootTableGenerator = lootTableGenerator;
            this.tags.addAll(tags);
        }

        public static BlockDefinitionParams<Block> of(BlockBehaviour.Properties properties) {
            return new BlockDefinitionParams<>(properties, Block::new, BlockItem::new,
                    (block, modelGenerator) -> modelGenerator.createTrivialCube(block),
                    (block, lootGenerator) -> lootGenerator.dropSelf(block),
                    List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE));
        }

        public static BlockDefinitionParams<Block> defaultStone() {
            return of(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops());
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Function<BlockBehaviour.Properties, U> ctor) {
            return new BlockDefinitionParams<>(this, ctor, (BiFunction) this.blockItemCtor, this.modelGenerator, this.lootTableGenerator, this.tags);
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Supplier<U> ctor) {
            return new BlockDefinitionParams<>(this,
                    p -> ctor.get(),
                    (BiFunction) this.blockItemCtor,
                    this.modelGenerator,
                    this.lootTableGenerator,
                    this.tags);
        }

        public BlockDefinitionParams<T> withBlockItemConstructor(BiFunction<? super T, FabricItemSettings, BlockItem> blockItemCtor) {
            this.blockItemCtor = blockItemCtor;
            return this;
        }

        public BlockDefinitionParams<T> withModel(BiConsumer<Block, BlockModelGenerators> modelGenerator) {
            this.modelGenerator = modelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withModel(TexturedModel.Provider model) {
            return this.withModel((block, blockModelGenerator) -> blockModelGenerator.createTrivialBlock(block, model));
        }

        public BlockDefinitionParams<T> withItemModel(BiConsumer<Item, ItemModelGenerators> itemModelGenerator) {
            this.itemModelGenerator = itemModelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withBlockEntityRendererItemModel() {
            var currentModel = this.modelGenerator;
            return withModel((block, gen) -> {
                currentModel.accept(block, gen);
                // Skip default item model
                gen.skipAutoItemBlock(block);
            }).withItemModel((item, gen) -> {
                // We need the builtin/entity parent and the proper transforms (copied from block/block.json from vanilla)
                gen.output.accept(ModelLocationUtils.getModelLocation(item), () -> {
                    var json = JsonParser.parseString("""
                            {
                                "display": {
                                    "gui": {
                                        "rotation": [ 30, 225, 0 ],
                                        "translation": [ 0, 0, 0],
                                        "scale":[ 0.625, 0.625, 0.625 ]
                                    },
                                    "ground": {
                                        "rotation": [ 0, 0, 0 ],
                                        "translation": [ 0, 3, 0],
                                        "scale":[ 0.25, 0.25, 0.25 ]
                                    },
                                    "fixed": {
                                        "rotation": [ 0, 0, 0 ],
                                        "translation": [ 0, 0, 0],
                                        "scale":[ 0.5, 0.5, 0.5 ]
                                    },
                                    "thirdperson_righthand": {
                                        "rotation": [ 75, 45, 0 ],
                                        "translation": [ 0, 2.5, 0],
                                        "scale": [ 0.375, 0.375, 0.375 ]
                                    },
                                    "firstperson_righthand": {
                                        "rotation": [ 0, 45, 0 ],
                                        "translation": [ 0, 0, 0 ],
                                        "scale": [ 0.40, 0.40, 0.40 ]
                                    },
                                    "firstperson_lefthand": {
                                        "rotation": [ 0, 225, 0 ],
                                        "translation": [ 0, 0, 0 ],
                                        "scale": [ 0.40, 0.40, 0.40 ]
                                    }
                                }
                            }
                                                        """).getAsJsonObject();
                    json.addProperty("parent", "builtin/entity");
                    return json;
                });
            });
        }

        public BlockDefinitionParams<T> withLootTable(BiConsumer<Block, BlockLootSubProvider> lootTableGenerator) {
            this.lootTableGenerator = lootTableGenerator;
            return this;
        }

        public BlockDefinitionParams<T> noModel() {
            this.modelGenerator = (block, modelGenerator) -> modelGenerator.createNonTemplateModelBlock(block);
            // still creating the blockstate
            return this;
        }

        public BlockDefinitionParams<T> noLootTable() {
            this.lootTableGenerator = null;
            return this;
        }

        public BlockDefinitionParams<T> clearTags() {
            this.tags.clear();
            return this;
        }

        public BlockDefinitionParams<T> addMoreTags(TagKey<Block>... tagsToAdd) {
            return this.addMoreTags(Arrays.asList(tagsToAdd));
        }

        public BlockDefinitionParams<T> addMoreTags(Collection<TagKey<Block>> tagsToAdd) {
            this.tags.addAll(tagsToAdd);
            return this;
        }

        public BlockDefinitionParams<T> sortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }
    }

}
