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
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.storage.tank.*;
import aztech.modern_industrialization.machines.models.MachineModelProvider;
import aztech.modern_industrialization.util.ResourceUtil;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class TankPart extends UnbuildablePart<Integer> {

    public TankPart() {
        super("tank");
    }

    @Override
    public MaterialPartBuilder builder(Integer bucketCapacity) {

        BlockEntityType<BlockEntity>[] refs = new BlockEntityType[1]; // evil hack
        long capacity = FluidConstants.BUCKET * bucketCapacity;

        return new RegularPart(key).withoutTextureRegister().withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {

            BlockEntityProvider factory = (pos, state) -> new TankBlockEntity(refs[0], pos, state, capacity);
            TankBlock block = new TankBlock(itemPath, (MIBlock b) -> new TankItem(b, new Item.Settings().group(ITEM_GROUP), capacity), factory);
            TankItem item = (TankItem) block.blockItem;

            ResourceUtil.appendWrenchable(new MIIdentifier(itemPath));
            refs[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, itemId,
                    FabricBlockEntityTypeBuilder.create(block.factory::createBlockEntity, block).build(null));

            // Fluid API
            FluidStorage.SIDED.registerSelf(refs[0]);
            item.registerItemApi();

        }).withClientRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            UnbakedModel tankModel = new TankModel(partContext.getMaterialName());
            MachineModelProvider.register(new MIIdentifier("block/" + itemPath), tankModel);
            MachineModelProvider.register(new MIIdentifier("item/" + itemPath), tankModel);
            BlockEntityRendererRegistry.INSTANCE.register(refs[0], TankRenderer::new);
        });
    }
}
