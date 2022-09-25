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
package aztech.modern_industrialization.blocks.storage.tank.creativetank;

import aztech.modern_industrialization.MIBlock;
import aztech.modern_industrialization.MIBlockEntityTypes;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.TankItemUnbakedModel;
import aztech.modern_industrialization.blocks.storage.tank.TankRenderer;
import aztech.modern_industrialization.machines.models.MachineModelProvider;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

@Environment(EnvType.CLIENT)
public class CreativeTankClientSetup {
    public static void setupClient() {
        MachineModelProvider.register(new MIIdentifier("item/creative_tank"),
                new TankItemUnbakedModel("creative_tank"));
        BlockRenderLayerMap.INSTANCE.putBlock(MIBlock.CREATIVE_TANK_BLOCK.asBlock(), RenderType.cutout());
        BlockEntityRendererRegistry.register(MIBlockEntityTypes.CREATIVE_TANK,
                (BlockEntityRendererProvider<AbstractTankBlockEntity>) TankRenderer::new);
    }
}
