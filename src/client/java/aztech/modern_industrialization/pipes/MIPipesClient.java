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
import aztech.modern_industrialization.pipes.api.PipeNetworkType;
import aztech.modern_industrialization.pipes.api.PipeRenderer;
import aztech.modern_industrialization.pipes.impl.PipeBlock;
import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import aztech.modern_industrialization.pipes.impl.PipeColorProvider;
import aztech.modern_industrialization.pipes.impl.PipeMeshCache;
import aztech.modern_industrialization.util.RenderHelper;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.common.NeoForge;

public class MIPipesClient {
    public static void setupClient(IEventBus modBus) {
        modBus.addListener(RegisterColorHandlersEvent.Block.class, event -> {
            event.register(new PipeColorProvider(), MIPipes.BLOCK_PIPE.get());
        });
        registerRenderers();

        NeoForge.EVENT_BUS.addListener(RenderHighlightEvent.Block.class, event -> {
            var level = Minecraft.getInstance().level;
            var pos = event.getTarget().getBlockPos();

            if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe && !pipe.hasCamouflage()) {
                var shape = PipeBlock.getHitPart(level, pos, event.getTarget());

                if (shape != null) {
                    Vec3 camPos = event.getCamera().getPosition();
                    RenderHelper.renderVoxelShape(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()), shape.shape,
                            pos.getX() - camPos.x(),
                            pos.getY() - camPos.y(),
                            pos.getZ() - camPos.z(),
                            0, 0, 0, 0.4f);
                    event.setCanceled(true);
                }
            }
        });

        NeoForge.EVENT_BUS.addListener(InputEvent.MouseScrollingEvent.class, event -> {
            var player = Objects.requireNonNull(Minecraft.getInstance().player);

            if (player.isShiftKeyDown() && MIItem.CONFIG_CARD.is(player.getItemInHand(InteractionHand.MAIN_HAND))) {
                // noinspection NonAtomicOperationOnVolatileField
                MIPipes.transparentCamouflage = !MIPipes.transparentCamouflage;
                Minecraft.getInstance().levelRenderer.allChanged();
                var miText = MIPipes.transparentCamouflage ? MIText.TransparentCamouflageEnabled : MIText.TransparentCamouflageDisabled;
                player.displayClientMessage(miText.text(), true);
                event.setCanceled(true);
            }
        });
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

    private static void registerRenderers() {
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
