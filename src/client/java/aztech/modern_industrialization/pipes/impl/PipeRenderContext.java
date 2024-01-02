package aztech.modern_industrialization.pipes.impl;

import aztech.modern_industrialization.thirdparty.fabricrendering.EncodingFormat;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadView;
import aztech.modern_industrialization.thirdparty.fabricrendering.MutableQuadViewImpl;
import aztech.modern_industrialization.thirdparty.fabricrendering.QuadEmitter;
import aztech.modern_industrialization.thirdparty.fabricrendering.SpriteFinder;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.ArrayList;
import java.util.List;

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
