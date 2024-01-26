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
package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.thirdparty.fabricrendering.EncodingFormat;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadView;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadViewImpl;
import aztech.modern_industrialization.thirdparty.fabricrendering.QuadEmitter;
import aztech.modern_industrialization.thirdparty.fabricrendering.SpriteFinder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class PipeRenderContext {
    private final SpriteFinder spriteFinder;
    private final boolean acceptNormalQuads;
    private final boolean acceptFluidQuads;
    private final List<QuadTransform> transforms = new ArrayList<>();
    final List<BakedQuad> quads = new ArrayList<>();
    private final MutableQuadViewImpl editorQuad = new MutableQuadViewImpl() {
        {
            data = new int[EncodingFormat.TOTAL_STRIDE];
            clear();
        }

        @Override
        public void emitDirectly() {
            var tag = tag();
            if (!acceptNormalQuads && tag == 0 || !acceptFluidQuads && tag == 1) {
                return;
            }

            if (!passTransforms(this)) {
                return;
            }

            quads.add(editorQuad.toBakedQuad(spriteFinder.find(editorQuad)));
        }
    };

    public PipeRenderContext(SpriteFinder spriteFinder, boolean acceptNormalQuads, boolean acceptFluidQuads) {
        this.spriteFinder = spriteFinder;
        this.acceptNormalQuads = acceptNormalQuads;
        this.acceptFluidQuads = acceptFluidQuads;
    }

    private boolean passTransforms(MutableQuadView q) {
        int i = transforms.size() - 1;

        while (i >= 0) {
            if (!transforms.get(i--).transform(q)) {
                return false;
            }
        }

        return true;
    }

    public void pushTransform(QuadTransform transform) {
        this.transforms.add(transform);
    }

    public void popTransform() {
        this.transforms.remove(this.transforms.size() - 1);
    }

    public QuadEmitter getEmitter() {
        return this.editorQuad;
    }

    public interface QuadTransform {
        boolean transform(MutableQuadView quad);
    }
}
