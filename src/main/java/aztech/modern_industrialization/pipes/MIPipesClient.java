package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.impl.PipeColorProvider;
import aztech.modern_industrialization.pipes.impl.PipeModelProvider;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.pipes.item.ItemPipeScreen;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;

public class MIPipesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModernIndustrialization.RESOURCE_PACK.addBlockState(
                JState.state(new JVariant().put("", new JBlockModel("modern_industrialization:block/pipe"))), new MIIdentifier("pipe"));
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new PipeModelProvider());
        ColorProviderRegistry.BLOCK.register(new PipeColorProvider(), MIPipes.BLOCK_PIPE);
        ScreenRegistry.register(MIPipes.SCREN_HANDLER_TYPE_ITEM_PIPE, ItemPipeScreen::new);
        registerPackets();

        PipeModelProvider.modelNames.addAll(MIPipes.PIPE_MODEL_NAMES);
    }

    public void registerPackets() {
        ClientSidePacketRegistry.INSTANCE.register(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST);
        ClientSidePacketRegistry.INSTANCE.register(PipePackets.SET_ITEM_CONNECTION_TYPE, PipePackets.ON_SET_ITEM_CONNECTION_TYPE);
        ClientSidePacketRegistry.INSTANCE.register(PipePackets.SET_ITEM_PRIORITY, PipePackets.ON_SET_ITEM_PRIORITY);
    }
}
