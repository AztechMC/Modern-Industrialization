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

import aztech.modern_industrialization.MI;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import java.io.IOException;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jspecify.annotations.Nullable;

public class MIShaders {
    @Nullable
    private static ShaderInstance TRANSLUCENT_HIGHLIGHT_INSTANCE;

    private static ShaderInstance translucentHighlight() {
        return TRANSLUCENT_HIGHLIGHT_INSTANCE;
    }

    public static final RenderStateShard.ShaderStateShard TRANSLUCENT_HIGHLIGHT = new RenderStateShard.ShaderStateShard(MIShaders::translucentHighlight);

    public static void init(IEventBus bus) {
        bus.addListener(MIShaders::registerShaders);
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), MI.id("translucent_highlight"), DefaultVertexFormat.BLOCK), (shader) -> TRANSLUCENT_HIGHLIGHT_INSTANCE = shader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
