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
package aztech.modern_industrialization.util;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class RenderHelper {
    private static final BakedQuad[] quads;
    private static final float W = 0.05f;

    public static void drawOverlay(MatrixStack ms, VertexConsumerProvider vcp, float r, float g, float b, int light, int overlay) {
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucent());
        for (int i = 0; i < quads.length; ++i) {
            vc.quad(ms.peek(), quads[i], r, g, b, light, overlay);
        }
    }

    static {
        quads = new BakedQuad[24];
        Renderer r = RendererAccess.INSTANCE.getRenderer();
        RenderMaterial material = r.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find();
        for (Direction direction : Direction.values()) {
            QuadEmitter emitter;
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 0, 1, W, 0);
            emitter.material(material);
            quads[direction.getId() * 4] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, 1 - W, 1, 1, 0);
            emitter.material(material);
            quads[direction.getId() * 4 + 1] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 0, W, W, 1 - W, 0);
            emitter.material(material);
            quads[direction.getId() * 4 + 2] = emitter.toBakedQuad(0, null, false);
            emitter = r.meshBuilder().getEmitter();
            emitter.square(direction, 1 - W, W, 1, 1 - W, 0);
            emitter.material(material);
            quads[direction.getId() * 4 + 3] = emitter.toBakedQuad(0, null, false);
        }
    }
}
