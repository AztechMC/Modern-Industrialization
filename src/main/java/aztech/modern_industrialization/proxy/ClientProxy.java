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
package aztech.modern_industrialization.proxy;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.blocks.storage.tank.TankModel;
import aztech.modern_industrialization.blocks.storage.tank.TankRenderer;
import aztech.modern_industrialization.machines.models.MachineModelProvider;
import aztech.modern_industrialization.materials.MaterialBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class ClientProxy extends CommonProxy {
    @Override
    public @Nullable PlayerEntity findUser(@Nullable LivingEntity entity) {
        if (Thread.currentThread().getName().equals("Render thread")) {
            // This is necessary for Magna's overlay
            return MinecraftClient.getInstance().player;
        }
        return super.findUser(entity);
    }

    @Override
    public void registerPartTankClient(MaterialBuilder.PartContext partContext, String itemPath, BlockEntityType<BlockEntity> blockEntityType) {
        UnbakedModel tankModel = new TankModel(partContext.getMaterialName());
        MachineModelProvider.register(new MIIdentifier("block/" + itemPath), tankModel);
        MachineModelProvider.register(new MIIdentifier("item/" + itemPath), tankModel);
        BlockEntityRendererRegistry.INSTANCE.register(blockEntityType, TankRenderer::new);
    }
}
