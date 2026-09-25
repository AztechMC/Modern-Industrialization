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

package aztech.modern_industrialization.client.compat.guideme;

import guideme.color.ConstantColor;
import guideme.document.LytRect;
import guideme.render.RenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class MIGuideMeRenderHelper {
    // Unfortunately GuideME does not provide a method that takes in UVs in int form
    public static void fillTexturedRect(RenderContext context, ResourceLocation textureLocation, int x, int y, int width, int height, int uOffset, int vOffset, int uWidth, int uHeight, int textureWidth, int textureHeight) {
        var texture = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);
        context.fillTexturedRect(
                new LytRect(x, y, width, height),
                texture,
                ConstantColor.WHITE, ConstantColor.WHITE, ConstantColor.WHITE, ConstantColor.WHITE,
                uOffset / (float) textureWidth,
                vOffset / (float) textureHeight,
                (uOffset + uWidth) / (float) textureWidth,
                (vOffset + uHeight) / (float) textureHeight);
    }
}
