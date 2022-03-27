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

import aztech.modern_industrialization.materials.MaterialBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

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
    static {
        ServerLifecycleEvents.SERVER_STARTED.register(s -> currentServer = s);
        ServerLifecycleEvents.SERVER_STOPPED.register(s -> currentServer = null);
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

    public void registerPartTankClient(MaterialBuilder.PartContext partContext, String itemPath, BlockEntityType<BlockEntity> blockEntityType) {
    }
}
