package aztech.modern_industrialization;

import aztech.modern_industrialization.gui.SteamBoilerScreen;
import aztech.modern_industrialization.model.block.ModelProvider;
import aztech.modern_industrialization.model.Models;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

public class ModernIndustrializationClient implements ClientModInitializer {
    public static final String MOD_ID = ModernIndustrialization.MOD_ID;

    @Override
    public void onInitializeClient() {
        setupScreens();
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> {
            return new ModelProvider();
        });

        ModernIndustrialization.LOGGER.info("Modern Industrialization client setup done!");
    }

    private void setupScreens() {
        ScreenRegistry.register(ModernIndustrialization.SCREEN_HANDLER_TYPE_STEAM_BOILER, SteamBoilerScreen::new);
    }
}
