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
import aztech.modern_industrialization.datagen.model.BaseModelProvider;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.ContainerItem;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.materials.part.TankPart;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MIBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MI.ID);
    public static final SortedMap<ResourceLocation, BlockDefinition<?>> BLOCK_DEFINITIONS = new TreeMap<>();

    public static void init(IEventBus modBus) {
        BLOCKS.register(modBus);
    }

    // @formatter:off
    // Forge hammer
    public static final BlockDefinition<ForgeHammerBlock> FORGE_HAMMER = block("Forge Hammer", "forge_hammer",
            BlockDefinitionParams.defaultStone().withBlockConstructor(ForgeHammerBlock::new).sortOrder(SortOrder.FORGE_HAMMER).noModel().destroyTime(6.0f).explosionResistance(1200)
                    .sound(SoundType.ANVIL));

    // Bronze stuff
    public static final BlockDefinition<TrashCanBlock> TRASH_CAN = block("Automatic Trash Can", "trash_can",
            BlockDefinitionParams.defaultStone().withBlockConstructor(TrashCanBlock::new).destroyTime(6.0f).explosionResistance(1200))
            .withBlockRegistrationEvent(TrashCanBlock::onRegister);

    // Other
    public static final BlockDefinition<Block> BASIC_MACHINE_HULL = block("Basic Machine Hull", MIBlockKeys.BASIC_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> ADVANCED_MACHINE_HULL = block("Advanced Machine Hull", MIBlockKeys.ADVANCED_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> TURBO_MACHINE_HULL = block("Turbo Machine Hull", MIBlockKeys.TURBO_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> HIGHLY_ADVANCED_MACHINE_HULL = block("Highly Advanced Machine Hull", MIBlockKeys.HIGHLY_ADVANCED_MACHINE_HULL.location().getPath());
    public static final BlockDefinition<Block> QUANTUM_MACHINE_HULL = block("Quantum Machine Hull", MIBlockKeys.QUANTUM_MACHINE_HULL.location().getPath(), BlockDefinitionParams.defaultStone().explosionResistance(6000f));

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
                    .noLootTable()
    )
            .withBlockRegistrationEvent((block, item) -> {
                MICapabilities.onEvent(event -> {
                    event.registerItem(Capabilities.FluidHandler.ITEM, (stack, vd) -> new ContainerItem.FluidHandler(stack, (TankItem) item), item);
                });
            });

    public static final BlockDefinition<BarrelBlock> CREATIVE_BARREL = block(
            "Creative Barrel",
            "creative_barrel",
            BlockDefinitionParams.defaultStone()
                    .withBlockConstructor((p) -> new BarrelBlock(CreativeBarrelBlockEntity::new, StorageBehaviour.creative()))
                    .withBlockItemConstructor(BarrelItem::new)
                    .withModel((block, gen) -> {
                        String name = gen.name(block);
                        gen.simpleBlock(block, gen.models().cubeColumn(name, gen.blockTexture(name + "_side"), gen.blockTexture(name + "_top")));
                    })
                    .withBlockEntityRendererItemModel()
                    .noLootTable()
    );


    public static final BlockDefinition<CreativeStorageUnitBlock> CREATIVE_STORAGE_UNIT = block("Creative Storage Unit",
            "creative_storage_unit", BlockDefinitionParams.defaultStone().withBlockConstructor(CreativeStorageUnitBlock::new));

    // Materials
    public static final BlockDefinition<Block> BLOCK_FIRE_CLAY_BRICKS = block("Fire Clay Bricks", "fire_clay_bricks",
            BlockDefinitionParams.of(BlockBehaviour.Properties.of().mapColor(STONE)).sortOrder(SortOrder.MATERIALS.and("fire_clay")).destroyTime(2.0f).explosionResistance(6.0f).requiresCorrectToolForDrops());

    // @formatter:on

    public static <T extends Block> BlockDefinition<T> block(
            String englishName, String id, BlockDefinitionParams<T> params) {

        var holder = BLOCKS.registerBlock(id, params.ctor, params.props);
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

    public static BlockDefinition<Block> blockExplosive(String englishName, String id) {
        return MIBlock.block(
                englishName,
                id,
                BlockDefinitionParams.of(
                        BlockBehaviour.Properties.of().mapColor(MapColor.FIRE).ignitedByLava().instabreak().sound(SoundType.GRASS))
                        .clearTags().noModel());

        // TODO : Datagen model
    }

    public static class BlockDefinitionParams<T extends Block> {

        public final BlockBehaviour.Properties props;
        public BiConsumer<Block, BaseModelProvider> modelGenerator;
        public BiConsumer<Item, ItemModelProvider> itemModelGenerator = (item, gen) -> {
        };
        @Nullable
        public MIBlockLoot blockLoot;
        public final ArrayList<TagKey<Block>> tags = new ArrayList<>();
        public SortOrder sortOrder = SortOrder.BLOCKS_OTHERS;

        public Function<BlockBehaviour.Properties, T> ctor;
        public BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor;

        protected BlockDefinitionParams(BlockBehaviour.Properties properties,
                Function<BlockBehaviour.Properties, T> ctor,
                BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor,
                BiConsumer<Block, BaseModelProvider> modelGenerator,
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
                    (block, modelGenerator) -> modelGenerator.simpleBlockWithItem(block, modelGenerator.cubeAll(block)),
                    new MIBlockLoot.DropSelf(),
                    List.of(BlockTags.NEEDS_STONE_TOOL, BlockTags.MINEABLE_WITH_PICKAXE));
        }

        public static BlockDefinitionParams<Block> defaultStone() {
            return of(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).destroyTime(4.0f).requiresCorrectToolForDrops());
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Function<BlockBehaviour.Properties, U> ctor) {
            return new BlockDefinitionParams<>(props, ctor, (BiFunction) this.blockItemCtor, this.modelGenerator, this.blockLoot, this.tags);
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Supplier<U> ctor) {
            return new BlockDefinitionParams<>(props,
                    p -> ctor.get(),
                    (BiFunction) this.blockItemCtor,
                    this.modelGenerator,
                    this.blockLoot,
                    this.tags);
        }

        public BlockDefinitionParams<T> withBlockItemConstructor(BiFunction<? super T, Item.Properties, BlockItem> blockItemCtor) {
            this.blockItemCtor = blockItemCtor;
            return this;
        }

        public BlockDefinitionParams<T> withModel(BiConsumer<Block, BaseModelProvider> modelGenerator) {
            this.modelGenerator = modelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withItemModel(BiConsumer<Item, ItemModelProvider> itemModelGenerator) {
            this.itemModelGenerator = itemModelGenerator;
            return this;
        }

        public BlockDefinitionParams<T> withBlockEntityRendererItemModel() {
            return withItemModel((item, gen) -> {
                var builder = gen.getBuilder(BuiltInRegistries.ITEM.getKey(item).toString())
                        .parent(new ModelFile.UncheckedModelFile("builtin/entity"));
                var transforms = builder.transforms();
                transforms.transform(ItemDisplayContext.GUI)
                        .rotation(30, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.625f, 0.625f, 0.625f);
                transforms.transform(ItemDisplayContext.GROUND)
                        .rotation(0, 0, 0)
                        .translation(0, 3, 0)
                        .scale(0.25f, 0.25f, 0.25f);
                transforms.transform(ItemDisplayContext.FIXED)
                        .rotation(0, 0, 0)
                        .translation(0, 0, 0)
                        .scale(0.5f, 0.5f, 0.5f);
                transforms.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                        .rotation(75, 45, 0)
                        .translation(0, 2.5f, 0)
                        .scale(0.375f, 0.375f, 0.375f);
                transforms.transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                        .rotation(0, 45, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f);
                transforms.transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                        .rotation(0, 225, 0)
                        .translation(0, 0, 0)
                        .scale(0.4f, 0.4f, 0.4f);
            });
        }

        public BlockDefinitionParams<T> withLoot(MIBlockLoot blockLoot) {
            this.blockLoot = blockLoot;
            return this;
        }

        public BlockDefinitionParams<T> noModel() {
            this.modelGenerator = (block, modelGenerator) -> modelGenerator.existingModelWithItem(block);
            // still creating the blockstate
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
