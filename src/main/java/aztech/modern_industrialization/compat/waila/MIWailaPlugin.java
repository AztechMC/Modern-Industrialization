package aztech.modern_industrialization.compat.waila;

import aztech.modern_industrialization.pipes.impl.PipeBlockEntity;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;

public class MIWailaPlugin implements IWailaPlugin {
    @Override
    public void register(IRegistrar r) {
        r.registerBlockDataProvider(new PipeDataProvider(), PipeBlockEntity.class);

        PipeComponentProvider pipeComponentProvider = new PipeComponentProvider();
        r.registerComponentProvider(pipeComponentProvider, TooltipPosition.HEAD, PipeBlockEntity.class);
        r.registerComponentProvider(pipeComponentProvider, TooltipPosition.BODY, PipeBlockEntity.class);
    }
}
