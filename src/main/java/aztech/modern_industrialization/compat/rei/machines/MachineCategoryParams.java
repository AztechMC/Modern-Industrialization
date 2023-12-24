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

import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.machines.guicomponents.ProgressBar;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;

public class MachineCategoryParams {
    public final String englishName;
    public final String category;
    public final SlotPositions itemInputs;
    public final SlotPositions itemOutputs;
    public final SlotPositions fluidInputs;
    public final SlotPositions fluidOutputs;
    public final ProgressBar.Parameters progressBarParams;
    public final Predicate<MachineRecipe> recipePredicate;
    public final boolean isMultiblock;
    public final SteamMode steamMode;
    public final List<ResourceLocation> workstations = new ArrayList<>();

    public MachineCategoryParams(String englishName, String category, SlotPositions itemInputs, SlotPositions itemOutputs, SlotPositions fluidInputs,
            SlotPositions fluidOutputs, ProgressBar.Parameters progressBarParams, Predicate<MachineRecipe> recipePredicate, boolean isMultiblock,
            SteamMode steamMode) {
        this.englishName = englishName;
        this.category = category;
        this.itemInputs = itemInputs;
        this.itemOutputs = itemOutputs;
        this.fluidInputs = fluidInputs;
        this.fluidOutputs = fluidOutputs;
        this.progressBarParams = progressBarParams;
        this.recipePredicate = recipePredicate;
        this.isMultiblock = isMultiblock;
        this.steamMode = steamMode;
    }
}
