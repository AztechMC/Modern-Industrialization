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

import aztech.modern_industrialization.blocks.storage.barrel.BarrelBlockEntity;
import aztech.modern_industrialization.blocks.storage.tank.AbstractTankBlockEntity;
import aztech.modern_industrialization.machines.gui.MachineMenuCommon;
import aztech.modern_industrialization.util.UnsidedPacketHandler;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

/**
 * Abstractions over client-only code called in common code.
 */
public class CommonProxy {
    public static CommonProxy INSTANCE = instantiateProxy();

    private static CommonProxy instantiateProxy() {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                Class<?> clientProxy = Class.forName("aztech.modern_industrialization.proxy.ClientProxy");
                return (CommonProxy) clientProxy.getConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Modern Industrialization ClientProxy.", e);
            }
        } else {
            return new CommonProxy();
        }
    }

    private static MinecraftServer currentServer = null;

    public static void initEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> currentServer = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> currentServer = null);
    }

    public static MinecraftServer getCurrentServer() {
        return currentServer;
    }

    /**
     * Try to find a suitable user.
     */
    @Nullable
    public Player findUser(ItemStack mainHand) {
        if (currentServer != null) {
            for (var player : currentServer.getPlayerList().getPlayers()) {
                if (player.getMainHandItem() == mainHand) {
                    return player;
                }
            }
        }
        return null;
    }

    public void delayNextBlockAttack(Player player) {
    }

    public boolean hasShiftDown() {
        return false;
    }

    public List<Component> getFluidTooltip(FluidVariant variant) {
        List<Component> list = new ArrayList<>();
        list.add(FluidVariantAttributes.getName(variant));
        return list;
    }

    public void registerUnsidedPacket(ResourceLocation identifier, UnsidedPacketHandler handler) {
        ServerPlayNetworking.registerGlobalReceiver(identifier, (server, player, listener, buf, responseSender) -> {
            server.execute(handler.handlePacket(player, buf));
        });
    }

    public void registerPartTankClient(Block tankBlock, Item tankItem, String materialName, String itemPath,
            BlockEntityType<AbstractTankBlockEntity> blockEntityType, int meanRgb) {
    }

    public void registerPartBarrelClient(Block barrelBlock, Item barrelItem, String materialName, String itemPath,
            BlockEntityType<BarrelBlockEntity> blockEntityType, int meanRgb) {
    }

    public MachineMenuCommon createClientMachineMenu(int syncId, Inventory playerInventory, FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Only supported on the server");
    }
}
