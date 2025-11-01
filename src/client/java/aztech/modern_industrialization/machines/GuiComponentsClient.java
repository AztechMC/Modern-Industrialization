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

package aztech.modern_industrialization.machines;

import aztech.modern_industrialization.machines.gui.GuiComponentClient;
import aztech.modern_industrialization.machines.gui.GuiComponentServer;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.AutoExtractClient;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.EnergyBarClient;
import aztech.modern_industrialization.machines.guicomponents.GeneratorMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.GeneratorMultiblockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.GunpowderOverclockGui;
import aztech.modern_industrialization.machines.guicomponents.GunpowderOverclockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.LargeTankFluidDisplay;
import aztech.modern_industrialization.machines.guicomponents.LargeTankFluidDisplayClient;
import aztech.modern_industrialization.machines.guicomponents.NuclearReactorGui;
import aztech.modern_industrialization.machines.guicomponents.NuclearReactorGuiClient;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.ProgressBarClient;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBarClient;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLockingClient;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelectionClient;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.guicomponents.SlotPanelClient;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBar;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBarClient;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class GuiComponentsClient {
    private static final Map<ResourceLocation, Registration<?, ?>> components = new HashMap<>();

    public static Registration<?, ?> get(ResourceLocation id) {
        var registration = components.get(id);
        if (registration == null) {
            throw new IllegalArgumentException("Unknown GUI component ID: " + id);
        }
        return registration;
    }

    public static <P, D> void register(GuiComponentServer.Type<P, D> type, BiFunction<P, D, GuiComponentClient<P, D>> clientFactory) {
        if (components.put(type.id(), new Registration<>(type, clientFactory)) != null) {
            throw new RuntimeException("Duplicate registration of component identifier.");
        }
    }

    static {
        register(AutoExtract.TYPE, AutoExtractClient::new);
        register(CraftingMultiblockGui.TYPE, CraftingMultiblockGuiClient::new);
        register(EnergyBar.TYPE, EnergyBarClient::new);
        register(LargeTankFluidDisplay.TYPE, LargeTankFluidDisplayClient::new);
        register(GeneratorMultiblockGui.TYPE, GeneratorMultiblockGuiClient::new);
        register(GunpowderOverclockGui.TYPE, GunpowderOverclockGuiClient::new);
        register(NuclearReactorGui.TYPE, NuclearReactorGuiClient::new);
        register(ProgressBar.TYPE, ProgressBarClient::new);
        register(RecipeEfficiencyBar.TYPE, RecipeEfficiencyBarClient::new);
        register(ReiSlotLocking.TYPE, ReiSlotLockingClient::new);
        register(ShapeSelection.TYPE, ShapeSelectionClient::new);
        register(SlotPanel.TYPE, SlotPanelClient::new);
        register(TemperatureBar.TYPE, TemperatureBarClient::new);
    }

    public record Registration<P, D>(GuiComponentServer.Type<P, D> type, BiFunction<P, D, GuiComponentClient<P, D>> clientFactory) {
        public GuiComponentClient<P, D> readNewComponent(RegistryFriendlyByteBuf buf) {
            P params = type.paramsCodec().decode(buf);
            D data = type.dataCodec().decode(buf);
            return clientFactory.apply(params, data);
        }
    }
}
