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
package aztech.modern_industrialization.compat.rei.machines;

import aztech.modern_industrialization.MIIdentifier;
import aztech.modern_industrialization.compat.rei.Rectangle;
import aztech.modern_industrialization.compat.rei.machines.ReiMachineRecipes.ClickAreaCategory;
import aztech.modern_industrialization.machines.MachineScreenHandlers;
import aztech.modern_industrialization.machines.multiblocks.ShapeTemplate;
import java.util.*;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;

/**
 * Helper to register stuff for use with the REI plugin. This class will be
 * called even if REI is not loaded, and should not reference the REI API.
 */
public class ReiMachineRecipes {
    public static final Map<String, MachineCategoryParams> categories = new TreeMap<>();
    /**
     * Maps a machine block id to the list of recipe categories.
     */
    static final Map<String, List<ClickAreaCategory>> machineToClickAreaCategory = new HashMap<>();
    /**
     * Maps a machine block id to the parameters of the click area for the recipe.
     */
    static final Map<String, Rectangle> machineToClickArea = new HashMap<>();
    /**
     * List of registered multiblock shape "recipes".
     */
    static final List<Tuple<String, ShapeTemplate>> multiblockShapes = new ArrayList<>();

    public static void registerCategory(String machine, MachineCategoryParams params) {
        if (categories.put(machine, params) != null) {
            throw new IllegalStateException("Machine was already registered: " + machine);
        }
    }

    public static void registerWorkstation(String machine, String item) {
        MachineCategoryParams params = categories.get(machine);
        if (params == null) {
            throw new NullPointerException("Machine params may not be null for machine " + machine);
        }
        params.workstations.add(item);
    }

    public static void registerRecipeCategoryForMachine(String machine, String category) {
        registerRecipeCategoryForMachine(machine, category, screen -> true);
    }

    public static void registerRecipeCategoryForMachine(String machine, String category,
            Predicate<MachineScreenHandlers.ClientScreen> screenPredicate) {
        machineToClickAreaCategory.computeIfAbsent(machine, k -> new ArrayList<>())
                .add(new ClickAreaCategory(new MIIdentifier(category), screenPredicate));
    }

    public static void registerMachineClickArea(String machine, Rectangle clickArea) {
        machineToClickArea.put(machine, clickArea);
    }

    public static void registerMultiblockShape(String machine, ShapeTemplate shapeTemplate) {
        multiblockShapes.add(new Tuple<>(machine, shapeTemplate));
    }

    static class ClickAreaCategory {
        public final ResourceLocation category;
        public final Predicate<MachineScreenHandlers.ClientScreen> predicate;

        ClickAreaCategory(ResourceLocation category, Predicate<MachineScreenHandlers.ClientScreen> predicate) {
            this.category = category;
            this.predicate = predicate;
        }
    }
}
