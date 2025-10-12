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

package aztech.modern_industrialization.compat.viewer.impl.jei;

import aztech.modern_industrialization.compat.viewer.abstraction.ViewerCategory;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.client.gui.GuiGraphics;

public class DrawableIcon implements IDrawable {
    private final ViewerCategory.Icon.Texture texture;

    public static IDrawable create(IGuiHelper guiHelper, ViewerCategory.Icon icon) {
        return switch (icon) {
        case ViewerCategory.Icon.Stack stack -> guiHelper.createDrawableItemStack(stack.stack());
        case ViewerCategory.Icon.Texture texture -> new DrawableIcon(texture);
        case null -> throw new NullPointerException("Icon cannot be null");
        };
    }

    public DrawableIcon(ViewerCategory.Icon.Texture texture) {
        this.texture = texture;
    }

    @Override
    public int getWidth() {
        return 18;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
        guiGraphics.blit(texture.loc(), xOffset - 1, yOffset - 1, 0, texture.u(), texture.v(), 18, 18, 256, 256);
    }
}
