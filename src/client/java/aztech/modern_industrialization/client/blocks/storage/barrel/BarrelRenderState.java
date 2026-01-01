package aztech.modern_industrialization.client.blocks.storage.barrel;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class BarrelRenderState extends BlockEntityRenderState {
    public boolean locked;
    public final ItemStackRenderState stackRenderState = new ItemStackRenderState();
}
