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
package aztech.modern_industrialization.compat.jei;

import aztech.modern_industrialization.MIIdentifier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCategory {

    private static final ResourceLocation ARROWS = new MIIdentifier("textures/gui/jei/arrow.png");

    private final LoadingCache<Integer, IDrawableAnimated> cachedArrows;
    private final IDrawableStatic unfilledArrow;
    private final IDrawableStatic filledArrow;

    public AbstractCategory(IGuiHelper guiHelper) {
        this.unfilledArrow = guiHelper.createDrawable(ARROWS, 0, 17, 24, 17);
        this.filledArrow = guiHelper.createDrawable(ARROWS, 0, 0, 24, 17);
        this.cachedArrows = CacheBuilder.newBuilder()
                .maximumSize(25)
                .build(new CacheLoader<>() {
                    @Override
                    public IDrawableAnimated load(Integer cookTime) {
                        return guiHelper.createAnimatedDrawable(filledArrow, cookTime, IDrawableAnimated.StartDirection.LEFT, false);
                    }
                });
    }

    protected final IDrawable getArrow(int cookTime) {
        if (cookTime <= 0) {
            return this.unfilledArrow;
        }
        return this.cachedArrows.getUnchecked(cookTime);
    }

}
