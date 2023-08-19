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
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.StorageBehaviour;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelItem;
import aztech.modern_industrialization.datagen.tag.TagsToGenerate;
import aztech.modern_industrialization.definition.BlockDefinition;
import aztech.modern_industrialization.items.SortOrder;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public class BarrelPart implements PartKeyProvider {

    @Override
    public PartKey key() {
        return new PartKey("barrel");
    }

    public PartTemplate of(String nameOverride, String pathOverride, int stackCapacity) {
        return of(new PartEnglishNameFormatter.Overridden(nameOverride), (long) stackCapacity, pathOverride);
    }

    public PartTemplate of(long stackCapacity) {
        return of(new PartEnglishNameFormatter.Default("Barrel"), stackCapacity, null);
    }

    private PartTemplate of(PartEnglishNameFormatter englishNameFormatter, Long stackCapacity, @Nullable String maybeOverriddenPath) {
        MutableObject<BlockEntityType<BarrelBlockEntity>> bet = new MutableObject<>();

        PartTemplate template = new PartTemplate(englishNameFormatter, key()).asColumnBlock(SortOrder.BARRELS)
                .withRegister((partContext, part, itemPath, itemId, itemTag, englishName) -> {

                    StorageBehaviour<ItemVariant> barrelStorageBehaviour = BarrelBlock.withStackCapacity(stackCapacity);

                    EntityBlock factory = (pos, state) -> new BarrelBlockEntity(bet.getValue(), pos, state);

                    BlockDefinition<BarrelBlock> blockDefinition = MIBlock.block(
                            englishName,
                            itemPath,
                            MIBlock.BlockDefinitionParams.defaultStone()
                                    .withBlockConstructor(s -> new BarrelBlock(factory, barrelStorageBehaviour))
                                    .withBlockItemConstructor(BarrelItem::new)
                                    .withModel(TexturedModel.COLUMN)
                                    .withBlockEntityRendererItemModel()
                                    .noLootTable()
                                    .sortOrder(SortOrder.BARRELS.and(stackCapacity)));

                    TagsToGenerate.generateTag(MITags.BARRELS, blockDefinition.asItem(), "Barrels");
                    BarrelBlock block = blockDefinition.asBlock();
                    BarrelItem item = (BarrelItem) blockDefinition.asItem();

                    // noinspection unchecked,rawtypes
                    bet.setValue((BlockEntityType) Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, itemId,
                            FabricBlockEntityTypeBuilder.create(
                                    block.factory::newBlockEntity, block).build(null)));

                    ItemStorage.SIDED.registerSelf(bet.getValue());

                    CommonProxy.INSTANCE.registerPartBarrelClient(block, item, partContext.getMaterialName(), itemPath, bet.getValue(),
                            partContext.get(MEAN_RGB));
                });
        if (maybeOverriddenPath != null) {
            template = template.withCustomPath(maybeOverriddenPath);
        }
        return template;
    }

}
