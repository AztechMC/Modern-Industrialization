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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.blocks.tank.*;
import aztech.modern_industrialization.machines.models.MachineModelProvider;
import aztech.modern_industrialization.materials.MaterialBuilder;
import aztech.modern_industrialization.textures.TextureManager;
import aztech.modern_industrialization.textures.coloramp.Coloramp;
import aztech.modern_industrialization.util.ResourceUtil;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

public class TankMaterialPart implements MaterialPart {
    private final String materialName;
    private final String idPath;
    private final String itemId;
    private final TankBlock block;
    private final TankItem item;
    private BlockEntityType<BlockEntity> blockEntityType;

    public static Function<MaterialBuilder.PartContext, MaterialPart> of(int bucketCapacity) {
        return ctx -> new TankMaterialPart(ctx.getMaterialName(), ctx.getColoramp(), bucketCapacity);
    }

    private TankMaterialPart(String materialName, Coloramp coloramp, int bucketCapacity) {
        this.materialName = materialName;
        this.idPath = materialName + "_tank";
        long capacity = FluidConstants.BUCKET * bucketCapacity;
        this.itemId = "modern_industrialization:" + idPath;
        BlockEntityProvider factory = (pos, state) -> new TankBlockEntity(blockEntityType, pos, state, capacity);
        this.block = new TankBlock(FabricBlockSettings.of(Material.METAL).hardness(4.0f), factory);
        this.item = new TankItem(block, new Item.Settings().group(ITEM_GROUP), 81000L * bucketCapacity);
    }

    @Override
    public String getPart() {
        return MIParts.TANK;
    }

    @Override
    public String getTaggedItemId() {
        return itemId;
    }

    @Override
    public String getItemId() {
        return itemId;
    }

    @Override
    public void register(MaterialBuilder.RegisteringContext context) {
        ModernIndustrialization.registerBlock(block, item, idPath, 0);
        ResourceUtil.appendWrenchable(new MIIdentifier(idPath));
        this.blockEntityType = Registry.register(Registry.BLOCK_ENTITY_TYPE, itemId,
                FabricBlockEntityTypeBuilder.create(block.factory::createBlockEntity, block).build(null));

        // Fluid API
        FluidStorage.SIDED.registerSelf(blockEntityType);
        this.item.registerItemApi();
    }

    @Override
    public void registerTextures(TextureManager textureManager) {
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void registerClient() {
        UnbakedModel tankModel = new TankModel(materialName);
        MachineModelProvider.register(new MIIdentifier("block/" + idPath), tankModel);
        MachineModelProvider.register(new MIIdentifier("item/" + idPath), tankModel);
        BlockEntityRendererRegistry.INSTANCE.register(blockEntityType, TankRenderer::new);
    }
}
