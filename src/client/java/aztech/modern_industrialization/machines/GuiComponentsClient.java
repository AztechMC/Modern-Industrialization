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
import aztech.modern_industrialization.machines.guicomponents.AutoExtractClient;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.EnergyBarClient;
import aztech.modern_industrialization.machines.guicomponents.GunpowderOverclockGuiClient;
import aztech.modern_industrialization.machines.guicomponents.LargeTankFluidDisplayClient;
import aztech.modern_industrialization.machines.guicomponents.NuclearReactorGuiClient;
import aztech.modern_industrialization.machines.guicomponents.ProgressBarClient;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBarClient;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLockingClient;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelectionClient;
import aztech.modern_industrialization.machines.guicomponents.SlotPanelClient;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBarClient;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class GuiComponentsClient {
    private static final Map<ResourceLocation, GuiComponentClient.Factory> components = new HashMap<>();

    public static GuiComponentClient.Factory get(ResourceLocation identifier) {
        return components.get(identifier);
    }

    public static void register(ResourceLocation id, GuiComponentClient.Factory clientFactory) {
        if (components.put(id, clientFactory) != null) {
            throw new RuntimeException("Duplicate registration of component identifier.");
        }
    }

    static {
        register(GuiComponents.AUTO_EXTRACT, AutoExtractClient::new);
        register(GuiComponents.CRAFTING_MULTIBLOCK_GUI, CraftingMultiblockGuiClient::new);
        register(GuiComponents.ENERGY_BAR, EnergyBarClient::new);
        register(GuiComponents.LARGE_TANK_FLUID_DISPLAY, LargeTankFluidDisplayClient::new);
        register(GuiComponents.GUNPOWDER_OVERCLOCK_GUI, GunpowderOverclockGuiClient::new);
        register(GuiComponents.NUCLEAR_REACTOR_GUI, NuclearReactorGuiClient::new);
        register(GuiComponents.PROGRESS_BAR, ProgressBarClient::new);
        register(GuiComponents.RECIPE_EFFICIENCY_BAR, RecipeEfficiencyBarClient::new);
        register(GuiComponents.REI_SLOT_LOCKING, ReiSlotLockingClient::new);
        register(GuiComponents.SHAPE_SELECTION, ShapeSelectionClient::new);
        register(GuiComponents.SLOT_PANEL, SlotPanelClient::new);
        register(GuiComponents.TEMPERATURE_BAR, TemperatureBarClient::new);
    }
}
