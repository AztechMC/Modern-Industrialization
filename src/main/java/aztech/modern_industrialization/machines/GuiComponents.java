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

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.machines.gui.GuiComponent;
import aztech.modern_industrialization.machines.guicomponents.AutoExtract;
import aztech.modern_industrialization.machines.guicomponents.CraftingMultiblockGui;
import aztech.modern_industrialization.machines.guicomponents.EnergyBar;
import aztech.modern_industrialization.machines.guicomponents.FluidGUIComponent;
import aztech.modern_industrialization.machines.guicomponents.GunpowderOverclockGui;
import aztech.modern_industrialization.machines.guicomponents.NuclearReactorGui;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.guicomponents.RecipeEfficiencyBar;
import aztech.modern_industrialization.machines.guicomponents.ReiSlotLocking;
import aztech.modern_industrialization.machines.guicomponents.ShapeSelection;
import aztech.modern_industrialization.machines.guicomponents.SlotPanel;
import aztech.modern_industrialization.machines.guicomponents.TemperatureBar;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class GuiComponents {
    public static final ResourceLocation AUTO_EXTRACT = new MIIdentifier("auto_extract");
    public static final ResourceLocation CRAFTING_MULTIBLOCK_GUI = new MIIdentifier("crafting_multiblock_gui");
    public static final ResourceLocation ENERGY_BAR = new MIIdentifier("energy_bar");
    public static final ResourceLocation FLUID_STORAGE_GUI = new MIIdentifier("fluid_storage_gui");
    public static final ResourceLocation GUNPOWDER_OVERCLOCK_GUI = new MIIdentifier("gunpowder_overclock_gui");
    public static final ResourceLocation NUCLEAR_REACTOR_GUI = new MIIdentifier("nuclear_reactor_gui");
    public static final ResourceLocation PROGRESS_BAR = new MIIdentifier("progress_bar");
    public static final ResourceLocation RECIPE_EFFICIENCY_BAR = new MIIdentifier("recipe_efficiency_bar");
    public static final ResourceLocation REI_SLOT_LOCKING = new MIIdentifier("rei_slot_locking");
    public static final ResourceLocation SHAPE_SELECTION = new MIIdentifier("shape_selection");
    public static final ResourceLocation SLOT_PANEL = new MIIdentifier("slot_panel");
    public static final ResourceLocation TEMPERATURE_BAR = new MIIdentifier("temperature_bar");

    public static final class Client {
        private static final Map<ResourceLocation, GuiComponent.ClientFactory> components = new HashMap<>();

        public static GuiComponent.ClientFactory get(ResourceLocation identifier) {
            return components.get(identifier);
        }

        public static void register(ResourceLocation id, GuiComponent.ClientFactory clientFactory) {
            if (components.put(id, clientFactory) != null) {
                throw new RuntimeException("Duplicate registration of component identifier.");
            }
        }

        static {
            register(AUTO_EXTRACT, AutoExtract.Client::new);
            register(CRAFTING_MULTIBLOCK_GUI, CraftingMultiblockGui.Client::new);
            register(ENERGY_BAR, EnergyBar.Client::new);
            register(FLUID_STORAGE_GUI, FluidGUIComponent.Client::new);
            register(GUNPOWDER_OVERCLOCK_GUI, GunpowderOverclockGui.Client::new);
            register(NUCLEAR_REACTOR_GUI, NuclearReactorGui.Client::new);
            register(PROGRESS_BAR, ProgressBar.Client::new);
            register(RECIPE_EFFICIENCY_BAR, RecipeEfficiencyBar.Client::new);
            register(REI_SLOT_LOCKING, ReiSlotLocking.Client::new);
            register(SHAPE_SELECTION, ShapeSelection.Client::new);
            register(SLOT_PANEL, SlotPanel.Client::new);
            register(TEMPERATURE_BAR, TemperatureBar.Client::new);
        }
    }
}
