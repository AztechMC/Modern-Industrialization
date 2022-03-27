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

import static aztech.modern_industrialization.ModernIndustrialization.ITEM_GROUP;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MITags;
import aztech.modern_industrialization.blocks.storage.tank.*;
import aztech.modern_industrialization.datagen.tag.MIItemTagProvider;
import aztech.modern_industrialization.proxy.CommonProxy;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.mutable.MutableObject;

public class TankPart extends UnbuildablePart<Long> {

    public TankPart() {
        super("tank");
    }

    public RegularPart of(int bucketCapacity) {
        return of((long) bucketCapacity);
    }

    @Override
    public RegularPart of(Long bucketCapacity) {
        MutableObject<BlockEntityType<BlockEntity>> bet = new MutableObject<>();
        long capacity = FluidConstants.BUCKET * bucketCapacity;

        return new RegularPart(key).withoutTextureRegister().withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            EntityBlock factory = (pos, state) -> new TankBlockEntity(bet.getValue(), pos, state, capacity);
            TankBlock block = new TankBlock(itemPath, (MIBlock b) -> new TankItem(b, new Item.Properties().tab(ITEM_GROUP), capacity), factory);
            TankItem item = (TankItem) block.blockItem;
            MIItemTagProvider.generateTag(MITags.TANKS, item);

            bet.setValue(Registry.register(Registry.BLOCK_ENTITY_TYPE, itemId,
                    FabricBlockEntityTypeBuilder.create(block.factory::newBlockEntity, block).build(null)));

            // Fluid API
            FluidStorage.SIDED.registerSelf(bet.getValue());
            item.registerItemApi();
        }).withClientRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            CommonProxy.INSTANCE.registerPartTankClient(partContext, itemPath, bet.getValue());
        });
    }
}
