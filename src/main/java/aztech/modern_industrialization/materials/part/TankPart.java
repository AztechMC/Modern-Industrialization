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
package aztech.modern_industrialization.materials.part;

import static aztech.modern_industrialization.materials.property.MaterialProperty.MEAN_RGB;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.blocks.storage.tank.*;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.proxy.CommonProxy;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class TankPart implements PartKeyProvider {

    public static final BiConsumer<Block, BlockModelGenerators> MODEL_GENERATOR = (block, gen) -> {
        var textureSlot = TextureSlot.create("0");
        var mapping = TextureMapping.singleSlot(textureSlot, new MIIdentifier("block/" + BuiltInRegistries.BLOCK.getKey(block).getPath()));
        gen.createTrivialBlock(block, mapping, new ModelTemplate(Optional.of(new MIIdentifier("base/tank")), Optional.empty(), textureSlot));
    };

    @Override
    public PartKey key() {
        return new PartKey("tank");
    }

    public PartTemplate of(String nameOverride, String path, int bucketCapacity) {
        return of(new PartEnglishNameFormatter.Overridden(nameOverride), bucketCapacity, path);
    }

    public PartTemplate of(long bucketCapacity) {
        return of(new PartEnglishNameFormatter.Default("Tank"), bucketCapacity, null);
    }

    public PartTemplate of(PartEnglishNameFormatter englishNameFormatter, long bucketCapacity, @Nullable String maybePathOverridden) {
        MutableObject<BlockEntityType<AbstractTankBlockEntity>> bet = new MutableObject<>();
        long capacity = FluidConstants.BUCKET * bucketCapacity;

        PartTemplate tank = new PartTemplate(englishNameFormatter, key())
                .asBlock(SortOrder.TANKS, new TextureGenParams.SimpleRecoloredBlock())
                .withRegister((partContext, part, itemPath, itemId, itemTag, englishName) -> {

                    StorageBehaviour<FluidVariant> tankStorageBehaviour = StorageBehaviour.uniformQuantity(capacity);

                    EntityBlock factory = (pos, state) -> new TankBlockEntity(bet.getValue(), pos, state);

                    BlockDefinition<TankBlock> blockDefinition = MIBlock.block(
                            englishName,
                            itemPath,
                            MIBlock.BlockDefinitionParams.defaultStone()
                                    .withBlockConstructor(s -> new TankBlock(factory, tankStorageBehaviour))
                                    .withBlockItemConstructor(TankItem::new)
                                    .withModel(MODEL_GENERATOR)
                                    .withBlockEntityRendererItemModel()
                                    .noLootTable()
                                    .sortOrder(SortOrder.TANKS.and(bucketCapacity)));

                    TankBlock block = blockDefinition.asBlock();
                    TankItem item = (TankItem) blockDefinition.asItem();

                    TagsToGenerate.generateTag(MITags.TANKS, item, "Tanks");

                    // noinspection unchecked,rawtypes
                    bet.setValue((BlockEntityType) Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, itemId,
                            FabricBlockEntityTypeBuilder.create(
                                    block.factory::newBlockEntity, block).build(null)));
                    // Fluid API
                    FluidStorage.SIDED.registerSelf(bet.getValue());
                    item.registerItemApi();

                    CommonProxy.INSTANCE.registerPartTankClient(block, item, partContext.getMaterialName(), itemPath, bet.getValue(),
                            partContext.get(MEAN_RGB));
                });

        if (maybePathOverridden != null) {
            tank = tank.withCustomPath(maybePathOverridden);
        }

        return tank;
    }

}
