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

import aztech.modern_industrialization.MIClient;
import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.barrel.BarrelRenderer;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.TankRenderer;
import aztech.modern_industrialization.items.SteamDrillHooks;
import aztech.modern_industrialization.machines.gui.MachineMenuClient;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.machines.models.MachineBakedModel;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.models.UseBlockModelBakedModel;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.textures.TextureHelper;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class ClientProxy extends CommonProxy {
    @Override
    public Player getClientPlayer() {
        return Objects.requireNonNull(Minecraft.getInstance().player);
    }

    @Override
    public @Nullable Player findUser(ItemStack mainHand) {
        if (Minecraft.getInstance().isSameThread()) {
            for (var player : Minecraft.getInstance().level.players()) {
                if (player.getMainHandItem() == mainHand) {
                    return player;
                }
            }
            return null;
        }
        return super.findUser(mainHand);
    }

    @Override
    public boolean shouldSteamDrillForceBreakReset() {
        if (Minecraft.getInstance().isSameThread()) {
            if (Minecraft.getInstance().hitResult instanceof BlockHitResult bhr) {
                return bhr.getDirection() != SteamDrillHooks.breakingSide;
            }
        }
        return false;
    }

    @Override
    public boolean hasShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public void withStandardItemRenderer(Consumer<?> stupidClientProperties) {
        ((Consumer<IClientItemExtensions>) stupidClientProperties).accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return RenderHelper.BLOCK_AND_ENTITY_RENDERER;
            }
        });
    }

    @Override
    public void registerPartTankClient(Supplier<BlockEntityType<AbstractTankBlockEntity>> blockEntityType, int meanRgb) {
        MIClient.registerBlockEntityRenderer(blockEntityType, context -> new TankRenderer(TextureHelper.getOverlayTextColor(meanRgb)));
    }

    @Override
    public void registerPartBarrelClient(Supplier<BlockEntityType<BarrelBlockEntity>> blockEntityType, int meanRgb) {
        MIClient.registerBlockEntityRenderer(blockEntityType, context -> new BarrelRenderer(TextureHelper.getOverlayTextColor(meanRgb)));
    }

    @Override
    public MachineMenuCommon createClientMachineMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        return MachineMenuClient.create(syncId, playerInventory, buf);
    }

    @Override
    public BlockState getMachineCasingBlockState(BlockState state, BlockAndTintGetter renderView, BlockPos pos) {
        var be = renderView.getBlockEntity(pos); // Note: not safe to access fields!
        if (!MIConfig.getConfig().enableInterMachineConnectedTextures) {
            // Use the machine's own state, unless we are a hatch or a multiblock controller of course.
            if (!(be instanceof HatchBlockEntity) && !(be instanceof MultiblockMachineBlockEntity)) {
                return state;
            }
        }

        var modelData = renderView.getModelDataManager().getAtOrEmpty(pos);
        var clientData = modelData.get(MachineModelClientData.KEY);
        if (clientData == null) {
            // Not a machine's data!
            return state;
        }
        var casing = clientData.casing;
        if (casing == null) {
            // No override, then pull the casing from the machine's baked model.
            var machineModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            if (machineModel instanceof MachineBakedModel mbm) {
                casing = mbm.getBaseCasing();
            } else {
                // Couldn't find casing... :(
                return state;
            }
        }

        // Pull the block state from the casing model if possible
        var casingModel = MachineBakedModel.getCasingModel(casing);
        if (casingModel instanceof UseBlockModelBakedModel ubmbm) {
            return ubmbm.getTargetState();
        }
        // Couldn't find target state
        return state;
    }
}
