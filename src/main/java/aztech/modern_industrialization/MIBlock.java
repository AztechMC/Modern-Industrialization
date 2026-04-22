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
import aztech.modern_industrialization.datagen.loot.MIBlockLoot;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.part.TankPart;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
public class MIBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MI.ID);
    public static final SortedMap<Identifier, BlockDefinition<?>> BLOCK_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    // @formatter:off
    // Forge hammer
    public static final BlockDefinition<ForgeHammerBlock> FORGE_HAMMER = block("Forge Hammer", "forge_hammer",
            BlockDefinitionParams.defaultStone().withBlockConstructor(ForgeHammerBlock::new).sortOrder(SortOrder.FORGE_HAMMER)
                    .withModel((block, gen) -> {
                        gen.createNonTemplateModelBlock(block);
                    })
                    .destroyTime(6.0f).explosionResistance(1200)
                    .sound(SoundType.ANVIL));

    // Bronze stuff
    public static final BlockDefinition<TrashCanBlock> TRASH_CAN = block("Automatic Trash Can", "trash_can",
            BlockDefinitionParams.defaultStone().withBlockConstructor(TrashCanBlock::new).destroyTime(6.0f).explosionResistance(1200).dontConductRedstone())
            .withBlockRegistrationEvent(TrashCanBlock::onRegister);

    // Other
    public static final BlockDefinition<Block> BASIC_MACHINE_HULL = nonConductorBlock("Basic Machine Hull", MIBlockKeys.BASIC_MACHINE_HULL.identifier().getPath());
    public static final BlockDefinition<Block> ADVANCED_MACHINE_HULL = nonConductorBlock("Advanced Machine Hull", MIBlockKeys.ADVANCED_MACHINE_HULL.identifier().getPath());
    public static final BlockDefinition<Block> TURBO_MACHINE_HULL = nonConductorBlock("Turbo Machine Hull", MIBlockKeys.TURBO_MACHINE_HULL.identifier().getPath());
    public static final BlockDefinition<Block> HIGHLY_ADVANCED_MACHINE_HULL = nonConductorBlock("Highly Advanced Machine Hull", MIBlockKeys.HIGHLY_ADVANCED_MACHINE_HULL.identifier().getPath());
    public static final BlockDefinition<Block> QUANTUM_MACHINE_HULL = block("Quantum Machine Hull", MIBlockKeys.QUANTUM_MACHINE_HULL.identifier().getPath(), BlockDefinitionParams.defaultStone().explosionResistance(6000f).dontConductRedstone());

    public static final BlockDefinition<Block> FUSION_CHAMBER = nonConductorBlock("Fusion Chamber", "fusion_chamber");
    public static final BlockDefinition<Block> INDUSTRIAL_TNT = blockExplosive("Industrial TNT", "industrial_tnt");
    public static final BlockDefinition<Block> NUKE = blockExplosive("Nuke", "nuke");

    public static final BlockDefinition<TankBlock> CREATIVE_TANK = block(
            "Creative Tank",
            "creative_tank",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor(p -> new TankBlock(p, CreativeTankBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(TankItem::new)
                    .withModel(TankPart.MODEL_GENERATOR)
                    .withBlockEntityRendererItemModel()
                    .noLootTable()
    )
            .withBlockRegistrationEvent((block, item) -> {
                MICapabilities.onEvent(event -> {
                    // TODO 26.1
//                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, vd) -> new ContainerItem.FluidHandler(stack, (TankItem) item), item);
                });
            });

    public static final BlockDefinition<BarrelBlock> CREATIVE_BARREL = block(
            "Creative Barrel",
            "creative_barrel",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor((p) -> new BarrelBlock(p, CreativeBarrelBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(BarrelItem::new)
                    .withModel((block, gen) -> {
                        gen.createTrivialBlock(block, TexturedModel.COLUMN);
                    })
                    .withBlockEntityRendererItemModel()
                    .noLootTable()
    );


    public static final BlockDefinition<CreativeStorageUnitBlock> CREATIVE_STORAGE_UNIT = block("Creative Storage Unit",
            "creative_storage_unit", BlockDefinitionParams.defaultStone().withBlockConstructor(CreativeStorageUnitBlock::new).dontConductRedstone());

    // Materials
    public static final BlockDefinition<Block> FIRE_CLAY_BRICKS = block("Fire Clay Bricks", "fire_clay_bricks",
            BlockDefinitionParams.of(BlockBehaviour.Properties.of().mapColor(STONE)).sortOrder(SortOrder.MATERIALS.and("fire_clay")).destroyTime(2.0f).explosionResistance(6.0f).requiresCorrectToolForDrops());

    // @formatter:on

    public static <T extends Block> BlockDefinition<T> block(
            String englishName, String id, BlockDefinitionParams<T> params) {
        var holder = BLOCKS.register(id, () -> params.ctor.apply(params.props.setId(ResourceKey.create(Registries.BLOCK, MI.id(id)))));
        var def = new BlockDefinition<>(
                englishName,
                holder,
                params.blockItemCtor,
                params.modelGenerator,
                params.itemModelGenerator,
                params.blockLoot,
                params.tags,
                params.sortOrder);
        BLOCK_DEFINITIONS.put(holder.getId(), def);
        return def;
    }

    public static BlockDefinition<Block> block(String englishName, String id) {
        return MIBlock.block(englishName, id, BlockDefinitionParams.defaultStone());
    }

    public static BlockDefinition<Block> nonConductorBlock(String englishName, String id) {
        return MIBlock.block(englishName, id, BlockDefinitionParams.defaultStone().dontConductRedstone());
    }

    public static BlockDefinition<Block> blockExplosive(String englishName, String id) {
        return MIBlock.block(
                englishName,
                id,
                BlockDefinitionParams.of(
                        BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).ignitedByLava().instabreak().sound(SoundType.GRASS))
                        .clearTags()
                        .withModel((block, gen) -> {
                            var mapping = new TextureMapping()
                                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(Blocks.TNT, "_top"))
                                    .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(Blocks.TNT, "_bottom"))
                                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block));

                            gen.createTrivialBlock(block, b -> new TexturedModel(mapping, ModelTemplates.CUBE_BOTTOM_TOP));
                        }));
    }

    // TODO: try to clean this up
    public static class BlockDefinitionParams<T extends Block> {
        public final BlockBehaviour.Properties props;
        // TODO: these need to go into the client sourceset
        public BiConsumer<Block, BlockModelGenerators> modelGenerator;
        public BiConsumer<Item, ItemModelGenerators> itemModelGenerator = (item, gen) -> {};
        @Nullable
        public MIBlockLoot blockLoot;
        public final ArrayList<TagKey<Block>> tags = new ArrayList<>();
        public SortOrder sortOrder = SortOrder.BLOCKS_OTHERS;

        public Function<BlockBehaviour.Properties, T> ctor;
        public BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor;

        protected BlockDefinitionParams(BlockBehaviour.Properties properties,
                Function<BlockBehaviour.Properties, T> ctor,
                BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor,
                BiConsumer<Block, BlockModelGenerators> modelGenerator,
                @Nullable MIBlockLoot blockLoot,
                List<TagKey<Block>> tags) {
            this.props = properties;
            this.ctor = ctor;
            this.blockItemCtor = blockItemCtor;
            this.modelGenerator = modelGenerator;
            this.blockLoot = blockLoot;
            this.tags.addAll(tags);
        }

        public static BlockDefinitionParams<Block> of() {
            return of(BlockBehaviour.Properties.of());
        }

        public static BlockDefinitionParams<Block> of(BlockBehaviour.Properties properties) {
            return new BlockDefinitionParams<>(properties, Block::new, BlockItem::new,
                    // TODO 26.1 - need to generate the item model as well?
                    (block, modelGenerator) -> modelGenerator.createTrivialCube(block),
                    new MIBlockLoot.DropSelf(),
                    List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE));
        }

        public static BlockDefinitionParams<Block> defaultStone() {
            return of(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops());
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Function<BlockBehaviour.Properties, U> ctor) {
            return new BlockDefinitionParams<>(props, ctor, (BiFunction) this.blockItemCtor, this.modelGenerator, this.blockLoot, this.tags);
        }

        public BlockDefinitionParams<T> withBlockItemConstructor(BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor) {
            this.blockItemCtor = blockItemCtor;
            return this;
        }

        public BlockDefinitionParams<T> withModel(BiConsumer<Block, BlockModelGenerators> modelGenerator) {
            this.modelGenerator = modelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withItemModel(BiConsumer<Item, ItemModelGenerators> itemModelGenerator) {
            this.itemModelGenerator = itemModelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withBlockEntityRendererItemModel() {
            return withItemModel((item, gen) -> {
                var baseBlockModel = BuiltInRegistries.ITEM.getKey(item).withPrefix("block/");
                try {
                    gen.itemModelOutput.accept(item, new CompositeModel.Unbaked(List.of(
                            new CuboidItemModelWrapper.Unbaked(baseBlockModel, Optional.empty(), List.of()),
                            // TODO: temporary reflection hack
                            new SpecialModelWrapper.Unbaked(baseBlockModel, Optional.empty(), (SpecialModelRenderer.Unbaked) Class.forName("aztech.modern_industrialization.client.util.UseBlockEntityRenderer$Unbaked").getConstructor().newInstance())),
                            Optional.empty()
                    ));
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
            // TODO 26.1: what about this stuff?
//            return withItemModel((item, gen) -> {
//                var builder = gen.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString())
//                        .parent(new ModelFile.UncheckedModelFile("builtin/entity"));
//                var transforms = builder.transforms();
//                transforms.transform(ItemDisplayContext.GUI)
//                        .rotation(30, 225, 0)
//                        .translation(0, 0, 0)
//                        .scale(0.625f, 0.625f, 0.625f);
//                transforms.transform(ItemDisplayContext.GROUND)
//                        .rotation(0, 0, 0)
//                        .translation(0, 3, 0)
//                        .scale(0.25f, 0.25f, 0.25f);
//                transforms.transform(ItemDisplayContext.FIXED)
//                        .rotation(0, 0, 0)
//                        .translation(0, 0, 0)
//                        .scale(0.5f, 0.5f, 0.5f);
//                transforms.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
//                        .rotation(75, 45, 0)
//                        .translation(0, 2.5f, 0)
//                        .scale(0.375f, 0.375f, 0.375f);
//                transforms.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
//                        .rotation(0, 45, 0)
//                        .translation(0, 0, 0)
//                        .scale(0.4f, 0.4f, 0.4f);
//                transforms.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
//                        .rotation(0, 225, 0)
//                        .translation(0, 0, 0)
//                        .scale(0.4f, 0.4f, 0.4f);
//            });
        }

        public BlockDefinitionParams<T> withLoot(MIBlockLoot blockLoot) {
            this.blockLoot = blockLoot;
            return this;
        }

        public BlockDefinitionParams<T> noLootTable() {
            this.blockLoot = null;
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

        // Bouncers to inner properties
        public BlockDefinitionParams<T> isValidSpawn(BlockBehaviour.StateArgumentPredicate<EntityType<?>> isValidSpawn) {
            this.props.isValidSpawn(isValidSpawn);
            return this;
        }

        public BlockDefinitionParams<T> dontConductRedstone() {
            this.props.isRedstoneConductor(Blocks::never);
            return this;
        }

        public BlockDefinitionParams<T> isRedstoneConductor(BlockBehaviour.StatePredicate isRedstoneConductor) {
            this.props.isRedstoneConductor(isRedstoneConductor);
            return this;
        }

        public BlockDefinitionParams<T> destroyTime(float destroyTime) {
            this.props.destroyTime(destroyTime);
            return this;
        }

        public BlockDefinitionParams<T> explosionResistance(float explosionResistance) {
            this.props.explosionResistance(explosionResistance);
            return this;
        }

        public BlockDefinitionParams<T> sound(SoundType soundType) {
            this.props.sound(soundType);
            return this;
        }

        public BlockDefinitionParams<T> requiresCorrectToolForDrops() {
            this.props.requiresCorrectToolForDrops();
            return this;
        }
    }
}
