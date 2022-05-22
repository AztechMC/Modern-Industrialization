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

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.tank.*;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.proxy.CommonProxy;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.mutable.MutableObject;

public class TankPart extends UnbuildablePart<Long> {
    public static final BiConsumer<Block, BlockModelGenerators> MODEL_GENERATOR = (block, gen) -> {
        var textureSlot = TextureSlot.create("0");
        var mapping = TextureMapping.singleSlot(textureSlot, new MIIdentifier("block/" + Registry.BLOCK.getKey(block).getPath()));
        gen.skipAutoItemBlock(block);
        gen.createTrivialBlock(block, mapping, new ModelTemplate(Optional.of(new MIIdentifier("base/tank")), Optional.empty(), textureSlot));
    };

    public TankPart() {
        super("tank");
    }

    public RegularPart of(int bucketCapacity) {
        return of("Tank", (long) bucketCapacity);
    }

    public RegularPart of(String englishNameFormatter, int bucketCapacity) {
        return of(englishNameFormatter, (long) bucketCapacity);
    }

    @Override
    public RegularPart of(Long bucketCapacity) {
        return of("Tank", bucketCapacity);
    }

    public RegularPart of(String englishNameFormatter, Long bucketCapacity) {
        MutableObject<BlockEntityType<BlockEntity>> bet = new MutableObject<>();
        long capacity = FluidConstants.BUCKET * bucketCapacity;

        return new RegularPart(englishNameFormatter, key)
                .asBlock()
                .withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
                    EntityBlock factory = (pos, state) -> new TankBlockEntity(bet.getValue(), pos, state, capacity);

                    String englishName = RegularPart.getEnglishName(englishNameFormatter, partContext.getEnglishName());

                    BlockDefinition<TankBlock> blockDefinition = MIBlock.block(
                            englishName,
                            itemPath,
                            MIBlock.BlockDefinitionParams.of().withBlockConstructor(
                                    s -> new TankBlock(factory)).withBlockItemConstructor(
                                            (b, s) -> new TankItem(b, capacity))
                                    .withModel(MODEL_GENERATOR)
                                    .noLootTable());

                    TankBlock block = blockDefinition.asBlock();
                    TankItem item = (TankItem) blockDefinition.asItem();

                    TagsToGenerate.generateTag(MITags.TANKS, item);

                    bet.setValue(Registry.register(Registry.BLOCK_ENTITY_TYPE, itemId,
                            FabricBlockEntityTypeBuilder.create(block.factory::newBlockEntity, block).build(null)));

                    // Fluid API
                    FluidStorage.SIDED.registerSelf(bet.getValue());
                    item.registerItemApi();

                    CommonProxy.INSTANCE.registerPartTankClient(block, partContext.getMaterialName(), itemPath, bet.getValue());
                });
    }
}
