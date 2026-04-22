package aztech.modern_industrialization.client.machines;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import org.jspecify.annotations.Nullable;

public class MachineRenderState extends BlockEntityRenderState {
    public final ActiveOverlay[] activeOverlays = new ActiveOverlay[] {
            new ActiveOverlay(),new ActiveOverlay(),new ActiveOverlay(),new ActiveOverlay(),new ActiveOverlay(),new ActiveOverlay()};

    public static class ActiveOverlay {
        @Nullable
        BakedQuad quad;
        int packedLight;
    }
}
