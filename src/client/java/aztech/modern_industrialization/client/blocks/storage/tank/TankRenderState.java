package aztech.modern_industrialization.client.blocks.storage.tank;

import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class TankRenderState extends BlockEntityRenderState {
    public FluidVariant resource = FluidVariant.blank();
    public int fluidColor;
    public float fillLevel;
    public boolean locked;
}
