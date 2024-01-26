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
import aztech.modern_industrialization.MICapabilities;
import aztech.modern_industrialization.MIRegistries;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.blocks.storage.tank.*;
import aztech.modern_industrialization.datagen.model.BaseModelProvider;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.ContainerItem;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.proxy.CommonProxy;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.bridge.SlotFluidHandler;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import java.util.function.BiConsumer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class TankPart implements PartKeyProvider {

    public static final BiConsumer<Block, BaseModelProvider> MODEL_GENERATOR = (block, gen) -> {
        gen.simpleBlock(block, gen.models()
                .getBuilder(gen.blockTexture(block).getPath())
                .parent(gen.models().getExistingFile(gen.modLoc("base/tank")))
                .texture("0", gen.blockTexture(block).toString()));
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
        long capacity = FluidType.BUCKET_VOLUME * bucketCapacity;

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

                    TagsToGenerate.generateTag(MITags.TANKS, blockDefinition, "Tanks");

                    MIRegistries.BLOCK_ENTITIES.register(itemPath, () -> {
                        var ret = BlockEntityType.Builder.of(factory::newBlockEntity, blockDefinition.asBlock()).build(null);
                        // noinspection unchecked,rawtypes
                        bet.setValue((BlockEntityType) ret);
                        return ret;
                    });

                    MICapabilities.onEvent(event -> {
                        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, bet.getValue(), (be, side) -> new SlotFluidHandler(be));

                        var item = (TankItem) blockDefinition.asItem();
                        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ignored) -> new ContainerItem.FluidHandler(stack, item), item);
                    });

                    CommonProxy.INSTANCE.registerPartTankClient(bet::getValue, partContext.get(MEAN_RGB));
                });

        if (maybePathOverridden != null) {
            tank = tank.withCustomPath(maybePathOverridden);
        }

        return tank;
    }

}
