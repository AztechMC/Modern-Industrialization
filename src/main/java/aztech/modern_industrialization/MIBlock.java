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

import static aztech.modern_industrialization.ModernIndustrialization.METAL_MATERIAL;
import static aztech.modern_industrialization.ModernIndustrialization.STONE_MATERIAL;

import aztech.modern_industrialization.blocks.TrashCanBlock;
import aztech.modern_industrialization.blocks.creativestorageunit.CreativeStorageUnitBlock;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankBlock;
import aztech.modern_industrialization.blocks.creativetank.CreativeTankItem;
import aztech.modern_industrialization.blocks.forgehammer.ForgeHammerBlock;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.materials.part.TankPart;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

@SuppressWarnings("unused")
public class MIBlock {

    public static SortedMap<ResourceLocation, BlockDefinition<?>> BLOCKS = new TreeMap<>();

    // hull
    public static final BlockDefinition<Block> BASIC_MACHINE_HULL = block("Basic Machine Hull", "basic_machine_hull");
    public static final BlockDefinition<Block> ADVANCED_MACHINE_HULL = block("Advanced Machine Hull", "advanced_machine_hull");
    public static final BlockDefinition<Block> TURBO_MACHINE_HULL = block("Turbo Machine Hull", "turbo_machine_hull");
    public static final BlockDefinition<Block> HIGHLY_ADVANCED_MACHINE_HULL = block("Highly Advanced Machine Hull", "highly_advanced_machine_hull");
    public static final BlockDefinition<Block> QUANTUM_MACHINE_HULL = block("Quantum Machine Hull", "quantum_machine_hull",
            BlockDefinitionParams.of().resistance(6000f));

    // Multiblock
    public static final BlockDefinition<Block> FUSION_CHAMBER = block("Fusion Chamber", "fusion_chamber");

    // other
    public static final BlockDefinition<Block> INDUSTRIAL_TNT = blockExplosive("Industrial TNT", "industrial_tnt");
    public static final BlockDefinition<Block> NUKE = blockExplosive("Nuke", "nuke");

    public static final BlockDefinition<Block> BLOCK_FIRE_CLAY_BRICKS = block("Fire Clay Bricks", "fire_clay_bricks",
            BlockDefinitionParams.of(STONE_MATERIAL).destroyTime(2.0f).explosionResistance(6.0f).requiresCorrectToolForDrops());

    public static final BlockDefinition<ForgeHammerBlock> FORGE_HAMMER = block("Forge Hammer", "forge_hammer",
            BlockDefinitionParams.of().withBlockConstructor(ForgeHammerBlock::new).noModel().destroyTime(6.0f).explosionResistance(1200)
                    .sound(SoundType.ANVIL),
            ForgeHammerBlock.class);

    public static final BlockDefinition<TrashCanBlock> TRASH_CAN = block("Automatic Trash Can", "trash_can",
            BlockDefinitionParams.of().withBlockConstructor(TrashCanBlock::new).destroyTime(6.0f).explosionResistance(1200),
            TrashCanBlock.class)
                    .withBlockRegistrationEvent(TrashCanBlock::onRegister);

    public static final BlockDefinition<CreativeTankBlock> CREATIVE_TANK_BLOCK = block(
            "Creative Tank",
            "creative_tank",
            BlockDefinitionParams.of().withBlockConstructor(CreativeTankBlock::new)
                    .withBlockItemConstructor(CreativeTankItem::new)
                    .withModel(TankPart.MODEL_GENERATOR).noLootTable().clearTags()
                    .noOcclusion(),
            CreativeTankBlock.class

    ).withBlockRegistrationEvent(
            (block, item) -> FluidStorage.ITEM.registerForItems(CreativeTankItem.TankItemStorage::new, item));

    public static final BlockDefinition<CreativeStorageUnitBlock> CREATIVE_STORAGE_UNIT = block("Creative Storage Unit",
            "creative_storage_unit", BlockDefinitionParams.of().withBlockConstructor(CreativeStorageUnitBlock::new));

    private static <T extends Block> BlockDefinition<T> block(
            String englishName,
            String id,
            T block,
            BiFunction<Block, FabricItemSettings, BlockItem> blockItemCtor,
            BiConsumer<Block, BlockModelGenerators> modelGenerator,
            BiConsumer<Block, BlockLoot> lootTableGenerator,
            List<TagKey<Block>> tags) {
        BlockDefinition<T> definition = new BlockDefinition<>(englishName, id, block, blockItemCtor, modelGenerator, lootTableGenerator, tags);
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
                params.lootTableGenerator,
                params.tags);
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
        return MIBlock.block(englishName, id, BlockDefinitionParams.of());
    }

    public static BlockDefinition<Block> blockExplosive(String englishName, String id) {
        return MIBlock.block(
                englishName,
                id,
                BlockDefinitionParams.of(
                        BlockBehaviour.Properties.of(Material.EXPLOSIVE).instabreak().sound(SoundType.GRASS))
                        .clearTags().noModel());

        // TODO : Datagen model
    }

    public static class BlockDefinitionParams<T extends Block> extends FabricBlockSettings {

        public BiConsumer<Block, BlockModelGenerators> modelGenerator;
        public BiConsumer<Block, BlockLoot> lootTableGenerator;
        public final ArrayList<TagKey<Block>> tags = new ArrayList<>();

        public Function<BlockBehaviour.Properties, T> ctor;
        public BiFunction<Block, FabricItemSettings, BlockItem> blockItemCtor;

        protected BlockDefinitionParams(BlockBehaviour.Properties properties,
                Function<BlockBehaviour.Properties, T> ctor,
                BiFunction<Block, FabricItemSettings, BlockItem> blockItemCtor,
                BiConsumer<Block, BlockModelGenerators> modelGenerator,
                BiConsumer<Block, BlockLoot> lootTableGenerator,
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

        public static BlockDefinitionParams<Block> of(Material material) {
            return of(FabricBlockSettings.of(material).destroyTime(4.0f).requiresCorrectToolForDrops());
        }

        public static BlockDefinitionParams<Block> of() {
            return of(METAL_MATERIAL);
        }

        public <U extends Block> BlockDefinitionParams<U> withBlockConstructor(Function<BlockBehaviour.Properties, U> ctor) {
            return new BlockDefinitionParams<>(this, ctor, this.blockItemCtor, this.modelGenerator, this.lootTableGenerator, this.tags);
        }

        public BlockDefinitionParams<T> withBlockItemConstructor(BiFunction<Block, FabricItemSettings, BlockItem> blockItemCtor) {
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

        public BlockDefinitionParams<T> withLootTable(BiConsumer<Block, BlockLoot> lootTableGenerator) {
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

    }

}
