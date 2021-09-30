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
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlock;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelItem;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelRenderer;
import aztech.modern_industrialization.util.ResourceUtil;
import aztech.modern_industrialization.util.TextHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class BarrelPart extends UnbuildablePart<Integer> {

    public BarrelPart() {
        super("barrel");
    }

    @Override
    public BuildablePart of(Integer stackCapacity) {

        BlockEntityType<BlockEntity>[] refs = new BlockEntityType[1]; // evil hack

        return new RegularPart(key).asColumnBlock().withRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> {
            ResourceUtil.appendWrenchable(new MIIdentifier(itemPath));

            BlockEntityProvider factory = (pos, state) -> new BarrelBlockEntity(refs[0], pos, state, stackCapacity);
            BarrelBlock block = new BarrelBlock(itemPath, (MIBlock b) -> new BarrelItem(b, stackCapacity), factory);

            refs[0] = Registry.register(Registry.BLOCK_ENTITY_TYPE, itemId,
                    FabricBlockEntityTypeBuilder.create(block.factory::createBlockEntity, block).build(null));

            ItemStorage.SIDED.registerSelf(refs[0]);
        }).withClientRegister((registeringContext, partContext, part, itemPath, itemId, itemTag) -> BarrelRenderer.register(refs[0],
                TextHelper.getOverlayTextColor(partContext.getColoramp().getMeanRGB())));
    }

}
