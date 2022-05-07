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
package aztech.modern_industrialization.pipes;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.ModernIndustrialization;
import aztech.modern_industrialization.pipes.fluid.FluidPipeScreen;
import aztech.modern_industrialization.pipes.impl.ClientPipePackets;
import aztech.modern_industrialization.pipes.impl.PipeColorProvider;
import aztech.modern_industrialization.pipes.impl.PipeModelProvider;
import aztech.modern_industrialization.pipes.impl.PipePackets;
import aztech.modern_industrialization.pipes.item.ItemPipeScreen;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class MIPipesClient {
    public void setupClient() {
        ModernIndustrialization.RESOURCE_PACK.addBlockState(
                JState.state(new JVariant().put("", new JBlockModel("modern_industrialization:block/pipe"))), new MIIdentifier("pipe"));
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(rm -> new PipeModelProvider());
        ColorProviderRegistry.BLOCK.register(new PipeColorProvider(), MIPipes.BLOCK_PIPE);
        MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_ITEM_PIPE, ItemPipeScreen::new);
        MenuScreens.register(MIPipes.SCREEN_HANDLER_TYPE_FLUID_PIPE, FluidPipeScreen::new);
        registerPackets();

        PipeModelProvider.modelNames.addAll(MIPipes.PIPE_MODEL_NAMES);
    }

    public void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(PipePackets.SET_ITEM_WHITELIST, PipePackets.ON_SET_ITEM_WHITELIST::handleS2C);
        ClientPlayNetworking.registerGlobalReceiver(PipePackets.SET_CONNECTION_TYPE, PipePackets.ON_SET_CONNECTION_TYPE::handleS2C);
        ClientPlayNetworking.registerGlobalReceiver(PipePackets.SET_PRIORITY, ClientPipePackets.ON_SET_PRIORITY);
        ClientPlayNetworking.registerGlobalReceiver(PipePackets.SET_NETWORK_FLUID, PipePackets.ON_SET_NETWORK_FLUID::handleS2C);
    }
}
