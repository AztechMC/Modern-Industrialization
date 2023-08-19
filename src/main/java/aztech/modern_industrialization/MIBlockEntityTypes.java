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

import aztech.modern_industrialization.api.energy.EnergyApi;
import aztech.modern_industrialization.blocks.creativestorageunit.CreativeStorageUnitBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.CreativeBarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.creativetank.CreativeTankBlockEntity;
import aztech.modern_industrialization.definition.BlockDefinition;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Doesn't contain block entity types for machines!
 */
public class MIBlockEntityTypes {

    public static BlockEntityType<CreativeTankBlockEntity> CREATIVE_TANK;
    public static BlockEntityType<CreativeBarrelBlockEntity> CREATIVE_BARREL;
    public static BlockEntityType<CreativeStorageUnitBlockEntity> CREATIVE_STORAGE_UNIT;

    public static void init() {
        CREATIVE_TANK = register(MIBlock.CREATIVE_TANK, CreativeTankBlockEntity::new);
        CREATIVE_STORAGE_UNIT = register(MIBlock.CREATIVE_STORAGE_UNIT, CreativeStorageUnitBlockEntity::new);
        CREATIVE_BARREL = register(MIBlock.CREATIVE_BARREL, CreativeBarrelBlockEntity::new);

        // API registrations below
        FluidStorage.SIDED.registerSelf(CREATIVE_TANK);
        ItemStorage.SIDED.registerSelf(CREATIVE_BARREL);
        EnergyApi.SIDED.registerForBlockEntities((be, d) -> EnergyApi.CREATIVE, CREATIVE_STORAGE_UNIT);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(BlockDefinition<?> block, FabricBlockEntityTypeBuilder.Factory<T> factory) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, block.getId(),
                FabricBlockEntityTypeBuilder.create(factory, block.asBlock()).build());
    }
}
