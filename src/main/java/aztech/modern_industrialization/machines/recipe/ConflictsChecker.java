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

package aztech.modern_industrialization.machines.recipe;

import aztech.modern_industrialization.inventory.ConfigurableFluidStack;
import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.init.MIMachineRecipeTypes;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.fluid.FluidVariant;
import aztech.modern_industrialization.thirdparty.fabrictransfer.api.item.ItemVariant;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.loading.FMLPaths;

public class ConflictsChecker {
    public static Path writeConflictReport(ServerLevel level) throws IOException {
        StringBuilder conflictsReport = new StringBuilder();
        int conflictCount = 0;
        int conflictTypeCount = 0;

        for (var type : MIMachineRecipeTypes.getRecipeTypes()) {
            var conflicts = new TreeMap<ResourceLocation, List<ResourceLocation>>();

            for (var recipe : type.getRecipesWithCache(level)) {
                var itemConfStacks = recipe.value().itemInputs.stream()
                        .map(input -> {
                            var confStack = new ConfigurableItemStack();
                            var item = input.getInputItems().stream().findFirst().orElse(Items.AIR);
                            confStack.setKey(ItemVariant.of(item));
                            confStack.setAmount(Integer.MAX_VALUE);
                            return confStack;
                        })
                        .toList();
                var fluidConfStacks = recipe.value().fluidInputs.stream()
                        .map(input -> {
                            var confStack = new ConfigurableFluidStack(Integer.MAX_VALUE);
                            var fluid = input.getInputFluids().stream().findFirst().orElse(Fluids.EMPTY);
                            confStack.setKey(FluidVariant.of(fluid));
                            confStack.setAmount(Integer.MAX_VALUE);
                            return confStack;
                        })
                        .toList();

                for (var otherRecipe : CrafterComponent.getRecipes(level, type, itemConfStacks)) {
                    if (otherRecipe.id().equals(recipe.id())) {
                        continue;
                    }

                    if (CrafterComponent.doInputsMatch(itemConfStacks, fluidConfStacks, otherRecipe.value())) {
                        conflicts.computeIfAbsent(recipe.id(), i -> new ArrayList<>())
                                .add(otherRecipe.id());
                        conflictCount++;
                    }
                }
            }

            if (conflicts.isEmpty()) {
                continue;
            }

            conflictTypeCount++;
            conflictsReport.append("\n\n");
            conflictsReport.append("Conflicts for ").append(type.getId()).append(":\n");
            for (var entry : conflicts.entrySet()) {
                conflictsReport.append("- ").append(entry.getKey()).append(" conflicts with:\n");
                Collections.sort(entry.getValue());
                for (var other : entry.getValue()) {
                    conflictsReport.append("  - ").append(other).append("\n");
                }
            }
        }

        String finalReport;
        if (conflictsReport.isEmpty()) {
            finalReport = "No conflicts found! Note that the detection is approximate.\n";
        } else {
            finalReport = "Found " + conflictCount + " conflicts over " + conflictTypeCount + " recipe types!\n";
            finalReport += conflictsReport.toString();
        }

        var filePath = FMLPaths.GAMEDIR.get().resolve("modern_industrialization").resolve("recipe_conflicts.txt");
        try (var os = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            os.write(finalReport.getBytes(StandardCharsets.UTF_8));
        }

        return filePath;
    }
}
