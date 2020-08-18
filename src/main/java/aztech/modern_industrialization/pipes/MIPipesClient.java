package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.impl.PipeColorProvider;
import aztech.modern_industrialization.pipes.impl.PipeModelProvider;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;

public class MIPipesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModernIndustrialization.RESOURCE_PACK.addBlockState(JState.state(new JVariant().put("", new JBlockModel("modern_industrialization:block/pipe"))), new MIIdentifier("pipe"));
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new PipeModelProvider());
        ColorProviderRegistry.BLOCK.register(new PipeColorProvider(), MIPipes.BLOCK_PIPE);
    }
}
