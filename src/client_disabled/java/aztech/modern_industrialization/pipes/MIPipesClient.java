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
package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIConfig;
import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.MIItem;
import aztech.modern_industrialization.MIText;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreen;
import aztech.modern_industrialization.pipes.impl.ClientPipePackets;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeColorProvider;
import aztech.modern_industrialization.pipes.impl.PipeMeshCache;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.pipes.impl.PipeUnbakedModel;
import aztech.modern_industrialization.pipes.item.ItemPipeScreen;
import aztech.modern_industrialization.util.InGameMouseScrollCallback;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.loading.v1.DelegatingUnbakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MIPipesClient {
    public static volatile boolean transparentCamouflage = false;

    public void setupClient() {
        ModelLoadingPlugin.register(pluginCtx -> {
            ResourceLocation blockModelLocation = new MIIdentifier("block/pipe");
            UnbakedModel itemModel = new DelegatingUnbakedModel(blockModelLocation);

            pluginCtx.resolveModel().register(ctx -> {
                if (!ctx.id().getNamespace().equals(ModernIndustrialization.MOD_ID)) {
                    return null;
                }

                if (ctx.id().equals(blockModelLocation)) {
                    return new PipeUnbakedModel();
                }
                if (MIPipes.ITEM_PIPE_MODELS.contains(ctx.id())) {
                    return itemModel;
                }
                return null;
            });
        });

        ColorProviderRegistry.BLOCK.register(new PipeColorProvider(), MIPipes.BLOCK_PIPE);
        MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_ITEM_PIPE, ItemPipeScreen::new);
        MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE, FluidPipeScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(PipePackets.SET_PRIORITY, ClientPipePackets.ON_SET_PRIORITY);
        registerRenderers();

        WorldRenderEvents.BLOCK_OUTLINE.register((wrc, boc) -> {
            if (wrc.world().getBlockEntity(boc.blockPos()) instanceof PipeBlockEntity pipe && !pipe.hasCamouflage()) {
                var shape = PipeBlock.getHitPart(wrc.world(), boc.blockPos(), (BlockHitResult) Minecraft.getInstance().hitResult);

                if (shape != null) {
                    BlockPos pos = boc.blockPos();
                    Vec3 camPos = wrc.camera().getPosition();
                    RenderHelper.renderVoxelShape(wrc.matrixStack(), wrc.consumers().getBuffer(RenderType.lines()), shape.shape,
                            pos.getX() - camPos.x(),
                            pos.getY() - camPos.y(),
                            pos.getZ() - camPos.z(),
                            0, 0, 0, 0.4f);
                    return false;
                }
            }

            return true;
        });

        ClientPickBlockGatherCallback.EVENT.register((player, result) -> {
            if (result instanceof BlockHitResult bhr) {
                if (player.level().getBlockEntity(bhr.getBlockPos()) instanceof PipeBlockEntity pipe) {
                    if (pipe.hasCamouflage()) {
                        return pipe.getCamouflageStack();
                    }

                    var targetedPart = PipeBlock.getHitPart(player.level(), bhr.getBlockPos(), bhr);
                    return new ItemStack(targetedPart == null ? Items.AIR : MIPipes.INSTANCE.getPipeItem(targetedPart.type));
                }
            }

            return ItemStack.EMPTY;
        });

        InGameMouseScrollCallback.EVENT.register((player, direction) -> {
            if (player.isShiftKeyDown() && MIItem.CONFIG_CARD.is(player.getItemInHand(InteractionHand.MAIN_HAND))) {
                // noinspection NonAtomicOperationOnVolatileField
                transparentCamouflage = !transparentCamouflage;
                Minecraft.getInstance().levelRenderer.allChanged();
                var miText = transparentCamouflage ? MIText.TransparentCamouflageEnabled : MIText.TransparentCamouflageDisabled;
                player.displayClientMessage(miText.text(), true);
                return false;
            }

            return true;
        });

        BlockRenderLayerMap.INSTANCE.putBlock(MIPipes.BLOCK_PIPE, RenderType.cutout());
    }

    private static PipeRenderer.Factory makeRenderer(List<String> sprites, boolean innerQuads) {
        return new PipeRenderer.Factory() {
            @Override
            public Collection<Material> getSpriteDependencies() {
                return sprites.stream().map(
                        n -> new Material(InventoryMenu.BLOCK_ATLAS, new MIIdentifier("block/pipes/" + n)))
                        .collect(Collectors.toList());
            }

            @Override
            public PipeRenderer create(Function<Material, TextureAtlasSprite> textureGetter) {
                Material[] ids = sprites.stream()
                        .map(n -> new Material(InventoryMenu.BLOCK_ATLAS,
                                new MIIdentifier("block/pipes/" + n)))
                        .toArray(Material[]::new);
                return new PipeMeshCache(textureGetter, ids, innerQuads);
            }
        };
    }

    private static final PipeRenderer.Factory ITEM_RENDERER = makeRenderer(Arrays.asList("item", "item_item", "item_in", "item_in_out", "item_out"),
            false);
    private static final PipeRenderer.Factory FLUID_RENDERER = makeRenderer(
            Arrays.asList("fluid", "fluid_item", "fluid_in", "fluid_in_out", "fluid_out"), true);
    private static final PipeRenderer.Factory ELECTRICITY_RENDERER = makeRenderer(Arrays.asList("electricity", "electricity_blocks"), false);

    // Use a set to avoid loading the same renderer multiple times
    public static final Collection<PipeRenderer.Factory> RENDERERS = new LinkedHashSet<>();

    private void registerRenderers() {
        for (var type : PipeNetworkType.getTypes().values()) {
            if (type.getIdentifier().getPath().endsWith("item_pipe")) {
                PipeRenderer.register(type, ITEM_RENDERER);
            } else if (type.getIdentifier().getPath().endsWith("fluid_pipe")) {
                PipeRenderer.register(type, FLUID_RENDERER);
            } else if (type.getIdentifier().getPath().endsWith("cable")) {
                PipeRenderer.register(type, ELECTRICITY_RENDERER);
            }
        }

        if (MIConfig.loadAe2Compat()) {
            try {
                Class.forName("aztech.modern_industrialization.compat.ae2.MIAEAddonClient")
                        .getMethod("registerPipeRenderers")
                        .invoke(null);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        for (var value : PipeNetworkType.getTypes().values()) {
            RENDERERS.add(PipeRenderer.get(value));
        }
    }
}
