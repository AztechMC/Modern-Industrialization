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
package aztech.modern_industrialization.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class MIRenderTypes {
    private static RenderType MACHINE_WRENCH_OVERLAY;
    private static RenderType SOLID_HIGHLIGHT; // used by hatch preview and wrong block highlight

    public static RenderType machineOverlay() {
        if (MACHINE_WRENCH_OVERLAY == null) {
            MACHINE_WRENCH_OVERLAY = Factory.makeMachineOverlay();
        }
        return MACHINE_WRENCH_OVERLAY;
    }

    public static RenderType solidHighlight() {
        if (SOLID_HIGHLIGHT == null) {
            SOLID_HIGHLIGHT = Factory.makeSolidHighlight();
        }
        return SOLID_HIGHLIGHT;
    }

    // This is a subclass to get access to a bunch of fields and classes.
    // TODO: PR more transitive access wideners to fabric
    private static class Factory extends RenderType {
        private Factory(String string, VertexFormat vertexFormat, VertexFormat.Mode mode, int i, boolean bl, boolean bl2, Runnable runnable,
                Runnable runnable2) {
            super(string, vertexFormat, mode, i, bl, bl2, runnable, runnable2);
        }

        private static RenderType makeMachineOverlay() {
            return create("machine_overlay", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.QUADS, 65536, false, true,
                    CompositeState.builder()
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setTextureState(NO_TEXTURE)
                            .setLightmapState(NO_LIGHTMAP)
                            .setShaderState(POSITION_COLOR_SHADER)
                            .createCompositeState(false));
        }

        private static RenderType makeSolidHighlight() {
            // Use block vertex format to use the fast path in BufferBuilder, even if the shader doesn't use the extra vertex attributes.
            return create("solid_highlight", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 65536, false, false,
                    CompositeState.builder()
                            .setTransparencyState(NO_TRANSPARENCY)
                            .setTextureState(NO_TEXTURE)
                            .setLightmapState(NO_LIGHTMAP)
                            .setShaderState(POSITION_COLOR_SHADER)
                            .createCompositeState(false));
        }
    }
}
