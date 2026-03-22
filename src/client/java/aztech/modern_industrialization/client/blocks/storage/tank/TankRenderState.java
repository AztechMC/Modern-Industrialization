package aztech.modern_industrialization.client.blocks.storage.tank;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public class TankRenderState extends BlockEntityRenderState {
    public FluidResource resource = FluidResource.EMPTY;
    public int fluidColor;
    public float fillLevel;
    public boolean locked;
}
